package project.masil.infrastructure.client.ai;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import project.masil.infrastructure.client.ai.dto.CommonResponse;
import project.masil.infrastructure.client.ai.dto.FaissSearchRequest;
import project.masil.infrastructure.client.ai.dto.FaissSearchResponse;

@Service
public class AiRerankService {

  @Qualifier("aiWebClient")      // 파이썬 서버용 WebClient
  private final WebClient ai;

  @Autowired
  public AiRerankService(@Qualifier("aiWebClient") WebClient ai) {
    this.ai = ai;
  }

  public List<Long> searchByAI(List<Long> candidateIds, List<Float> queryEmbedding, int topK) {
    if (candidateIds == null || candidateIds.isEmpty()) {
      return List.of();
    }

    FaissSearchRequest req = new FaissSearchRequest(queryEmbedding, candidateIds, topK, true);

    ParameterizedTypeReference<CommonResponse<FaissSearchResponse>> typeRef =
        new ParameterizedTypeReference<>() {
        };

    CommonResponse<FaissSearchResponse> res = ai.post()
        .uri("/api/faiss/search")   // 서버에서 candidateIds를 받아 서브셋 내에서만 검색
        .bodyValue(req)
        .retrieve()
        .bodyToMono(typeRef)
        .block();

    if (res == null) {
      throw new IllegalStateException("AI search null response");
    }
    if (!"success".equalsIgnoreCase(res.getStatus()) || res.getData() == null) {
      throw new IllegalStateException("AI search failed: " + res.getMessage());
    }

    return res.getData().getResults().stream()
        .map(FaissSearchResponse.Result::getPostId)
        .toList();
  }

  public List<Long> recommendByAI(List<Long> candidateIds, List<Float> queryEmbedding, int topK) {
    if (candidateIds == null || candidateIds.isEmpty()) {
      return List.of();
    }

    FaissSearchRequest req = new FaissSearchRequest(queryEmbedding, candidateIds, topK, true);

    ParameterizedTypeReference<CommonResponse<FaissSearchResponse>> typeRef =
        new ParameterizedTypeReference<>() {
        };

    CommonResponse<FaissSearchResponse> res = ai.post()
        .uri("/api/faiss/ai-recommend")   // 서버에서 candidateIds를 받아 서브셋 내에서만 검색
        .bodyValue(req)
        .retrieve()
        .bodyToMono(typeRef)
        .block();

    if (res == null) {
      throw new IllegalStateException("AI search null response");
    }
    if (!"success".equalsIgnoreCase(res.getStatus()) || res.getData() == null) {
      throw new IllegalStateException("AI search failed: " + res.getMessage());
    }

    return res.getData().getResults().stream()
        .map(FaissSearchResponse.Result::getPostId)
        .toList();
  }
}