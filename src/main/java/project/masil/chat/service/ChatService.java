package project.masil.chat.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.masil.chat.dto.request.SendMessageRequest;
import project.masil.chat.dto.response.ChatMessageResponse;
import project.masil.chat.dto.response.ChatRoomResponse;
import project.masil.chat.dto.response.ChatTargetResponse;
import project.masil.chat.enums.ChatContextType;

/**
 *  1) 채팅방 생성/조회 (범용: 컨텍스트 + 상대ID)
 *  - 채팅방이 없으면 생성
 *  - 채팅방이 있으면 기존 채팅방
 *
 *  2) 내 채팅방 목록
 *  3) 특정 방 메시지 조회(조회 시 내 unread=0)
 *  4) 메시지 전송(TEXT)
 */
public interface ChatService {

  /**
   * 이벤트, 소모임, 댓글 작성자 ID 조회
   * @param contextType
   * @param contextId
   * @return
   */
  Long findTargetUserId(ChatContextType contextType, Long contextId);

  /**
   * 채팅방 생성/조회 (범용: 컨텍스트 + 상대ID)
   *  - 채팅방이 없으면 생성
   *  - 채팅방이 있으면 기존 채팅방
   * @param contextType
   * @param contextId
   * @param targetUserId
   * @param currentUserId
   * @return
   */
  ChatRoomResponse openChatRoom(
      ChatContextType contextType, Long contextId, Long targetUserId, Long currentUserId
  );

  /**
   * 내 채팅방 목록 조회
   * @param currentUserId
   * @param pageable
   * @return
   */
  Page<ChatRoomResponse> getMyRooms(Long currentUserId, Pageable pageable);

  /**
   * 특정 방 메시지 조회(조회 시 unread = 0)
   * @param roomId
   * @param currentUserId
   * @param pageable
   * @return
   */
  Page<ChatMessageResponse> getMessages(Long roomId, Long currentUserId, Pageable pageable);

  /**
   * 메시지 전송 기능
   * @param roomId
   * @param senderId
   * @param request
   * @return
   */
  ChatMessageResponse sendMessage(Long roomId, Long senderId, SendMessageRequest request);


  /**
   * [읽음 처리]
   * - 해당 방을 'userId'가 열었을 때, 자신의 unread 카운트를 0으로 초기화.
   * - REST(메시지 조회) 또는 WebSocket(/app/chat/rooms/{roomId}/read)에서 호출.
   */
  void markAsRead(Long roomId, Long userId);

  /**
   * [상대방 식별]
   * - 1:1 방에서 'me의 상대방 userId를 돌려준다.
   * - 권한 및 존재 검증 포함(방이 없거나 내가 참여자가 아니면 예외).
   */
  Long getOtherParticipantId(Long roomId, Long me);

  /**
   * [방 목록 한 줄(내 관점)]
   * - roomId 기준으로 'userId' 관점의 ChatRoomResponse를 만들어 반환.
   * - myUnreadCount/targetUserId 등 "내 관점"으로 변환된 DTO를 얻는다.
   */
  ChatRoomResponse getRoomRowFor(Long roomId, Long userId);

}
