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
   * [전체 조회 - 최신순 기본]
   * - regionId 기준으로 이벤트 게시글 전체를 조회.
   *
   * 정렬 우선순위:
   *   1) 활성 UP 게시물 우선 노출
   *      - 조건: isUp = true AND (upExpiresAt IS NULL OR upExpiresAt > 현재시간)
   *
   *   2) 같은 활성 UP 게시물들 끼리는 랜덤 순서
   *      - CRC32(e.id + seed) 사용 → seed를 시간 단위로 고정하여 한 시간 동안 동일한 랜덤 순서 유지
   *
   *   3) 나머지 일반 게시물들은 최신순
   *      - createdAt DESC, id DESC
   *
   *   THEN 0 -> 활성 UP 이벤트 게시물들 -> 항상 먼저 정렬됨
   *   ELSE 1 -> 일반 이벤트 게시물들 -> UP 다음에 정렬됨
   *
   * 주의:
   *   - @EntityGraph(attributePaths = {"user", "eventImages"}) 를 통해 N+1 문제를 방지
   *   - pageable 을 통해 페이징 처리
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
   * [타입별 조회 - 최신순 기본] regionId + eventType - 정렬 로직 동일
   *
   * THEN 0 -> 활성 UP 이벤트 게시물들 -> 항상 먼저 정렬됨
   * ELSE 1 -> 일반 이벤트 게시물들 -> UP 다음에 정렬됨
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


  /**
   * [전체 이벤트 게시글들 기준 댓글순 정렬 조회]
   * - 정렬 우선순위:
   *   1) 활성 UP 최상단
   *   2) 같은 UP끼리는 CRC32(seed) 랜덤
   *   3) 그 외는 댓글 많은 순(commentCount DESC)
   *   4) 동점 처리: id DESC (안정적 순서 보장용)
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
      e.commentCount DESC, e.id DESC
    """)
  Page<EventPost> findSeededUpFirstOrderByComments(@Param("regionId") Long regionId,
      @Param("seed") long seed,
      Pageable pageable);

  /**
   * [전체 이벤트 게시글들 기준 인기순(좋아요수) 정렬 조회]
   * - 정렬 우선순위:
   *   1) 활성 UP 최상단
   *   2) 같은 UP끼리는 CRC32(seed) 랜덤
   *   3) 그 외는 좋아요 많은 순(favoriteCount DESC)
   *   4) 동점 처리: id DESC
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
      e.favoriteCount DESC, e.id DESC
    """)
  Page<EventPost> findSeededUpFirstOrderByPopularity(@Param("regionId") Long regionId,
      @Param("seed") long seed,
      Pageable pageable);

  /**
   * [타입별 + 댓글순 정렬 조회]
   *
   * - regionId + eventType 조건으로 이벤트 게시글을 조회.
   *
   * 정렬 우선순위:
   *   1) 활성 UP 게시물 최상단
   *      - 조건: isUp = true AND (upExpiresAt IS NULL OR upExpiresAt > 현재시간)
   *
   *   2) 같은 활성 UP 게시물들끼리는 CRC32(seed) 기반 랜덤
   *      - seed를 시간 단위로 고정(hourlySeed)하여, 한 시간 동안은 동일한 순서 유지
   *
   *   3) 그 외 일반 게시물들은 댓글 많은 순(commentCount DESC)
   *
   *   4) 동점(commentCount 같은 경우) → id DESC 로 정렬 안정성 보장
   *
   * 주의:
   *   - @EntityGraph(attributePaths = {"user", "eventImages"}) → user, eventImages 연관관계 즉시 로딩(N+1 방지)
   *   - Pageable 로 페이징 처리
   */
  @EntityGraph(attributePaths = {"user", "eventImages"})
  @Query("""
  SELECT e
  FROM EventPost e
  WHERE e.region.id = :regionId AND e.eventType = :eventType
  ORDER BY
    CASE
      WHEN (e.isUp = true AND (e.upExpiresAt IS NULL OR e.upExpiresAt > CURRENT_TIMESTAMP)) THEN 0
      ELSE 1
    END,
    CASE
      WHEN (e.isUp = true AND (e.upExpiresAt IS NULL OR e.upExpiresAt > CURRENT_TIMESTAMP))
        THEN FUNCTION('CRC32', CONCAT(CAST(e.id AS string), :seed))
    END,
    e.commentCount DESC, e.id DESC
  """)
  Page<EventPost> findSeededUpFirstByTypeOrderByComments(@Param("regionId") Long regionId,
      @Param("eventType") EventType eventType,
      @Param("seed") long seed,
      Pageable pageable);


  /**
   * [타입별 + 인기순(좋아요수) 정렬 조회]
   *
   * - regionId + eventType 조건으로 이벤트 게시글을 조회.
   *
   * 정렬 우선순위:
   *   1) 활성 UP 게시물 최상단
   *      - 조건: isUp = true AND (upExpiresAt IS NULL OR upExpiresAt > 현재시간)
   *
   *   2) 같은 활성 UP 게시물들끼리는 CRC32(seed) 기반 랜덤
   *      - seed를 시간 단위로 고정(hourlySeed)하여, 한 시간 동안은 동일한 순서 유지
   *
   *   3) 그 외 일반 게시물들은 좋아요 많은 순(favoriteCount DESC)
   *
   *   4) 동점(favoriteCount 같은 경우) → id DESC 로 정렬 안정성 보장
   *
   * 주의:
   *   - @EntityGraph(attributePaths = {"user", "eventImages"}) → user, eventImages 연관관계 즉시 로딩(N+1 방지)
   *   - Pageable 로 페이징 처리
   */
  @EntityGraph(attributePaths = {"user", "eventImages"})
  @Query("""
  SELECT e
  FROM EventPost e
  WHERE e.region.id = :regionId AND e.eventType = :eventType
  ORDER BY
    CASE
      WHEN (e.isUp = true AND (e.upExpiresAt IS NULL OR e.upExpiresAt > CURRENT_TIMESTAMP)) THEN 0
      ELSE 1
    END,
    CASE
      WHEN (e.isUp = true AND (e.upExpiresAt IS NULL OR e.upExpiresAt > CURRENT_TIMESTAMP))
        THEN FUNCTION('CRC32', CONCAT(CAST(e.id AS string), :seed))
    END,
    e.favoriteCount DESC, e.id DESC
  """)
  Page<EventPost> findSeededUpFirstByTypeOrderByPopularity(@Param("regionId") Long regionId,
      @Param("eventType") EventType eventType,
      @Param("seed") long seed,
      Pageable pageable);



  // 요약 업데이트용 레포지토리 메서드
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update EventPost e set e.summary = :summary, e.updatedAt = CURRENT_TIMESTAMP where e.id = :id")
  int updateSummary(@Param("id") Long id, @Param("summary") String summary);
}
