package project.masil.community.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import project.masil.community.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findByPostId(Long postId);

  List<Comment> findByUserId(Long userId);
  
  void deleteByPostId(Long postId);

}
