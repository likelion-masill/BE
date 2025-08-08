package project.masil.community.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import project.masil.community.entity.ClubPost;

public interface ClubPostRepository extends JpaRepository<ClubPost, Long> {

}
