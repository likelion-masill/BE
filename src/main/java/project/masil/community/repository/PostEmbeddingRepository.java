package project.masil.community.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.masil.community.entity.PostEmbedding;
import project.masil.community.enums.EventType;

@Repository
public interface PostEmbeddingRepository extends JpaRepository<PostEmbedding, Long> {

  @Query("""
      SELECT e.id
      FROM EventPost e
      WHERE e.region.id = :regionId
      AND e.eventType = :eventType
      """)
  List<Long> findPostIdsByRegionIdAndEventType(@Param("regionId") Long regionId,
      @Param("eventType") EventType eventType);

  @Query("SELECT p.postId FROM PostEmbedding p")
  List<Long> findAllIds();
}
