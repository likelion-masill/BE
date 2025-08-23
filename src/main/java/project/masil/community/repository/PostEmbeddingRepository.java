package project.masil.community.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.masil.community.entity.PostEmbedding;
import project.masil.community.enums.EventType;

@Repository
public interface PostEmbeddingRepository extends JpaRepository<PostEmbedding, Long> {


  // 지역만 (임베딩 존재하는 게시글로 한정)
  @Query("""
      select e.id
      from EventPost e
      join PostEmbedding pe on pe.postId = e.id
      where e.region.id = :regionId
      """)
  List<Long> findPostIdsByRegionId(@Param("regionId") Long regionId);

  // 지역 + 이벤트 타입
  @Query("""
      select e.id
      from EventPost e
      join PostEmbedding pe on pe.postId = e.id
      where e.region.id = :regionId
        and e.eventType = :eventType
      """)
  List<Long> findPostIdsByRegionIdAndEventType(@Param("regionId") Long regionId,
      @Param("eventType") EventType eventType);

  // 지역 + 오늘과 겹치는 일정
  @Query("""
      select e.id
      from EventPost e
      join PostEmbedding pe on pe.postId = e.id
      where e.region.id = :regionId
        and e.startAt <= :endOfDay
        and e.endAt   >= :startOfDay
      """)
  List<Long> findPostIdsByRegionIdAndActiveOnDate(@Param("regionId") Long regionId,
      @Param("startOfDay") LocalDateTime startOfDay,
      @Param("endOfDay") LocalDateTime endOfDay);


  @Query("SELECT p.postId FROM PostEmbedding p")
  List<Long> findAllIds();

  boolean existsByPostId(Long postId);
}
