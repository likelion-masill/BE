package project.masil.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import project.masil.user.entity.SearchLog;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

  // 최신(id 내림차순) 10개
  List<SearchLog> findTop10ByOrderByIdDesc();
}