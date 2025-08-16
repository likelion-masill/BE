package project.masil.community.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.masil.community.entity.Favorite;
import project.masil.community.entity.Post;
import project.masil.user.entity.User;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  Optional<Favorite> findByUserAndPost(User user, Post post);

  @EntityGraph(attributePaths = {"post", "post.user"})
  Page<Favorite> findByUserOrderByPostCreatedAtDesc(User user, Pageable pageable);

  default Page<Post> findFavoritePostsAsPage(User user, Pageable pageable) {
    return findByUserOrderByPostCreatedAtDesc(user, pageable)
        .map(Favorite::getPost);
  }

  /**
   * 좋아요 여부 판단
   */
  // 단건: 특정 사용자/게시글 조합이 관심목록에 존재하는지
  boolean existsByUserIdAndPostId(Long userId, Long postId);

  // 배치: 한 번에 여러 게시글에 대한 '좋아요(관심목록) 여부'를 가져오기 (N+1 회피)
  // => Favorite 테이블에서 "userId가 좋아요한 postId들"만 한 번에 IN 쿼리로 싹 가져옴 (N+1을 막음)
  @Query("select f.post.id from Favorite f where f.user.id = :userId and f.post.id in :postIds")
  Set<Long> findLikedPostIds(@Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);
}
