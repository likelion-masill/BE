package project.masil.community.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.masil.community.entity.PostEmbedding;

@Repository
public interface PostEmbeddingRepository extends JpaRepository<PostEmbedding, Long> {

  @Query("""
      SELECT pe.postId
      FROM PostEmbedding pe
      JOIN Post p ON pe.postId = p.id
      JOIN EventPost e ON e.id = p.id
      WHERE e.region.id = :regionId
      """)
  List<Long> findPostIdsByRegionId(@Param("regionId") Long regionId);

}
