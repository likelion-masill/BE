package project.masil.user.service;

import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.global.response.ListResponse;
import project.masil.user.entity.SearchLog;
import project.masil.user.repository.SearchLogRepository;

@Service
@AllArgsConstructor
public class SearchLogService {

  private final SearchLogRepository repository;


  @Transactional
  public void log(Long userId, String query) {
    if (query == null || query.isBlank()) {
      return;
    }
    SearchLog e = SearchLog.builder()
        .userId(userId)
        .query(query)
        .build();
    repository.save(e);
  }

  @Transactional(readOnly = true)
  public ListResponse<String> getRecent10() {
    return repository.findTop10ByOrderByIdDesc()
        .stream()
        .map(SearchLog::getQuery)
        .collect(Collectors.collectingAndThen(
            Collectors.toList(),
            items -> new ListResponse<>(items.size(), items)
        ));
  }
}