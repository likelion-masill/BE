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


  @PostMapping("/all")
  @Operation(summary = "게시글 임베딩 배치 처리", description = "모든 게시글에 대해 임베딩 처리를 수행합니다.")
  public ResponseEntity<BaseResponse<List<Long>>> processMissingEmbeddings() {
    List<Long> processed = embeddingBatchService.processMissingEmbeddings();
    return ResponseEntity.ok(BaseResponse.success("게시글 임베딩 배치 처리 완료", processed));
  }
}