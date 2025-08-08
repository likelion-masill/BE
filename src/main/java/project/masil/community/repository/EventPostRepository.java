package project.masil.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.masil.community.entity.EventPost;

@Repository
public interface EventPostRepository extends JpaRepository<EventPost, Long> {
}
