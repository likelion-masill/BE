package project.masil.mypage.service;

import static project.masil.mypage.converter.MyPageConverter.toPostResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.community.entity.Post;
import project.masil.community.repository.FavoriteRepository;
import project.masil.community.repository.PostRepository;
import project.masil.global.exception.CustomException;
import project.masil.infrastructure.client.opendata.OpenDataClient;
import project.masil.infrastructure.client.opendata.dto.BusinessInfoPayload;
import project.masil.infrastructure.client.opendata.dto.OwnerVerifyApiResponse;
import project.masil.mypage.dto.request.OwnerVerifyRequest;
import project.masil.mypage.dto.response.PostResponse;
import project.masil.mypage.exception.MyPageErrorCode;
import project.masil.user.entity.User;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageService {

  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final FavoriteRepository favoriteRepository;
  private final OpenDataClient openDataClient;


  /**
   * 사용자 ID로 작성한 게시글 목록을 조회합니다.
   *
   * @param userId   사용자 ID
   * @param pageable 페이징 정보
   * @return 작성한 게시글 목록
   */
  @Transactional(readOnly = true)
  public Page<PostResponse> getMyPostList(Long userId, Pageable pageable) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    Page<Post> posts = postRepository.findByUserOrderByCreatedAtDesc(user, pageable);

    List<Long> postIds = posts.getContent().stream().map(Post::getId)
        .toList();

    Set<Long> likedIds = postIds.isEmpty()
        ? Set.of()
        : new HashSet<>(favoriteRepository.findLikedPostIds(userId, postIds));

    return posts.map(post -> {
      boolean isBusinessVerified = post.getUser().isBusinessVerified();
      boolean isLiked = likedIds.contains(post.getId());
      return toPostResponse(post, isBusinessVerified, isLiked);
    });
  }

  /**
   * 사용자 ID로 즐겨찾기한 게시글 목록을 조회합니다.
   *
   * @param userId   사용자 ID
   * @param pageable 페이징 정보
   * @return 즐겨찾기한 게시글 목록
   */
  @Transactional(readOnly = true)
  public Page<PostResponse> getMyFavoritePostList(Long userId, Pageable pageable) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    Page<Post> posts = favoriteRepository.findFavoritePostsAsPage(user, pageable);
    return posts.map(post -> {
      boolean isBusinessVerified = post.getUser().isBusinessVerified();
      boolean isLiked = true;
      return toPostResponse(post, isBusinessVerified, isLiked);
    });
  }

  /**
   * 사업자 인증을 수행합니다.
   *
   * @param userId        사용자 ID
   * @param verifyRequest 사업자 인증 요청 정보
   * @throws CustomException 인증 실패 시 예외 발생
   */
  @Transactional
  public void verifyOwner(Long userId, OwnerVerifyRequest verifyRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    BusinessInfoPayload payload = new BusinessInfoPayload(
        verifyRequest.getBusinessNumber(),
        verifyRequest.getOpeningDate(),  // 예: "20250112" (YYYYMMDD)
        verifyRequest.getBusinessName()
    );

    OwnerVerifyApiResponse response = openDataClient.verifyBusiness(payload);

    if (response != null
        && "OK".equals(response.getStatusCode())
        && response.getData() != null
        && !response.getData().isEmpty()) {

      // data[0]에서 valid 코드 확인
      Map<String, Object> first = response.getData().get(0);

      // valid가 문자열이 아닐 수 있으니 안전 변환
      Object validObj = first.get("valid");
      String valid = (validObj != null) ? String.valueOf(validObj) : null;

      log.info("사업자 인증 응답 valid: {}", valid);
      if ("01".equals(valid)) {
        user.verifyBusiness(verifyRequest.getBusinessNumber());
        return;
      }
      throw new CustomException(MyPageErrorCode.OWNER_VERIFICATION_FAILED);
    }

    // 여기로 오면 외부 API 실패 혹은 비정상 응답
    log.error("사업자 인증 API 실패 또는 비정상 응답: {}", response);
    throw new CustomException(MyPageErrorCode.OWNER_VERIFICATION_API_ERROR);
  }


}