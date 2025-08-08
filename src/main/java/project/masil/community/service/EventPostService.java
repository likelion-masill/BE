package project.masil.community.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.masil.community.converter.EventPostConverter;
import project.masil.community.dto.request.EventPostRequest;
import project.masil.community.dto.response.EventPostResponse;
import project.masil.community.entity.EventPost;
import project.masil.community.entity.Region;
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

  public EventPostResponse createEvent(Long userId, EventPostRequest request, List<MultipartFile> images) {

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
    LocalDateTime startAtKst = request.getStartAt()
        .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
        .toLocalDateTime();
    LocalDateTime endAtKst = request.getEndAt()
        .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
        .toLocalDateTime();


    if (request.getEndAt().isBefore(request.getStartAt())) {
      throw new IllegalArgumentException("종료 일시는 시작 일시보다 같거나 이후여야 합니다.");
    }

    Region region = regionRepository.findById(request.getRegionId())
        .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다."));



    EventPost eventPost = EventPost.builder()
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
        .build();

    EventPost savedEventPost = eventPostRepository.save(eventPost);

    return converter.toResponse(savedEventPost);


  }

}
