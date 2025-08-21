package project.masil.community.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.community.repository.EventPostRepository;
import project.masil.community.repository.PostEmbeddingRepository;
import project.masil.embedding.service.EmbeddingPipelineService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingBatchService {

  private final EventPostRepository eventPostRepository;
  private final PostEmbeddingRepository postEmbeddingRepository;
  private final EmbeddingPipelineService embeddingPipelineService;

  @Transactional
  public List<Long> processMissingEmbeddings() {
    // 1) EventPost에 있는 모든 postId
    List<Long> allPostIds = eventPostRepository.findAllIds();

    // 2) PostEmbedding에 있는 postId
    List<Long> existingIds = postEmbeddingRepository.findAllIds();

    // 3) 차집합 = 아직 임베딩 안 된 게시글 id
    Set<Long> missingIds = new HashSet<>(allPostIds);
    missingIds.removeAll(existingIds);

    // 4) upsert 실행
    for (Long postId : missingIds) {
      eventPostRepository.findById(postId).ifPresent(ep -> {
        Long rid = (ep.getRegion() != null ? ep.getRegion().getId() : null);
        if (rid == null) {
          // 정책 택1: 건너뛰거나, 기본값(예: 0L) 사용 또는 예외 던지기
          // throw new IllegalStateException("regionId is null for postId=" + id);
          return; // 일단 건너뛰기 예시
        }
        embeddingPipelineService.upsertPost(postId, rid, ep.getTitle(), ep.getContent());
      });
    }

    return new ArrayList<>(missingIds);
  }

}