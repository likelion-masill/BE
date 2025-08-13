package project.masil.community.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import project.masil.community.entity.Comment;
import project.masil.community.entity.Post;
import project.masil.user.entity.User;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  @EntityGraph(attributePaths = {"user", "post"})
  List<Comment> findByPostAndParentCommentIsNull(Post post);

  @EntityGraph(attributePaths = {"user", "post"})
  List<Comment> findByPostAndParentComment(Post post, Comment parentComment);

  List<Comment> findByUser(User user);

}
