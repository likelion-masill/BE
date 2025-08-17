package project.masil.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 메시지 전송(TEXT 전용)
 */
@Getter
@Setter
public class SendMessageRequest {

  @NotBlank(message = "메시지 내용은 필수입니다.")
  @Schema(description = "메시지 내용", example = "안녕하세요 제 이름은 비쿠입니다.")
  private String content;


}
