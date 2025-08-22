package project.masil.community.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.masil.community.converter.EventPostConverter;
import project.masil.community.converter.RegionConverter;
import project.masil.community.dto.request.EventPostRequest;
import project.masil.community.dto.response.EventPostResponse;
import project.masil.community.entity.EventPost;
import project.masil.community.entity.Region;
import project.masil.community.enums.EventType;
import project.masil.community.enums.PostType;
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
import project.masil.infrastructure.client.ai.dto.AiSummarizeRequest;
import project.masil.infrastructure.client.ai.dto.AiSummarizeResponse;
import project.masil.user.entity.User;
import project.masil.user.entity.UserActionType;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPostService {


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
   * 이벤트 생성
   * - 이미지 필수 검증
   * - 지역/유저 유효성 검증
   * - 이미지 업로드 (S3)
   * - AI 요약 생성 (실패 시 무시)
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

    String summary = null;
    try {
      AiSummarizeRequest aiReq =
          new AiSummarizeRequest(
              request.getContent(),   // 원문
              5,                      // top_k
              10,                     // min_len
              0.3,                    // temperature
              300                     // max_output_tokens
          );

      AiSummarizeResponse response = aiClient.summarize(aiReq);

      if (response != null && "success".equalsIgnoreCase(response.getStatus())
          && response.getData() != null) {
        String data = response.getData().trim();
        summary = data;
      }
    } catch (Exception e) {
      // 요약 실패는 전체 트랜잭션 실패로 볼 필요 없으면 여기서만 로깅하고 넘어가
      log.warn("요약 생성 실패", e);
    }

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
        .summary(summary)
        .viewCount(0) //생성할때 0
        .favoriteCount(0)
        .commentCount(0)
        .createdAt(LocalDateTime.now())
        .build();

    EventPost savedEventPost = eventPostRepository.save(eventPost);

    embeddingPipelineService.upsertPost(savedEventPost.getId(), region.getId(),
        savedEventPost.getTitle(),
        savedEventPost.getContent());

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

  /**
   * [지역ID + 전체 리스트 조회]
   * - 지역ID는 필수
   * - isUp=true는 상단 랜덤 노출(시드 기반 → 한 시간 동안 고정)
   * - 나머지는 최신순
   * - 로그인 사용자 좋아요 여부 포함
   *
   * @param pageable
   * @param userId
   * @return
   */
  @Transactional(readOnly = true)
  public Page<EventPostResponse> getEventAll(Long regionId, Pageable pageable, Long userId) {
    // 시드 랜덤 정렬 쿼리 적용
    long seed = hourlySeed();

    Page<EventPost> page = eventPostRepository.findSeededUpFirst(regionId, seed, pageable);

    return mapToResponse(page, userId);
  }

  /**
   * [지역ID + 이벤트 타입 리스트 조회]
   * - 정렬 정책은 전체 리스트와 동일
   *
   * @param regionId
   * @param eventType
   * @param pageable
   * @return
   */
  @Transactional(readOnly = true)
  public Page<EventPostResponse> getEventTypeList(Long regionId, EventType eventType,
      Pageable pageable, Long userId) {

    // 시드 랜덤 정렬 쿼리 적용
    long seed = hourlySeed();
    Page<EventPost> page = eventPostRepository.findSeededUpFirstByType(regionId,
        eventType, seed, pageable);
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

    LocalDateTime startAtKst = request.getStartAt();
    LocalDateTime endAtKst = request.getEndAt();

    //업데이트
    eventPost.updateEventPost(
        region,
        request.getEventType(),
        request.getTitle(),
        request.getContent(),
        request.getLocation(),
        startAtKst,
        endAtKst
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

    boolean isLiked = favoriteRepository.existsByUserIdAndPostId(userId, eventPost.getId());
    // @Transactional이 있어서 변경감지로 저장되므로 save() 불필요. 항상 마지막에 반환
    return converter.toResponse(eventPost, isLiked, userId.equals(eventPost.getUser().getId()),
        RegionConverter.toRegionResponse(region));
  }

  public Boolean deleteEvent(Long eventPostId) {
    EventPost eventPost = eventPostRepository.findById(eventPostId)
        .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));
    eventPostRepository.delete(eventPost);
    return true;
  }

  /**
   * 이벤트 UP 시작 기능
   * - 입력한 기간 만큼 이벤트 UP 활성화
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