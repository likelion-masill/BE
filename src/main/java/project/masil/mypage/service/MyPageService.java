package project.masil.mypage.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import project.masil.community.entity.Post;
import project.masil.community.repository.FavoriteRepository;
import project.masil.community.repository.PostRepository;
import project.masil.global.exception.CustomException;
import project.masil.mypage.converter.MyPageConverter;
import project.masil.mypage.dto.request.BusinessInfo;
import project.masil.mypage.dto.request.OwnerVerifyRequest;
import project.masil.mypage.dto.response.OwnerVerifyResponse;
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
  private final @Qualifier("opendataClient") WebClient openDataClient;


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
    return posts
        .map(MyPageConverter::toPostResponse);
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
    return posts
        .map(MyPageConverter::toPostResponse);
  }

  // verifyOwner
  @Transactional
  public void verifyOwner(Long userId, OwnerVerifyRequest verifyRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 요청 바디 생성
    BusinessInfo businessInfo = new BusinessInfo(
        verifyRequest.getBusinessNumber(),
        verifyRequest.getOpeningDate(),
        verifyRequest.getBusinessName()
    );
    Map<String, Object> requestBody = Map.of("businesses", List.of(businessInfo));
    // API 호출
    OwnerVerifyResponse response = openDataClient.post()
        .uri(uriBuilder -> uriBuilder
            .path("/validate")
            .queryParam("returnType", "JSON")
            .build())
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(OwnerVerifyResponse.class)
        .block();

    // 결과 검증
    if (response != null && "OK".equals(response.getStatus_code())) {
      Map<String, Object> data = response.getData().get(0);
      String valid = (String) data.get("valid");
      if ("01".equals(valid)) {
        user.verifyBusiness();
      } else {
        throw new CustomException(MyPageErrorCode.OWNER_VERIFICATION_FAILED);
      }
    } else {
      log.error("사업자 인증 API 호출 실패: {}", response);
      throw new CustomException(MyPageErrorCode.OWNER_VERIFICATION_API_ERROR);
    }

  }

}