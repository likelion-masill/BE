package project.masil.community.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.masil.community.service.EventPostUpdater;
import project.masil.embedding.service.EmbeddingPipelineService;
import project.masil.infrastructure.client.ai.AiClient;
import project.masil.infrastructure.client.ai.dto.AiSummarizeRequest;
import project.masil.infrastructure.client.ai.dto.AiSummarizeResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPostAsyncListener {

  private final AiClient aiClient;
  private final EventPostUpdater updater;
  private final EmbeddingPipelineService embeddingPipelineService;

  @Async("appTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleSummary(EventCreatedEvent e) {
    try {
      AiSummarizeRequest req = new AiSummarizeRequest(
          e.content(), 5, 10, 0.3, 300
      );
      AiSummarizeResponse res = aiClient.summarize(req);
      if (res != null && "success".equalsIgnoreCase(res.getStatus()) && res.getData() != null) {
        String summary = res.getData().trim();
        updater.updateSummary(e.postId(), summary);
      }
    } catch (Exception ex) {
      log.warn("요약 생성/저장 실패 postId={}", e.postId(), ex);
    }
  }

  @Async("appTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleEmbeddingUpsert(EventCreatedEvent e) {
    try {
      embeddingPipelineService.upsertPost(
          e.postId(),
          e.regionId(),
          e.title(),
          e.content()
      );
    } catch (Exception ex) {
      log.warn("임베딩 upsert 실패 postId={}", e.postId(), ex);
    }
  }
}