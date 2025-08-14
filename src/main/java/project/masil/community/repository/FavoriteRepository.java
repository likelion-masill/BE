package project.masil.community.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
