package project.masil.chat.websocket;

import java.security.Principal;
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

  /** 방 참여자 여부를 검사하기 위해 DB에서 방을 조회 */
  private final ChatRoomRepository roomRepository;

  /** STOMP 헤더(Authorization 등)에서 userId를 파싱하는 도우미*/
  private final WebSocketUserResolver webSocketUserResolver;

  /**
   * [구독 경로 정규식]  "/user/queue/rooms.{roomId}"
   *  - 예) /user/queue/rooms.10  →  roomId=10
   *  - 개인 큐만 사용하므로 topic 브로드캐스트는 필요 없음.
   */
  private static final Pattern USER_QUEUE_ROOMS = Pattern.compile(".*/queue/rooms\\.(\\d+)");

  /**
   * [전송 경로 정규식]  "/app/chat/rooms/{roomId}/..."
   *  - 예) /app/chat/rooms/10/messages  →  roomId=10
   *  - 클라이언트가 서버로 메시지를 보낼 때 사용하는 경로 패턴.
   */
  private static final Pattern APP_ROOMS_PATH   = Pattern.compile(".*/rooms/(\\d+)/.*");


  /**
   * INBOUND 프레임이 올 때마다 호출된다.
   * 여기서 STOMP 헤더를 읽고, 프레임의 타입/명령에 따라 인증/권한을 검사한다.
   * @param message
   * @param channel
   * @return
   */
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    //STOMP 헤더에 접근하기 위한 래퍼
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    // 1) CONNECT 단계: 최초 핸드셰이크 완료 후 STOMP CONNECT 프레임이 들어온다.
    //    - 이때 토큰을 파싱해서 userId를 구하고, 세션 Principal 로 심어둔다.
    //    - 이후 프레임에서는 accessor.getUser() 로 userId 에 접근할 수 있다.
    //    - SimpMessageType.CONNECT 와 StompCommand.CONNECT 둘 다 체크하는 이유:
    //      구현체/상황에 따라 타입/커맨드로 구분되는 경우가 있기 때문(안전하게 모두 허용).
    if (accessor.getMessageType() == SimpMessageType.CONNECT || StompCommand.CONNECT.equals(
        accessor.getCommand())) {
      Long userId = webSocketUserResolver.resolve(
          accessor);     // 예: Authorization / X-User-Id 헤더에서 파싱
      accessor.setUser(new WebSocketPrincipal(userId));          // Principal.name = "userId" 로 저장
      log.info("[WS] CONNECT userId={}", userId);
      return message;                              // CONNECT는 여기서 종료(권한 체크 대상 아님)
    }

    // 2) CONNECT 이후의 모든 프레임(SUBSCRIBE / SEND 등)은 반드시 Principal 이 있어야 한다.
    //    - 세션에 사용자 정보가 없다면 인증 실패로 간주.
    Principal principal = accessor.getUser();
    if (principal == null) {
      throw new CustomException(ChatErrorCode.WEBSOCKET_UNAUTHORIZED);
    }


    // Principal.name 에는 문자열 형태의 userId를 넣어두었음
    Long userId = Long.valueOf(principal.getName());

    // 3) SUBSCRIBE 권한 체크
    //    - 구독 대상이 "/user/queue/rooms.{roomId}"라면,
    //      해당 roomId의 "참여자"만 구독을 허용한다.
    //    - 개인 큐는 원칙적으로 타인이 수신 못하지만(서버가 보내주지 않음),
    //      형식상/보안상 구독 자체를 제한하는 편이 더 안전하다.
    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      log.debug("SUBSCRIBE dest={}, userId={}", accessor.getDestination(), userId);
      Long roomId = extract(accessor.getDestination(), USER_QUEUE_ROOMS); // 경로에서 roomId 뽑기
      if (roomId != null && !isParticipant(roomId, userId)) {
        //roomId == null이면 우리 패턴(/user/queue/rooms.{roomId})이 아닌 다른 구독이거나 잘못된 경로라서 권한체크 자체를 스킵해야 함.
        throw new CustomException(ChatErrorCode.SUBSCRIPTION_FORBIDDEN); // 방 참가자 아님 → 구독 금지
      }
    }

    // 4) SEND 권한 체크
    //    - 전송 대상이 "/app/chat/rooms/{roomId}/..." 라면,
    //      해당 roomId의 "참여자"만 메시지 전송을 허용한다.
    if (StompCommand.SEND.equals(accessor.getCommand())) {
      log.debug("SUBSCRIBE dest={}, userId={}", accessor.getDestination(), userId);
      Long roomId = extract(accessor.getDestination(), APP_ROOMS_PATH); // 경로에서 roomId 뽑기
      if (roomId != null && !isParticipant(roomId, userId)) {
        throw new CustomException(ChatErrorCode.FORBIDDEN_ROOM_ACCESS); // 방 참가자 아님 → 전송 금지
      }
    }

    // 5) 나머지 프레임(예: DISCONNECT)은 별도 로직 없이 통과
    return message;
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
    return m.matches() ? Long.valueOf(m.group(1)) : null; //그룹 1번이 roomId
  }


}
