package project.masil.chat.websocket;

import java.security.Principal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import project.masil.chat.exception.ChatErrorCode;
import project.masil.chat.repository.ChatRoomRepository;
import org.springframework.messaging.support.MessageBuilder;
import project.masil.global.exception.CustomException;

/**
 * [StompAuthChannelInterceptor]
 * WebSocket/STOMP "INBOUND 채널"로 들어오는 프레임(CONNECT / SUBSCRIBE / SEND)을 가로채
 * 인증/권한을 검사하는 필터 역할의 컴포넌트.
 *
 * 이 프로젝트는 "1:1 DM 전용"이므로, 다음 정책을 강제한다.
 *  - CONNECT  : 헤더(Authorization, X-User-Id 등)에서 userId를 뽑아 Principal 로 저장해야 한다.
 *  - SUBSCRIBE: "/user/queue/rooms.{roomId}" 구독은 해당 roomId의 "참여자"만 허용한다.
 *  - SEND     : "/app/chat/rooms/{roomId}/..." 전송도 해당 roomId의 "참여자"만 허용한다.
 *
 * 왜 필요한가?
 *  - WebSocket은 HTTP처럼 매 요청마다 필터/인증이 자동으로 동작하지 않음.
 *  - STOMP 프레임(연결/구독/전송)마다 직접 인증/권한을 체크해줘야 보안이 보장된다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

  private final ChatRoomRepository roomRepository;
  private final WebSocketUserResolver webSocketUserResolver;

  private static final Pattern APP_ROOMS_PATH = Pattern.compile("^/app/chat/rooms/(\\d+)/.*$");
  private static final Pattern USER_QUEUE_ROOMS = Pattern.compile("^/user/queue/rooms\\.(\\d+)$");
  private static final String USER_QUEUE_LIST = "/user/queue/rooms.list";

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    StompCommand cmd = accessor.getCommand();

    // CONNECT: 토큰 -> Principal 세팅
    if (accessor.getMessageType() == SimpMessageType.CONNECT || StompCommand.CONNECT.equals(cmd)) {
      Long userId = webSocketUserResolver.resolve(accessor);
      accessor.setUser(new WebSocketPrincipal(userId));
      Map<String, Object> session = accessor.getSessionAttributes();
      if (session != null) session.put("userId", userId);
      log.info("[WS] CONNECT userId={}", userId);
      accessor.setLeaveMutable(true);
      return message;
    }

    // 이후 프레임은 인증 필수
    Long userId = resolveUserId(accessor);
    if (accessor.getUser() == null) accessor.setUser(new WebSocketPrincipal(userId));

    if (StompCommand.SUBSCRIBE.equals(cmd)) {
      log.info("[WS] SUBSCRIBE userId={} dest={}", userId, accessor.getDestination());
      String dest = accessor.getDestination();
      if (dest == null) throw new CustomException(ChatErrorCode.SUBSCRIPTION_FORBIDDEN);

      if (USER_QUEUE_LIST.equals(dest)) { accessor.setLeaveMutable(true); return message; }

      Long roomId = extract(dest, USER_QUEUE_ROOMS);
      if (roomId == null || !isParticipant(roomId, userId))
        throw new CustomException(ChatErrorCode.SUBSCRIPTION_FORBIDDEN);

      accessor.setLeaveMutable(true);
      return message;
    }

    if (StompCommand.SEND.equals(cmd)) {
      String dest = accessor.getDestination();
      String payload = safeBody(message.getPayload());
      log.info("[WS] SEND userId={} dest={} payload={}", userId, dest, payload);

      if (dest == null) throw new CustomException(ChatErrorCode.FORBIDDEN_ROOM_ACCESS);
      Long roomId = extract(dest, APP_ROOMS_PATH);
      if (roomId == null || !isParticipant(roomId, userId))
        throw new CustomException(ChatErrorCode.FORBIDDEN_ROOM_ACCESS);

      accessor.setLeaveMutable(true);
      return message;
    }

    accessor.setLeaveMutable(true);
    return message;
  }

  private String safeBody(Object p) {
    try {
      if (p instanceof byte[] b) return new String(b);
      return String.valueOf(p);
    } catch (Exception e) { return "<unreadable>"; }
  }

  private Long resolveUserId(StompHeaderAccessor accessor) {
    Principal p = accessor.getUser();
    if (p != null) {
      try { return Long.valueOf(p.getName()); } catch (Exception ignore) {}
    }
    Map<String, Object> session = accessor.getSessionAttributes();
    if (session != null) {
      Object v = session.get("userId");
      if (v instanceof Long l) return l;
      if (v instanceof Integer i) return i.longValue();
      if (v instanceof String s) try { return Long.parseLong(s); } catch (Exception ignore) {}
    }
    throw new CustomException(ChatErrorCode.WEBSOCKET_UNAUTHORIZED);
  }

  /**
   * 해당 userId 가 roomId 의 "참여자"인지 검사한다.
   * - DB에서 방을 찾아 ChatRoom#hasParticipant(userId) 로 판단.
   * - 방이 없으면(삭제 등) false 로 처리.
   */
  private boolean isParticipant(Long roomId, Long userId) {
    return roomRepository.findById(roomId)
            .map(r -> r.hasParticipant(userId))
            .orElse(false);
  }

  /**
   * STOMP 목적지(destination)에서 정규식으로 roomId 를 추출한다.
   * - 예) destination = "/user/queue/rooms.10" , pattern = USER_QUEUE_ROOMS  →  10
   * - 예) destination = "/app/chat/rooms/10/messages" , pattern = APP_ROOMS_PATH  →  10
   * - m.matches() 를 사용하는 이유:
   *   전체 문자열이 패턴과 일치하는지 확인하여 오탐을 줄이기 위함(부분 일치보다 안전).
   */
  private Long extract(String destination, Pattern pattern) {
    if (destination == null) return null;
    Matcher m = pattern.matcher(destination);
    return m.matches() ? Long.valueOf(m.group(1)) : null;
  }

}
