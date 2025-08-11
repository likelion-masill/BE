package project.masil.community.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import project.masil.community.entity.ClubPost;

public interface ClubPostRepository extends JpaRepository<ClubPost, Long> {

  /**
   * 페이징 처리 + N+1문제의 쿼리 횟수가
   *
   * @param pageable
   * @return
   */
  @EntityGraph(attributePaths = {"user"})
  Page<ClubPost> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
