package project.masil.community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.masil.community.entity.Post;
import project.masil.user.entity.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  @EntityGraph(attributePaths = {"user"})
  Page<Post> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

}
