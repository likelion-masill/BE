package project.masil.infrastructure.client.openAi.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OpenAIEmbeddingResponse {

  private List<EmbeddingData> data;
}