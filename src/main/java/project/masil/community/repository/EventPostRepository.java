package project.masil.community.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.masil.community.entity.EventPost;
import project.masil.community.enums.EventType;

@Repository
public interface EventPostRepository extends JpaRepository<EventPost, Long>,
    JpaSpecificationExecutor<EventPost> {

  /**
   * 페이징 처리 + N+1문제 해결하기 위해 @EntityGraph 추가 RegionId로 이벤트 게시글 전체 조회
   *
   * @param pageable
   * @return
   */
  @EntityGraph(attributePaths = {"user", "eventImages"})
  Page<EventPost> findAllByRegionIdOrderByCreatedAtDesc(Long regionId, Pageable pageable);

  /**
   * RegionId와 이벤트 타입으로 이벤트 게시글 리스트 조회
   *
   * @param regionId
   * @param eventType
   * @param pageable
   * @return
   */
  Page<EventPost> findByRegionIdAndEventType(Long regionId, EventType eventType, Pageable pageable);

  @Query(value = """
      SELECT *
      FROM event_post e
      WHERE e.id IN (:ids)
      ORDER BY e.created_at DESC, e.id DESC
      LIMIT :limit OFFSET :offset
      """, nativeQuery = true)
  List<EventPost> findRecentByIdsPage(@Param("ids") List<Long> ids,
      @Param("offset") int offset,
      @Param("limit") int limit);


  @Query("SELECT e.id FROM EventPost e")
  List<Long> findAllIds();
}