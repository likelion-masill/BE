package project.masil.embedding.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import project.masil.community.entity.PostEmbedding;
import project.masil.community.repository.PostEmbeddingRepository;
import project.masil.global.config.props.OpenAIProps;
import project.masil.global.util.EmbeddingCodec;
import project.masil.infrastructure.client.ai.dto.CommonResponse;
import project.masil.infrastructure.client.ai.dto.FaissRemoveResponse;
import project.masil.infrastructure.client.ai.dto.FaissUpsertRequest;
import project.masil.infrastructure.client.ai.dto.FaissUpsertResponse;
import project.masil.infrastructure.client.openAi.dto.OpenAIEmbeddingRequest;
import project.masil.infrastructure.client.openAi.dto.OpenAIEmbeddingResponse;

@Service
@Slf4j
public class EmbeddingPipelineService {

  private static final int EXPECTED_DIM = 1536;

  @Qualifier("openaiWebClient")
  private final WebClient openai;              // OpenAI 호출
  @Qualifier("aiWebClient")
  private final WebClient ai;                  // 파이썬 FAISS 서버
  private final OpenAIProps props;
  private final PostEmbeddingRepository postEmbeddingRepository;

  @Autowired
  public EmbeddingPipelineService(
      @Qualifier("openaiWebClient") WebClient openai,
      @Qualifier("aiWebClient") WebClient ai,
      OpenAIProps props,
      PostEmbeddingRepository postEmbeddingRepository
  ) {
    this.openai = openai;
    this.ai = ai;
    this.props = props;
    this.postEmbeddingRepository = postEmbeddingRepository;
  }


  /**
   * 게시글 저장/수정 시 호출: 임베딩 → DB → FAISS upsert
   */
  @Transactional
  public void upsertPost(long postId, long regionId, String title, String body) {
    String input = title + " \n\n###\n\n " + body;

    // 1) OpenAI 임베딩
    List<Float> vec = requestEmbedding(input);

    // 2) DB에 임베딩 값 저장 (LONGBLOB)
    PostEmbedding pe = postEmbeddingRepository.findById(postId).orElseGet(PostEmbedding::new);
    pe.setPostId(postId);
    pe.setEmbedding(EmbeddingCodec.toBytes(vec));
    pe.setRegionId(regionId);
    postEmbeddingRepository.save(pe);

    // 3) 파이썬 FAISS upsert
    FaissUpsertRequest upsert = new FaissUpsertRequest(postId, vec);
    CommonResponse<FaissUpsertResponse> res = ai.post()
        .uri("/api/faiss/upsert")
        .bodyValue(upsert)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<CommonResponse<FaissUpsertResponse>>() {
        })
        .block();

    if (res == null || !"success".equalsIgnoreCase(res.getStatus())) {
      throw new IllegalStateException("FAISS upsert 실패: "
          + (res != null ? res.getMessage() : "응답 없음"));
    }

    // 성공 시 데이터 활용 가능
    int upserted = res.getData().getUpserted();
    int total = res.getData().getNtotal();
    log.info("FAISS upsert 성공: upserted={}, ntotal={}", upserted, total);

  }

  public void removePost(long postId) {
    // 1) DB 임베딩 삭제(있을 때만)
    try {
      if (postEmbeddingRepository.existsById(postId)) {
        postEmbeddingRepository.deleteById(postId);
      }
    } catch (Exception e) {
      // 여기서 실패하더라도 FAISS remove는 시도 (로그만 남김)
      log.warn("PostEmbedding DB 삭제 실패 postId={}: {}", postId, e.getMessage(), e);
    }

    // 2) 파이썬 FAISS remove 호출
    CommonResponse<FaissRemoveResponse> res = ai.delete()
        .uri("/api/faiss/remove/{postId}", postId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<CommonResponse<FaissRemoveResponse>>() {
        })
        .block();

    if (res == null || !"success".equalsIgnoreCase(res.getStatus())) {
      throw new IllegalStateException(
          "FAISS remove 실패: " + (res != null ? res.getMessage() : "응답 없음"));
    }

    FaissRemoveResponse data = res.getData();
    log.info("FAISS remove 성공: removed={}, ntotal={}",
        data != null ? data.getRemoved() : null,
        data != null ? data.getNtotal() : null);
  }

  // ----- 내부: OpenAI 임베딩 -----
  public List<Float> requestEmbedding(String text) {
    OpenAIEmbeddingRequest req = new OpenAIEmbeddingRequest(props.getEmbeddingModel(), text);

    OpenAIEmbeddingResponse res = openai.post().uri("/v1/embeddings")
        .bodyValue(req)
        .retrieve()
        .bodyToMono(OpenAIEmbeddingResponse.class)
        .block();

    if (res == null || res.getData() == null || res.getData().isEmpty()) {
      throw new IllegalStateException("OpenAI embedding response empty");
    }

    System.out.println("OpenAI embedding response: " + res.toString());

    List<Float> vec = res.getData().get(0).getEmbedding();
    if (vec == null || vec.size() != EXPECTED_DIM) {
      throw new IllegalStateException(
          "Unexpected embedding dim: " + (vec == null ? -1 : vec.size()));
    }

    return vec;
  }
}