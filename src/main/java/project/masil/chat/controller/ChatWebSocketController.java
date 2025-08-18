package project.masil.chat.controller;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.stereotype.Controller;
import project.masil.chat.dto.request.SendMessageRequest;
import project.masil.chat.dto.response.ChatMessageResponse;
import project.masil.chat.dto.response.ChatRoomResponse;
import project.masil.chat.service.ChatService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;

/**
 * [ChatWebSocketController]
 *
 * 경로 약속(1:1 전용, 개인 큐 구독):
 *  - 연결(핸드셰이크):   /websocket/chat            (SockJS 엔드포인트)
 *  - 전송(클라→서버):   /app/chat/rooms/{roomId}/messages
 *  - 구독(서버→클라):   /user/queue/rooms.{roomId}  ← "본인 전용" 개인 큐
 *  - (선택) 목록 갱신:   /user/queue/rooms.list      ← 목록 행 갱신/배지카운트용 개인 큐
 *
 * 흐름:
 *  1) 사용자가 방 화면 입장 → "/user/queue/rooms.{roomId}" 구독
 *  2) 메시지 입력 → "/app/chat/rooms/{roomId}/messages" 로 전송
 *  3) 서버는 DB 저장 + 캐시 갱신 + unread 반영 후,
 *     보낸 사람/상대방 각각의 개인 큐로 메시지를 푸시
 *  4) (선택) 방 목록 행도 개인 큐로 각각 푸시(본인 관점의 myUnreadCount 포함)
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

  private final ChatService chatService; // 채팅 서비스 비즈니스 로직
  private final SimpMessagingTemplate broker; // WebSocket 브로커로 푸시

  /**
   * [메시지 전송]
   * 클라이언트 전송 경로: /app/chat/rooms/{roomId}/messages
   * - payload: { "content": "텍스트..." }
   * - Principal: Stomp 인터셉터에서 주입된 WebSocketPrincipal(userId)
   *
   * 처리:
   *  1) userId 식별
   *  2) 기존 서비스로 저장 (INSERT, lastMessage/lastMessageAt, unread 갱신)
   *  3) 보낸 사람 & 상대방의 "개인 큐"로 동일 메시지를 각각 푸시
   *  4) (선택) 각자 관점의 "목록 행"도 개인 큐로 푸시 → 최근메시지 갱신 즉시 반영
   */
  @MessageMapping("/rooms/{roomId}/messages")
  public void sendMessage(
      @DestinationVariable Long roomId,
      SendMessageRequest payload,
      Principal principal
  ) {
    // 1) STOMP 세션에서 userId 꺼내기
    Long userId = Long.valueOf(principal.getName()); //보내는 사람 userId
    log.info("[WS] IN rooms/{}/messages by user={} payload={}", roomId, userId, payload);


    // 2) 저장/캐시/unread 반영
    ChatMessageResponse saved = chatService.sendMessage(roomId, userId, payload);

    // 3) 수신자(상대방) 식별
    Long otherUserId = chatService.getOtherParticipantId(roomId, userId);

    // 4) 보낸 사람/상대방 각각 "개인 큐"로 메시지 푸시
    //    - 클라는 "/user/queue/rooms.{roomId}"를 구독해야 수신
    String destinationMy = "/queue/rooms." + roomId;
    String destinationOther = "/queue/rooms." + roomId;

    log.info("[WS] PUSH start dest={} my={} other={} body={}",
        destinationMy, userId, otherUserId, saved);

    broker.convertAndSendToUser(String.valueOf(userId), destinationMy, saved);
    broker.convertAndSendToUser(String.valueOf(otherUserId), destinationOther, saved);

    // 5) 방 목록 행 갱신(각자 관점: myUnreadCount 가 다름)
    //    - 프런트가 목록을 따로 구독하고 있다면 유용 (/user/queue/rooms.list)
    //    - 원치 않으면 이 블록은 제거해도 됨
    ChatRoomResponse myRoomRow = chatService.getRoomRowFor(roomId, userId);
    ChatRoomResponse otherRoomRow = chatService.getRoomRowFor(roomId, otherUserId);

    broker.convertAndSendToUser(String.valueOf(userId),    "/queue/rooms.list", myRoomRow);
    broker.convertAndSendToUser(String.valueOf(otherUserId), "/queue/rooms.list", otherRoomRow);
    log.info("[WS] PUSH done dest={} myRowUnread={} otherRowUnread={}",
        destinationMy, myRoomRow.getMyUnreadCount(), otherRoomRow.getMyUnreadCount());
  }

  /**
   * [읽음 처리]
   * 클라이언트 전송 경로: /app/chat/rooms/{roomId}/read
   * - 방을 열람/포커스 했을 때 호출 → "내 unread=0"
   * - (선택) 상대에게 "읽음" 이벤트를 보내고 싶으면 개인 큐로 안내 가능
   */
  @MessageMapping("/rooms/{roomId}/read")
  public void markRead(@DestinationVariable Long roomId, Principal principal) {
    Long userId = Long.valueOf(principal.getName());

    // 1) 내 unread= 0
    chatService.markAsRead(roomId, userId);

    // 2) 목록 행(row) DTO를 "내 관점"으로 만들어서
    ChatRoomResponse myRow = chatService.getRoomRowFor(roomId, userId);

    // 3) 내 개인 큐로 푸시 → 리스트 화면이 구독 중이면 즉시 배지/미리보기 갱신
    //    클라 구독 경로: /user/queue/rooms.list
    broker.convertAndSendToUser(String.valueOf(userId), "/queue/rooms.list", myRow);


  }


}
