package project.masil.community.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.community.dto.response.FavoriteResponse;
import project.masil.community.entity.Favorite;
import project.masil.community.entity.Post;
import project.masil.community.enums.PostType;
import project.masil.community.exception.PostErrorCode;
import project.masil.community.repository.FavoriteRepository;
import project.masil.community.repository.PostRepository;
import project.masil.embedding.service.FeedbackService;
import project.masil.global.exception.CustomException;
import project.masil.user.entity.User;
import project.masil.user.entity.UserActionType;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class FavoriteService {

  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final FavoriteRepository favoriteRepository;
  private final FeedbackService feedbackService;


  /**
   * 게시글의 관심목록 상태를 토글합니다.
   *
   * @param userId       사용자 ID
   * @param postId       게시글 ID
   * @param expectedType 게시글 타입 (EVENT 또는 CLUB)
   * @return 관심목록 응답 객체
   */
  @Transactional
  public FavoriteResponse toggleFavorite(Long userId, Long postId, PostType expectedType) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

    // 게시글의 타입이 예상 타입과 일치하는지 확인
    if (!post.getPostType().equals(expectedType)) {
      throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
    }

    Optional<Favorite> existing = favoriteRepository.findByUserAndPost(user, post);

    // 즐겨찾기가 이미 존재하는 경우 삭제하고, 그렇지 않으면 새로 추가
    if (existing.isPresent()) {
      favoriteRepository.delete(existing.get());
      if (expectedType == PostType.EVENT) {
        feedbackService.handle(userId, postId, UserActionType.FAVORITE_REMOVE);
      }

      post.decrementFavoriteCount();
    } else {
      Favorite favorite = Favorite.builder()
          .user(user)
          .post(post)
          .build();
      favoriteRepository.save(favorite);
      if (expectedType == PostType.EVENT) {
        feedbackService.handle(userId, postId, UserActionType.FAVORITE_ADD);
      }

      post.incrementFavoriteCount();
    }
    return FavoriteResponse.builder()
        .isFavorite(existing.isEmpty())
        .favoriteCount(post.getFavoriteCount())
        .build();

  }

}
