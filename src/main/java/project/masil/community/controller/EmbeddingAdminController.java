package project.masil.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.masil.community.service.EmbeddingBatchService;
import project.masil.global.response.BaseResponse;

@RestController
@RequestMapping("/api/admin/embeddings")
@RequiredArgsConstructor
public class EmbeddingAdminController {

  private final EmbeddingBatchService embeddingBatchService;


  @PostMapping("/process-missing")
  @Operation(summary = "누락된 게시글 임베딩 처리", description = "PostEmbedding 테이블에 없는 EventPost들을 찾아서 임베딩을 생성합니다.")
  public ResponseEntity<BaseResponse<List<Long>>> processMissingEmbeddings() {
    List<Long> processed = embeddingBatchService.processMissingEmbeddings();
    return ResponseEntity.ok(BaseResponse.success("누락된 임베딩 처리 완료", processed));
  }
}