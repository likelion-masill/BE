package project.masil.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.masil.community.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {


}
