package project.masil.chat.dto.response;

// project.masil.chat.dto.response.ResolveTargetResponse.java
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import project.masil.chat.enums.ChatContextType;

@Getter
@Builder
public class ChatTargetResponse {
  @Schema(description = "컨텍스트 타입", example = "EVENT_POST")
  private ChatContextType contextType;

  @Schema(description = "컨텍스트 ID", example = "1")
  private Long contextId;

  @Schema(description = "상대 사용자 ID (메시지 보낼 대상)", example = "2")
  private Long targetUserId;
}

