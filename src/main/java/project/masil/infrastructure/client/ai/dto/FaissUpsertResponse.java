package project.masil.infrastructure.client.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FaissUpsertResponse {

  private int upserted;
  private int ntotal;

}