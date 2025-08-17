// infrastructure/client/ai/dto/AiSummarizeRequest.java
package project.masil.infrastructure.client.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class AiSummarizeRequest {

  @JsonProperty("text")
  private String text;

  @JsonProperty("top_k")
  private int topK = 5;

  @JsonProperty("min_len")
  private int minLen = 10;

  @JsonProperty("temperature")
  private double temperature = 0.3;

  @JsonProperty("max_output_tokens")
  private int maxOutputTokens = 300;

}