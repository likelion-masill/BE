package project.masil.community.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import project.masil.community.converter.EventPostConverter;
import project.masil.community.converter.RegionConverter;
import project.masil.community.dto.request.EventPostRequest;
import project.masil.community.dto.response.EventPostResponse;
import project.masil.community.entity.EventPost;
import project.masil.community.entity.Region;
import project.masil.community.enums.EventSort;
import project.masil.community.enums.EventType;
import project.masil.community.enums.PostType;
import project.masil.community.event.EventCreatedEvent;
import project.masil.community.exception.EventErrorCode;
import project.masil.community.exception.PostErrorCode;
import project.masil.community.exception.RegionErrorCode;
import project.masil.community.repository.EventPostRepository;
import project.masil.community.repository.FavoriteRepository;
import project.masil.community.repository.RegionRepository;
import project.masil.embedding.service.EmbeddingPipelineService;
import project.masil.embedding.service.FeedbackService;
import project.masil.global.config.S3.AmazonS3Manager;
import project.masil.global.config.S3.Uuid;
import project.masil.global.config.S3.UuidRepository;
import project.masil.global.exception.CustomException;
import project.masil.infrastructure.client.ai.AiClient;
import project.masil.user.entity.User;
import project.masil.user.entity.UserActionType;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPostService {

  private final ApplicationEventPublisher publisher;

  private final EventPostRepository eventPostRepository;
  private final RegionRepository regionRepository;
  private final UserRepository userRepository;
  private final EmbeddingPipelineService embeddingPipelineService;


  private final EventPostConverter converter;

  //좋아요 여부


  //s3
  private final UuidRepository uuidRepository;
  private final AmazonS3Manager s3Manager;
  private final FavoriteRepository favoriteRepository;

  private final AiClient aiClient;

  private final FeedbackService feedbackService;

  public static final int LEN_THRESHOLD = 50;

  public static int effectiveLen(String s) {
    if (s == null) {
      return 0;
    }
    return s.replaceAll("\\s+", "").length(); // 공백/개행 제거 후 길이
  }

  /**
   * 이벤트 작성자 userId를 반환 - 채팅 서비스에서 "이벤트 컨텍스트로 채팅 시작" 시 대상 사용자 검증 용도 - 존재하지 않으면 예외
   * (PostErrorCode.POST_NOT_FOUND)
   *
   * @param eventId
   * @return
   */
  @Transactional(readOnly = true)
  public Long getEventAuthorId(Long eventId) {
    EventPost eventPost = eventPostRepository.findById(eventId)
        .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));
    return eventPost.getUser().getId();
  }

  /**
   * 이벤트 생성 - 이미지 필수 검증 - 지역/유저 유효성 검증 - 이미지 업로드 (S3) - AI 요약 생성 (실패 시 무시)
   *
   * @param userId
   * @param request
   * @param images
   * @return
   */
  @Transactional
  public EventPostResponse createEvent(Long userId, EventPostRequest request,
      List<MultipartFile> images) {

    // 이미지 필수 검증
    if (images == null || images.isEmpty()) {
      throw new CustomException(EventErrorCode.IMAGE_REQUIRED);
    }

    // 비어 있는 파일 방지(프론트에서 name만 있고 실제 파일이 비어있는 경우)
    if (images.stream().anyMatch(f -> f == null || f.isEmpty())) {
      throw new CustomException(EventErrorCode.EMPTY_IMAGE);
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 종료 >= 시작 검증
    if (request.getEndAt().isBefore(request.getStartAt())) {
      throw new IllegalArgumentException("종료 일시는 시작 일시보다 같거나 이후여야 합니다.");
    }

    //지역 검증
    Region region = regionRepository.findById(request.getRegionId())
        .orElseThrow(() -> new CustomException(RegionErrorCode.REGION_NOT_FOUND));

    // 이미지 업로드 (실패 시 예외 전파 -> 트랜잭션 롤백)
    List<String> imageUrls = images.stream()
        .map(image -> {
          String uuid = UUID.randomUUID().toString();
          Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
          return s3Manager.uploadFile(s3Manager.generateEvent(savedUuid), image);
        })
        .collect(Collectors.toList());

    EventPost eventPost = EventPost.builder()
        .postType(PostType.EVENT)
        .user(user)
        .region(region)
        .eventImages(imageUrls)
        .eventType(request.getEventType())
        .location(request.getLocation())
        .title(request.getTitle())
        .content(request.getContent())
        .startAt(request.getStartAt())
        .endAt(request.getEndAt())
        .summary(null)
        .viewCount(0) //생성할때 0
        .favoriteCount(0)
        .commentCount(0)
        .createdAt(LocalDateTime.now())
        .build();

    EventPost savedEventPost = eventPostRepository.save(eventPost);

    int bodyLen = effectiveLen(savedEventPost.getContent());

    // (비동기) AI 요약 생성 및 파이썬 서버 임베딩 upsert
    if (bodyLen >= LEN_THRESHOLD) {
      publisher.publishEvent(new EventCreatedEvent(
          savedEventPost.getId(),
          region.getId(),
          savedEventPost.getTitle(),
          savedEventPost.getContent()
      ));
    } else {
      log.info("[AI] (create) skip postId={} (bodyLen={} < {})",
          savedEventPost.getId(), bodyLen, LEN_THRESHOLD);
    }

    return converter.toResponse(savedEventPost, false,
        userId.equals(savedEventPost.getUser().getId()), RegionConverter.toRegionResponse(region));


  }

  /**
   * 이벤트 단일 조회 로직
   *
   * @param eventPostId
   * @param userId
   * @return
   */
  public EventPostResponse getEventPost(Long eventPostId, Long userId) {
    //이벤트가 없으면 예외처리
    EventPost eventPost = eventPostRepository.findById(eventPostId)
        .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));

    boolean isLiked = false;
    if (userId != null) {
      isLiked = favoriteRepository.existsByUserIdAndPostId(userId, eventPost.getId());
    }
    // 사용자 행동 피드백 반영 (비로그인 보호)
    if (userId != null) {
      feedbackService.handle(userId, eventPostId, UserActionType.VIEW);
    }

    return converter.toResponse(eventPost, isLiked, userId.equals(eventPost.getUser().getId()),
        RegionConverter.toRegionResponse(eventPost.getRegion()));
  }

  public String getEventSummary(Long eventPostId) {
    EventPost eventPost = eventPostRepository.findById(eventPostId)
        .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));
    return eventPost.getSummary();
  }

  /**
   * [지역ID + 전체 리스트 조회] - 지역ID는 필수 - isUp=true는 상단 랜덤 노출(시드 기반 → 한 시간 동안 고정) - 나머지는 최신순 - 로그인 사용자
   * 좋아요 여부 포함
   *
   * @param pageable
   * @param userId
   * @return
   */
  @Transactional(readOnly = true)
  public Page<EventPostResponse> getEventAll(Long regionId, Pageable pageable, Long userId,
      EventSort sort) {
    // 시드 랜덤 정렬 쿼리 적용
    long seed = hourlySeed();
    if (sort == null) {
      sort = EventSort.DATE;
    }

    Page<EventPost> page = switch (sort) {
      case COMMENTS ->
          eventPostRepository.findSeededUpFirstOrderByComments(regionId, seed, pageable);
      case POPULARITY ->
          eventPostRepository.findSeededUpFirstOrderByPopularity(regionId, seed, pageable);
      case DATE -> eventPostRepository.findSeededUpFirst(regionId, seed, pageable);
    };

    return mapToResponse(page, userId);
  }

  /**
   * [지역ID + 이벤트 타입 리스트 조회] - 정렬 정책은 전체 리스트와 동일
   *
   * @param regionId
   * @param eventType
   * @param pageable
   * @return
   */
  @Transactional(readOnly = true)
  public Page<EventPostResponse> getEventTypeList(Long regionId, EventType eventType,
      Pageable pageable, Long userId, EventSort sort) {

    // 시드 랜덤 정렬 쿼리 적용
    long seed = hourlySeed();
    if (sort == null) {
      sort = EventSort.DATE;
    }

    Page<EventPost> page = switch (sort) {
      case COMMENTS -> eventPostRepository
          .findSeededUpFirstByTypeOrderByComments(regionId, eventType, seed, pageable);
      case POPULARITY -> eventPostRepository
          .findSeededUpFirstByTypeOrderByPopularity(regionId, eventType, seed, pageable);
      case DATE -> eventPostRepository
          .findSeededUpFirstByType(regionId, eventType, seed, pageable);
    };

    return mapToResponse(page, userId);

  }


  /**
   * 이벤트 수정
   *
   * @param eventPostId
   * @param userId
   * @param request
   * @param images
   * @return
   */
  @Transactional
  public EventPostResponse updateEvent(Long eventPostId, Long userId, EventPostRequest request,
      List<MultipartFile> images) {
    EventPost eventPost = eventPostRepository.findById(eventPostId)
        .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));

    // 작성자 확인
    if (!eventPost.getUser().getId().equals(userId)) {
      throw new CustomException(EventErrorCode.EVENT_FORBIDDEN); //403 에러
    }

    // 종료 >= 시작 검증
    if (request.getEndAt().isBefore(request.getStartAt())) {
      throw new IllegalArgumentException("종료 일시는 시작 일시보다 같거나 이후여야 합니다.");
    }

    //지역 검증
    Region region = regionRepository.findById(request.getRegionId())
        .orElseThrow(() -> new CustomException(RegionErrorCode.REGION_NOT_FOUND));

    // 변경 감지용 기존 값 백업
    String oldTitle = eventPost.getTitle();
    String oldContent = eventPost.getContent();
    Long oldRegionId = eventPost.getRegion().getId();

    // 업데이트
    eventPost.updateEventPost(
        region,
        request.getEventType(),
        request.getTitle(),
        request.getContent(),
        request.getLocation(),
        request.getStartAt(),
        request.getEndAt()
    );

    // 이미지: null/빈 리스트면 유지, 있으면 추가만
    if (images != null && !images.isEmpty()) {
      List<String> newUrls = images.stream()
          .map(image -> {
            String uuid = UUID.randomUUID().toString();
            Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
            return s3Manager.uploadFile(s3Manager.generateEvent(savedUuid), image);
          })
          .toList();
      eventPost.addImages(newUrls);
    }

    // 임베딩 개싱 필요 여부 판단
    boolean changed = !Objects.equals(oldTitle, eventPost.getTitle())
        || !Objects.equals(oldContent, eventPost.getContent())
        || !Objects.equals(oldRegionId, region.getId());

    int bodyLen = effectiveLen(eventPost.getContent());

    long postId = eventPost.getId();
    long regionId = region.getId();
    String title = eventPost.getTitle();
    String content = eventPost.getContent();

    // 커밋 후 단일 이벤트 발행(요약+임베딩은 리스너에서 처리)
    if (changed) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          publisher.publishEvent(
              new EventCreatedEvent(postId, regionId, title, content));
          log.info("[AI] (update) published EventPostChangedEvent postId={} changed={}", postId,
              true);
        }
      });
    }

    boolean isLiked = favoriteRepository.existsByUserIdAndPostId(userId, eventPost.getId());
    return converter.toResponse(eventPost, isLiked, true,
        RegionConverter.toRegionResponse(region));
  }

  @Transactional
  public Boolean deleteEvent(Long eventPostId) {
    EventPost eventPost = eventPostRepository.findById(eventPostId)
        .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));

    // 1) 우선 도메인 삭제 (DB 트랜잭션 안)
    eventPostRepository.delete(eventPost);

    // 2) 커밋 이후에 외부(FAISS) 반영
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        try {
          embeddingPipelineService.removePost(eventPostId);
        } catch (Exception e) {
          // 외부 연동 실패
          log.error("FAISS remove 연동 실패 postId={}", eventPostId, e);
        }
      }
    });

    return true;
  }

  /**
   * 이벤트 UP 시작 기능 - 입력한 기간 만큼 이벤트 UP 활성화
   *
   * @param eventId
   * @param userId
   * @param days
   * @return
   */
  @Transactional
  public EventPostResponse startUp(Long eventId, Long userId, int days) {
    EventPost post = eventPostRepository.findById(eventId)
        .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));

    /**
     * 만료되어 있거나 현재 OFF면 새로 시작, 진행 중이면 기간 연장 정책 택1
     */
    post.refreshUpStatusByNow();
    if (post.isUp()) {
      // 진행 중 → 연장 정책 (원하면 아래 주석 해제)
//      post.setUpExpiresAt(post.getUpExpiresAt().plusDays(days));
    } else {
      post.startUpForDays(days);
    }

    return converter.toResponse(
        post,
        false,
        userId != null && userId.equals(post.getUser().getId()),
        RegionConverter.toRegionResponse(post.getRegion())
    );


  }

  /**
   * 이벤트 UP 중지 기능
   *
   * @param eventId
   * @param userId
   * @return
   */
  @Transactional
  public EventPostResponse stopUp(Long eventId, Long userId) {
    EventPost post = eventPostRepository.findById(eventId)
        .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));
    post.stopUp();
    return converter.toResponse(
        post,
        false,
        userId != null && userId.equals(post.getUser().getId()),
        RegionConverter.toRegionResponse(post.getRegion())
    );
  }

  /**
   * 이벤트 UP 상태 조회
   *
   * @param eventId
   * @param userId
   * @return
   */
  @Transactional(readOnly = true)
  public EventPostResponse getEventStatus(Long eventId, Long userId) {
    EventPost post = eventPostRepository.findById(eventId)
        .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));
    post.refreshUpStatusByNow(); // 조회시 만료된 경우 자동 해제

    return converter.toResponse(
        post,
        false,
        userId != null && userId.equals(post.getUser().getId()),
        RegionConverter.toRegionResponse(post.getRegion()));

  }


  // 시간 단위(yyyyMMddHH)로 시드 고정 → 한 시간 동안은 UP끼리의 순서가 유지됨
  private long hourlySeed() {
    String time = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
    return Long.parseLong(time);
  }

  /**
   * 좋아요 / 작성자 여부까지 한 번에 매핑
   */
  private Page<EventPostResponse> mapToResponse(Page<EventPost> page, Long userId) {

    // 비로그인: 전부 false (NPE 절대 발생 X)
    if (userId == null) {
      return page.map(post -> converter.toResponse(
          post,
          /* isLiked */ false,
          /* isAuthor */ false,
          RegionConverter.toRegionResponse(post.getRegion())
      ));
    }

    /**
     * 로그인: 현재 페이지 게시글 ID 수집 → 좋아요 세트 조회
     */
    // (1) 현재 조회된 페이지(Page<EventPost>) 안에 있는 게시글들을 꺼내서,
    //     각각의 게시글 ID만 뽑아서 List<Long> 형태로 모은다.
    //     예: [101, 102, 103, 104]
    List<Long> postIds = page.getContent()
        .stream()
        .map(EventPost::getId)
        .toList();

    // (2) 내가 로그인한 사용자(userId)가 "좋아요"를 누른 게시글들 중에서,
    //     방금 뽑은 postIds(= 이번 페이지 게시글들)에 해당하는 것만 한 번에 조회한다.
    //     결과는 Set<Long> 형태(중복 없음).
    //     예: {102, 104} → 이 페이지에서 내가 좋아요 누른 건 102번, 104번 게시글
    Set<Long> likedIds = favoriteRepository.findLikedPostIds(userId, postIds);

    return page.map(post -> converter.toResponse(
        post,
        likedIds.contains(post.getId()),
        userId.equals(post.getUser().getId()), //userId null 아님
        RegionConverter.toRegionResponse(post.getRegion())
    ));


  }


}