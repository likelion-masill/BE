package project.masil.infrastructure.client.openAi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIEmbeddingRequest {

  // 임베딩 모델
  private String model;
  // 임베딩할 텍스트
  private String input; // title + body
}
