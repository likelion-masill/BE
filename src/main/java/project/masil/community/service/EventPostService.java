package project.masil.community.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.masil.community.converter.EventPostConverter;
import project.masil.community.dto.request.EventPostRequest;
import project.masil.community.dto.response.EventPostResponse;
import project.masil.community.entity.EventPost;
import project.masil.community.entity.Region;
import project.masil.community.enums.PostType;
import project.masil.community.exception.EventErrorCode;
import project.masil.community.exception.RegionErrorCode;
import project.masil.community.repository.EventPostRepository;
import project.masil.community.repository.RegionRepository;
import project.masil.global.config.S3.AmazonS3Manager;
import project.masil.global.config.S3.Uuid;
import project.masil.global.config.S3.UuidRepository;
import project.masil.global.exception.CustomException;
import project.masil.user.entity.User;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class EventPostService {

  private final EventPostRepository eventPostRepository;
  private final RegionRepository regionRepository;
  private final UserRepository userRepository;

  private final EventPostConverter converter;

  //s3
  private final UuidRepository uuidRepository;
  private final AmazonS3Manager s3Manager;

  /**
   * 이벤트 생성 로직
   *
   * @param userId
   * @param request
   * @param images
   * @return
   */
  @Transactional
  public EventPostResponse createEvent(Long userId, EventPostRequest request,
      List<MultipartFile> images) {

    // images가 null인 경우 빈 리스트로 초기화
    List<String> imageUrls = (images != null) ? images.stream()
        .map(image -> {
          String uuid = UUID.randomUUID().toString();
          Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
          return s3Manager.uploadFile(s3Manager.generateEvent(savedUuid), image);
        })
        .collect(Collectors.toList()) : new ArrayList<>();

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 1) 날짜 변환 (KST 기준 LocalDateTime)
    LocalDateTime startAtKst = request.getStartAt();
    LocalDateTime endAtKst = request.getEndAt();

    // 종료 >= 시작 검증
    if (request.getEndAt().isBefore(request.getStartAt())) {
      throw new IllegalArgumentException("종료 일시는 시작 일시보다 같거나 이후여야 합니다.");
    }

    //지역 검증
    Region region = regionRepository.findById(request.getRegionId())
        .orElseThrow(() -> new CustomException(RegionErrorCode.REGION_NOT_FOUND));

    EventPost eventPost = EventPost.builder()
        .postType(PostType.EVENT)
        .user(user)
        .region(region)
        .eventType(request.getEventType())
        .location(request.getLocation())
        .title(request.getTitle())
        .content(request.getContent())
        .startAt(startAtKst)
        .endAt(endAtKst)
        .eventImages(imageUrls)
        .summary(null)
        .viewCount(0) //생성할때 0
        .favoriteCount(0)
        .commentCount(0)
        .createdAt(LocalDateTime.now())
        .build();

    EventPost savedEventPost = eventPostRepository.save(eventPost);

    return converter.toResponse(savedEventPost);


  }

  /**
   * \ 이벤트 단일 조회 로직
   *
   * @param eventPostId
   * @return
   */
  public EventPostResponse getEventPost(Long eventPostId) {
    //이벤트가 없으면 예외처리
    EventPost eventPost = eventPostRepository.findById(eventPostId)
        .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));

    return converter.toResponse(eventPost);
  }

  /**
   * 이벤트 리스트 조회
   *
   * @param pageable
   * @return
   */
  @Transactional(readOnly = true)
  public Page<EventPostResponse> getEventList(Long eventId, Pageable pageable) {
    Page<EventPost> page = eventPostRepository.findAllByRegionIdOrderByCreatedAtDesc(eventId,
        pageable);
    return page.map(converter::toResponse);
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

    // @Transactional이 있어서 변경감지로 저장되므로 save() 불필요. 항상 마지막에 반환
    return converter.toResponse(eventPost);
  }

  public Boolean deleteEvent(Long eventPostId) {
    EventPost eventPost = eventPostRepository.findById(eventPostId)
        .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));
    eventPostRepository.delete(eventPost);
    return true;
  }

}
