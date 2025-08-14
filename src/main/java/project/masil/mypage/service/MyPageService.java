package project.masil.mypage.service;

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
import project.masil.mypage.converter.MyPageConverter;
import project.masil.mypage.dto.response.PostResponse;
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

}
