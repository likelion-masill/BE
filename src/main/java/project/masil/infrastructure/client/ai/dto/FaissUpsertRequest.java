package project.masil.infrastructure.client.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FaissUpsertRequest {

  @JsonProperty("post_id")
  private long postId;
  private List<Float> embedding;
}