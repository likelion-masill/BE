package project.masil.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.masil.community.entity.PostEmbedding;

@Repository
public interface PostEmbeddingRepository extends JpaRepository<PostEmbedding, Long> {

}
