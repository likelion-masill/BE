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
      select pe.postId
      from PostEmbedding pe
      where pe.regionId = :regionId
      """)
  List<Long> findPostIdsByRegionId(@Param("regionId") Long regionId);

  @Query("SELECT p.postId FROM PostEmbedding p")
  List<Long> findAllIds();
}
