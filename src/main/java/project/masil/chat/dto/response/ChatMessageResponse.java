package project.masil.chat.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 메세지 응답 DTO
 */
@Getter
@Builder
public class ChatMessageResponse {
  private Long messageId; //메시지ID
  private Long roomId; //채팅방 ID
  private Long senderId; //메시지 보낸사람ID
  private String content;
  private LocalDateTime createdAt;
}
