package project.masil.infrastructure.client.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FaissSearchResponse {

  private Integer total;
  private List<Result> results;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Result {

    @JsonProperty("post_id")
    private Long postId;
    private Double score; // 코사인 유사도 등
  }
}