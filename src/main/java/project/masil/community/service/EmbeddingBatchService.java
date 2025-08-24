package project.masil.community.service;

import static project.masil.community.service.EventPostService.LEN_THRESHOLD;
import static project.masil.community.service.EventPostService.effectiveLen;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.community.entity.EventPost;
import project.masil.community.exception.EventErrorCode;
import project.masil.community.repository.EventPostRepository;
import project.masil.community.repository.PostEmbeddingRepository;
import project.masil.community.repository.PostRepository;
import project.masil.embedding.service.EmbeddingPipelineService;
import project.masil.global.exception.CustomException;
import project.masil.infrastructure.client.ai.AiClient;
import project.masil.infrastructure.client.ai.dto.AiSummarizeRequest;
import project.masil.infrastructure.client.ai.dto.AiSummarizeResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingBatchService {

  private final AiClient aiClient;
  private final EventPostRepository eventPostRepository;
  private final EventPostUpdater updater;
  private final PostEmbeddingRepository postEmbeddingRepository;
  private final EmbeddingPipelineService embeddingPipelineService;
  private final PostRepository postRepository;


  @Transactional
  public List<Long> processMissingEmbeddings() {
    // 1) EventPost 전체 ID
    List<Long> allPostIds = eventPostRepository.findAllIds();

    List<Long> processed = new ArrayList<>();

    //  upsert 실행
    for (Long postId : allPostIds) {
      eventPostRepository.findById(postId).ifPresent(ep -> {
        // 길이 체크 (기존에 쓰던 메서드 재사용)
        int bodyLen = effectiveLen(ep.getContent());
        if (bodyLen < LEN_THRESHOLD) {
          log.info("[EMB] skip postId={} (bodyLen={} < {})", postId, bodyLen, LEN_THRESHOLD);
          return;
        }

        long regionIdMeta = (ep.getRegion() != null ? ep.getRegion().getId() : 0L);

        try {
          EventPost eventPost = eventPostRepository.findById(postId)
              .orElseThrow(() -> new CustomException(EventErrorCode.EVENT_NOT_FOUND));

          AiSummarizeRequest req = new AiSummarizeRequest(
              eventPost.getContent(), 5, 10, 0.3, 300
          );
          AiSummarizeResponse res = aiClient.summarize(req);
          if (res != null && "success".equalsIgnoreCase(res.getStatus()) && res.getData() != null) {
            String summary = res.getData().trim();
            updater.updateSummary(eventPost.getId(), summary);
          }

          // regionId가 메타로만 필요할 때
          embeddingPipelineService.upsertPost(postId, regionIdMeta, ep.getTitle(), ep.getContent());

          // 만약 region을 완전히 제거한 오버로드를 사용 중이라면 ↓ 한 줄로 교체
          // embeddingPipelineService.upsertPost(postId, ep.getTitle(), ep.getContent());

          processed.add(postId);
        } catch (Exception e) {
          // 개별 실패는 로깅만 하고 계속 진행
          log.error("[EMB] upsert 실패 postId={}", postId, e);
        }
      });
    }

    return processed; // 실제로 upsert 시도(성공)한 ID 목록
  }

}