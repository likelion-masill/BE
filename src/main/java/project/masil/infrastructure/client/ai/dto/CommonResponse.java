package project.masil.infrastructure.client.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {

  private String status;   // "success" or "error"
  private T data;          // 제네릭 타입
  private String message;
}
