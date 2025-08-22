package project.masil.chat.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import project.masil.chat.enums.ChatContextType;

/**
 * 채팅방 응답 DTO - targetUserId : 현재 사용자 기준 상대방 ID - myUnreadCount : 현재 사용자 기준 '내' 안읽음 수 -
 * contextType/Id : 이 방이 어떤 출처에서 시작됐는지
 */
@Getter
@Builder
public class ChatRoomResponse {

  private Long roomId;
  private ChatContextType contextType;
  private Long contextId;
  private Long targetUserId;
  private String targetUserProfileImageUrl;
  private String targetUserNickname;
  private String lastMessage;
  private LocalDateTime lastMessageAt;
  private int myUnreadCount;

}
