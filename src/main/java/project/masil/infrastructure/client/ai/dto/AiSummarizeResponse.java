package project.masil.infrastructure.client.ai.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class AiSummarizeResponse {

  private String status;
  private String message;
  private String data;

}