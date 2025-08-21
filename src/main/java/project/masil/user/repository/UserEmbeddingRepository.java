package project.masil.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.masil.user.entity.UserEmbedding;

public interface UserEmbeddingRepository extends JpaRepository<UserEmbedding, Long> {
    
}
