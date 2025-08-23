package project.masil.community.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.masil.community.entity.EventPost;
import project.masil.community.enums.EventType;

@Repository
public interface EventPostRepository extends JpaRepository<EventPost, Long>,
    JpaSpecificationExecutor<EventPost> {

  /**
   * 오늘 범위와 겹치는(기간 overlap) 광고(UP) 게시글 ID 조회 - 지역 필수, 이벤트 타입은 선택(Null 허용) - isUp=true AND
   * (upExpiresAt IS NULL OR upExpiresAt > NOW) - [기간 겹침] e.startAt <= :endOfDay AND e.endAt >=
   * :startOfDay - 시드 기반 난수 정렬
   */
  @Query("""
      SELECT e.id
      FROM EventPost e
      WHERE e.region.id = :regionId
        AND (:eventType IS NULL OR e.eventType = :eventType)
        AND e.isUp = true
        AND (e.upExpiresAt IS NULL OR e.upExpiresAt > CURRENT_TIMESTAMP)
        AND e.startAt <= :endOfDay
        AND e.endAt >= :startOfDay
      ORDER BY FUNCTION('CRC32', CONCAT(CAST(e.id AS string), :seed))
      """)
  List<Long> findActiveAdPostIds(@Param("regionId") Long regionId,
      @Param("eventType") EventType eventType,   // null 허용
      @Param("startOfDay") LocalDateTime startOfDay,
      @Param("endOfDay") LocalDateTime endOfDay,
      @Param("seed") long seed);

  /**
   * (today 아님) 특정 이벤트 타입의 광고(UP) 게시글 ID 조회 - 지역 + 이벤트 타입 고정 - isUp=true AND (upExpiresAt IS NULL OR
   * upExpiresAt > NOW) - 시드 기반 난수 정렬
   */
  @Query("""
      SELECT e.id
      FROM EventPost e
      WHERE e.region.id = :regionId
        AND e.eventType = :eventType
        AND e.isUp = true
        AND (e.upExpiresAt IS NULL OR e.upExpiresAt > CURRENT_TIMESTAMP)
      ORDER BY FUNCTION('CRC32', CONCAT(CAST(e.id AS string), :seed))
      """)
  List<Long> findAdPostIdsByType(@Param("regionId") Long regionId,
      @Param("eventType") EventType eventType,
      @Param("seed") long seed);

  /**
   * (today 아님) 모든 타입 대상 광고(UP) 게시글 ID 조회 - 지역만 고정 - isUp=true AND (upExpiresAt IS NULL OR
   * upExpiresAt > NOW) - 시드 기반 난수 정렬
   */
  @Query("""
      SELECT e.id
      FROM EventPost e
      WHERE e.region.id = :regionId
        AND e.isUp = true
        AND (e.upExpiresAt IS NULL OR e.upExpiresAt > CURRENT_TIMESTAMP)
      ORDER BY FUNCTION('CRC32', CONCAT(CAST(e.id AS string), :seed))
      """)
  List<Long> findAdPostIds(@Param("regionId") Long regionId,
      @Param("seed") long seed);

  //UP 게시물 상단 노출 우선 정렬을 위해 기존 정렬 메서드는 사용 X
//  /**
//   * 페이징 처리 + N+1문제 해결하기 위해 @EntityGraph 추가
//   * RegionId로 이벤트 게시글 전체 조회
//   *
//   * @param pageable
//   * @return
//   */
//  @EntityGraph(attributePaths = {"user", "eventImages"})
//  Page<EventPost> findAllByRegionIdOrderByCreatedAtDesc(Long regionId, Pageable pageable);

  @Query(value = """
      SELECT p.id,
             MATCH(p.title, p.content) AGAINST(:q IN NATURAL LANGUAGE MODE) AS score
      FROM Post p
      JOIN events e ON e.id = p.id
      WHERE e.region_id = :regionId
        AND MATCH(p.title, p.content) AGAINST(:q IN NATURAL LANGUAGE MODE)
      ORDER BY score DESC, p.id DESC
      LIMIT :limit OFFSET :offset
      """, nativeQuery = true)
  List<Object[]> searchPostIdsByKeywordInRegion(
      @Param("q") String keyword,
      @Param("regionId") Long regionId,
      @Param("limit") int limit,
      @Param("offset") int offset
  );

  @Query(value = """
      SELECT COUNT(*)
      FROM Post p
      JOIN events e ON e.id = p.id
      WHERE e.region_id = :regionId
        AND MATCH(p.title, p.content) AGAINST(:q IN NATURAL LANGUAGE MODE)
      """, nativeQuery = true)
  long countByKeywordInRegion(
      @Param("q") String keyword,
      @Param("regionId") Long regionId
  );

  @Query("""
      SELECT e
      FROM EventPost e
      WHERE e.id IN :ids
      ORDER BY FUNCTION('FIND_IN_SET', e.id, :orderCsv)
      """)
  List<EventPost> findAllByIdInOrder(
      @Param("ids") List<Long> ids,
      @Param("orderCsv") String orderCsv
  );

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

  /**
   * [전체 조회] regionId 기준 - 활성 UP: isUp=true AND (upExpiresAt IS NULL OR upExpiresAt > NOW) - 1순위: 활성
   * UP 우선 - 2순위: 활성 UP 끼리는 seed 기반 랜덤 (CRC32 사용) - 3순위: 나머지 최신순(createdAt desc, id desc)
   */
  @EntityGraph(attributePaths = {"user", "eventImages"})
  @Query("""
      SELECT e
      FROM EventPost e
      WHERE e.region.id = :regionId
      ORDER BY
        CASE
          WHEN (e.isUp = true AND (e.upExpiresAt IS NULL OR e.upExpiresAt > CURRENT_TIMESTAMP)) THEN 0
          ELSE 1
        END,
        CASE
          WHEN (e.isUp = true AND (e.upExpiresAt IS NULL OR e.upExpiresAt > CURRENT_TIMESTAMP))
            THEN FUNCTION('CRC32', CONCAT(CAST(e.id AS string), :seed))
        END,
        e.createdAt DESC, e.id DESC
      """)
  Page<EventPost> findSeededUpFirst(@Param("regionId") Long regionId,
      @Param("seed") long seed,
      Pageable pageable);

  /**
   * [타입별 조회] regionId + eventType - 정렬 로직 동일
   */
  @EntityGraph(attributePaths = {"user", "eventImages"})
  @Query("""
      SELECT e
      FROM EventPost e
      WHERE e.region.id = :regionId
        AND e.eventType = :eventType
      ORDER BY
        CASE
          WHEN (e.isUp = true AND (e.upExpiresAt IS NULL OR e.upExpiresAt > CURRENT_TIMESTAMP)) THEN 0
          ELSE 1
        END,
        CASE
          WHEN (e.isUp = true AND (e.upExpiresAt IS NULL OR e.upExpiresAt > CURRENT_TIMESTAMP))
            THEN FUNCTION('CRC32', CONCAT(CAST(e.id AS string), :seed))
        END,
        e.createdAt DESC, e.id DESC
      """)
  Page<EventPost> findSeededUpFirstByType(@Param("regionId") Long regionId,
      @Param("eventType") EventType eventType,
      @Param("seed") long seed,
      Pageable pageable);

  // 요약 업데이트용 레포지토리 메서드
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update EventPost e set e.summary = :summary, e.updatedAt = CURRENT_TIMESTAMP where e.id = :id")
  int updateSummary(@Param("id") Long id, @Param("summary") String summary);
}
