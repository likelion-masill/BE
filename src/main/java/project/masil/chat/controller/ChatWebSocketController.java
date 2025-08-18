package project.masil.chat.controller;

import java.security.Principal;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import project.masil.chat.dto.request.SendMessageRequest;
import project.masil.chat.dto.response.ChatMessageResponse;
import project.masil.chat.dto.response.ChatRoomResponse;
import project.masil.chat.exception.ChatErrorCode;
import project.masil.chat.service.ChatService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import project.masil.chat.websocket.WebSocketPrincipal;
import project.masil.global.exception.CustomException;

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
      Principal principal,
      Message<?> message
  ) {
    // 1) STOMP 세션에서 userId 꺼내기
    Long userId = extractUserId(principal, message); //보내는 사람 userId
    log.info("[WS] RECEIVED message roomId={}, senderId={}, content={}",
            roomId, userId, payload.getContent());   // ✅ 수신 로그 추가

    // 2) 저장/캐시/unread 반영
    ChatMessageResponse saved = chatService.sendMessage(roomId, userId, payload);

    // 3) 수신자(상대방) 식별
    Long otherUserId = chatService.getOtherParticipantId(roomId, userId);
    log.info("[WS] SEND roomId={} sender={} other={}", roomId, userId, otherUserId);

    // ✅ JSON 헤더(콘텐츠 타입) 명시
    var headers = SimpMessageHeaderAccessor.create();
    headers.setContentType(org.springframework.util.MimeTypeUtils.APPLICATION_JSON);
    headers.setLeaveMutable(true);

    String dest = "/queue/rooms." + roomId;

    // 내 화면
    broker.convertAndSendToUser(String.valueOf(userId), dest, saved, headers.getMessageHeaders());
    // 상대 화면
    broker.convertAndSendToUser(String.valueOf(otherUserId), dest, saved, headers.getMessageHeaders());

    // (선택) 목록행 갱신
    ChatRoomResponse myRow = chatService.getRoomRowFor(roomId, userId);
    ChatRoomResponse otherRow = chatService.getRoomRowFor(roomId, otherUserId);

    broker.convertAndSendToUser(String.valueOf(userId), "/queue/rooms.list", myRow, headers.getMessageHeaders());
    broker.convertAndSendToUser(String.valueOf(otherUserId), "/queue/rooms.list", otherRow, headers.getMessageHeaders());
  }


  /** Principal이 없으면 세션 속성에서 복구 */
  private Long extractUserId(Principal principal, Message<?> message) {
    if (principal instanceof WebSocketPrincipal wsp) return wsp.getUserId();
    if (principal != null) {
      try { return Long.valueOf(principal.getName()); } catch (Exception ignore) {}
    }
    var acc = org.springframework.messaging.simp.stomp.StompHeaderAccessor.wrap(message);
    Map<String, Object> attrs = acc.getSessionAttributes();
    if (attrs != null) {
      Object v = attrs.get("userId");
      if (v instanceof Long l) return l;
      if (v instanceof Integer i) return i.longValue();
      if (v instanceof String s) { try { return Long.parseLong(s); } catch (Exception ignore) {} }
    }
    throw new CustomException(ChatErrorCode.WEBSOCKET_UNAUTHORIZED);
  }

  /**
   * [읽음 처리]
   * 클라이언트 전송 경로: /app/chat/rooms/{roomId}/read
   * - 방을 열람/포커스 했을 때 호출 → "내 unread=0"
   * - (선택) 상대에게 "읽음" 이벤트를 보내고 싶으면 개인 큐로 안내 가능
   */
  @MessageMapping("/rooms/{roomId}/read")
  public void markRead(@DestinationVariable Long roomId, Principal principal, Message<?> message) {
    Long userId = extractUserId(principal, message);
    chatService.markAsRead(roomId, userId);
    ChatRoomResponse myRow = chatService.getRoomRowFor(roomId, userId);

    SimpMessageHeaderAccessor h = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
    h.setLeaveMutable(true);
    h.setContentType(org.springframework.util.MimeType.valueOf("application/json"));

    broker.convertAndSendToUser(String.valueOf(userId), "/queue/rooms.list", myRow, h.getMessageHeaders());
  }



}



