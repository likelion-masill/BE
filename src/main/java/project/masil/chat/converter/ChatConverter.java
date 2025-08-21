package project.masil.chat.converter;

import org.springframework.stereotype.Component;
import project.masil.chat.dto.response.ChatMessageResponse;
import project.masil.chat.dto.response.ChatRoomResponse;
import project.masil.chat.entity.ChatMessage;
import project.masil.chat.entity.ChatRoom;
import project.masil.global.exception.CustomException;
import project.masil.user.entity.User;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

/**
 * 채팅방 -> 채팅방응답 DTO로 바꿔주는 컨버터 - targetUserId와 myUnreadCount는 '현재 사용자' 관점으로 계산한다.
 */
@Component
public class ChatConverter {

  private UserRepository userRepository;

  /**
   * ChatRoom 엔티티 → ChatRoomResponse 응답으로 변환.
   * <p>
   * "현재 사용자(userId)" 관점으로 아래 두 값을 치환해서 내려준다. - targetUserId   : 상대방 사용자 ID - myUnreadCount : '내'
   * 안읽음 카운트
   * <p>
   * 예) room(userAId=3, userBId=10, unreadA=0, unreadB=5) - userId=10 → targetUserId=3,
   * myUnreadCount=5 - userId=3  → targetUserId=10, myUnreadCount=0
   *
   * @param room   채팅방 엔티티
   * @param userId 현재 로그인 사용자 ID
   * @return 현재 사용자 관점으로 치환된 방 응답 DTO
   */
  public ChatRoomResponse toRoomResponse(ChatRoom room, Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    // 내가 A인지(B인지) 판별 : true -> A, false -> B
    boolean userIdIsA = room.getUserAId().equals(userId);

    // 상대방 ID : 내가 A면 상대방 B , 내가 B면 A
    Long targetUserId = userIdIsA ? room.getUserBId() : room.getUserAId();

    // 내 안읽음 수 : 내가 A면 unreadCountA, 내가 B면 unreadCountB
    int myUnreadCount = userIdIsA ? room.getUnreadCountA() : room.getUnreadCountB();

    return ChatRoomResponse.builder()
        .roomId(room.getId())
        .contextType(room.getContextType())
        .contextId(room.getContextId())
        .targetUserId(targetUserId)
        .targetUserProfileImageUrl(user.getProfileImageUrl())
        .lastMessage(room.getLastMessage())
        .lastMessageAt(room.getLastMessageAt()) // 마지막 메시지 시각
        .myUnreadCount(myUnreadCount) // 현재 사용자 관점의 내 안읽음 수
        .build();
  }

  /**
   * ChatMessage -> ChatMessageResponse
   */
  public ChatMessageResponse toMessageResponse(ChatMessage message) {
    return ChatMessageResponse.builder()
        .messageId(message.getId())
        .roomId(message.getRoom().getId())
        .senderId(message.getSender().getId())
        .content(message.getContent())
        .createdAt(message.getCreatedAt())
        .build();
  }


}
