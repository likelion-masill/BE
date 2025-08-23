package project.masil.infrastructure.client.ai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FaissRemoveResponse {

  private int removed;
  private int ntotal;
}