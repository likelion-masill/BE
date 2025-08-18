package project.masil.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import project.masil.chat.enums.ChatContextType;

/**
 * [요청] 채팅방 생성/가져오기
 *
 * 어디서 시작한 대화인지(컨텍스트)와 상대 사용자 ID를 함께 보낸다.
 * - contextType : EVENT_POST / COMMENT / CLUB_POST
 * - contextId : 이벤트 게시글ID 또는 댓글ID 또는 소모임 ID
 * - targetUserId: 대화할 상대의 사용자 ID
 *
 * 참고) 컨트롤러에서는 각 진입점(이벤트/댓글/소모임) 전용 엔드포인트도 제공하여
 *      targetUserId를 서버에서 유추하여 채워 호출할 수 있게 했음(아래 Controller 참고).
 */
@Getter
@Setter
public class CreateRoomRequest {

  @NotNull(message = "contextType 입력은 필수입니다.")
  @Schema(description = "ChatContextType(이벤트, 댓글, 소모임) 채팅 유형", example = "이벤트")
  private ChatContextType contextType;

  @NotNull(message = "contextId(이벤트 게시글ID, 댓글ID, 소모임ID)는 필수입니다.")
  @Schema(description = "contextId(이벤트 게시글ID, 댓글ID, 소모임ID)", example = "1")
  private Long contextId;

  @NotNull(message = "대화할 상대의 사용자 ID는 필수입니다.")
  @Schema(description = "대화할 상대의 사용자 ID", example = "2")
  private Long targetUserId;

}
