package project.masil.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.community.converter.ClubPostConverter;
import project.masil.community.dto.request.ClubPostRequest;
import project.masil.community.dto.response.ClubPostDetailResponse;
import project.masil.community.dto.response.ClubPostSummaryResponse;
import project.masil.community.entity.ClubPost;
import project.masil.community.entity.EventPost;
import project.masil.community.enums.PostType;
import project.masil.community.exception.ClubPostErrorCode;
import project.masil.community.exception.EventErrorCode;
import project.masil.community.repository.ClubPostRepository;
import project.masil.community.repository.EventPostRepository;
import project.masil.global.exception.CustomException;
import project.masil.user.entity.User;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubPostService {

  private final ClubPostRepository clubPostRepository;
  private final EventPostRepository eventPostRepository;
  private final UserRepository userRepository;

  /**
   * 소모임 게시글을 생성합니다.
   *
   * @param userId        작성자 ID
   * @param eventId       이벤트 ID
   * @param createRequest 소모임 게시글 생성 요청
   * @return 생성된 소모임 게시글 상세 정보
   */
  public ClubPostDetailResponse createClubPost(Long userId, Long eventId,
      ClubPostRequest createRequest) {
    log.info("[서비스] 소모임 게시글 생성 시도 - userId: {}, eventId: {}, createRequest: {}", userId, eventId,
        createRequest);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    EventPost eventPost = eventPostRepository.findById(eventId)
        .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));

    ClubPost clubPost = ClubPost.builder()
        .postType(PostType.CLUB)
        .user(user)
        .eventPost(eventPost)
        .title(createRequest.getTitle())
        .location(createRequest.getLocation())
        .startAt(createRequest.getStartAt())
        .content(createRequest.getContent())
        .build();

    ClubPost saved = clubPostRepository.save(clubPost);
    return ClubPostConverter.toClubPostDetailResponse(saved);
  }

  /**
   * 소모임 게시글의 상세 정보를 조회합니다.
   *
   * @param clubId 소모임 게시글 ID
   * @return 소모임 게시글 상세 정보
   */
  public ClubPostDetailResponse getClubPostDetail(Long clubId) {
    log.info("[서비스] 소모임 게시글 상세 조회 시도 - clubId: {}", clubId);
    ClubPost clubPost = clubPostRepository.findById(clubId)
        .orElseThrow(() -> new CustomException(ClubPostErrorCode.CLUB_POST_NOT_FOUND));
    return ClubPostConverter.toClubPostDetailResponse(clubPost);
  }

  /**
   * 소모임 게시글을 수정합니다.
   *
   * @param clubId        소모임 게시글 ID
   * @param updateRequest 소모임 게시글 수정 요청
   * @return 수정된 소모임 게시글 상세 정보
   */
  @Transactional
  public ClubPostDetailResponse updateClubPost(Long userId, Long clubId,
      ClubPostRequest updateRequest) {
    log.info("[서비스] 소모임 게시글 수정 시도 - clubId: {}, updateRequest: {}", clubId, updateRequest);
    ClubPost clubPost = clubPostRepository.findById(clubId)
        .orElseThrow(() -> new CustomException(ClubPostErrorCode.CLUB_POST_NOT_FOUND));
    if (!clubPost.getUser().getId().equals(userId)) {
      throw new CustomException(ClubPostErrorCode.CLUB_POST_FORBIDDEN);
    }
    clubPost.update(updateRequest);

    return ClubPostConverter.toClubPostDetailResponse(clubPost);
  }

  /**
   * 지정된 소모임 게시글을 삭제한다.
   * <p>
   * 게시글이 존재하지 않거나, 요청 사용자가 게시글 작성자가 아닌 경우 예외를 발생시킨다.
   * </p>
   *
   * @param userId 삭제를 요청한 사용자의 ID
   * @param clubId 삭제할 소모임 게시글의 ID
   * @throws CustomException {@link ClubPostErrorCode#CLUB_POST_NOT_FOUND} - 게시글이 존재하지 않는 경우
   * @throws CustomException {@link ClubPostErrorCode#CLUB_POST_FORBIDDEN} - 요청 사용자가 작성자가 아닌 경우
   */
  public void deleteClubPost(Long userId, Long clubId) {
    log.info("[서비스] 소모임 게시글 삭제 시도 - userId: {}, clubId: {}", userId, clubId);
    ClubPost clubPost = clubPostRepository.findById(clubId)
        .orElseThrow(() -> new CustomException(ClubPostErrorCode.CLUB_POST_NOT_FOUND));
    if (!clubPost.getUser().getId().equals(userId)) {
      throw new CustomException(ClubPostErrorCode.CLUB_POST_FORBIDDEN);
    }
    clubPostRepository.delete(clubPost);
  }

  @Transactional(readOnly = true)
  public Page<ClubPostSummaryResponse> getClubPostListByEventId(Long eventId, Pageable pageable) {
    EventPost eventPost = eventPostRepository.findById(eventId)
        .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));
    Page<ClubPost> clubPosts = clubPostRepository.findByEventPostOrderByCreatedAtDesc(eventPost,
        pageable);

    String coverImage = clubPosts.getContent().getFirst().getEventPost().getEventImages()
        .getFirst();
    return clubPosts.map(
        clubPost -> ClubPostConverter.toClubPostSummaryResponse(clubPost, coverImage));
  }

}
