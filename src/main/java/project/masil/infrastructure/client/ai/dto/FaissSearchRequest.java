package project.masil.infrastructure.client.ai.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FaissSearchRequest {

  private List<Float> queryEmbedding;
  private List<Long> candidateIds;
  private Integer topK;
  // 파이썬 서버에 정규화를 요청하는 필드
  private Boolean normalize; // true 권장 (L2 정규화 전제)

}