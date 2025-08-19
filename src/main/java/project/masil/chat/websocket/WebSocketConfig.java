package project.masil.chat.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * [WebSocketConfig = 1:1 DM 전용 설정]
 *
 * 1) 연결 엔드포인트
 *    - 클라이언트는 처음에 '/websocket/chat' 으로 접속(SockJS 폴백 허용)
 *
 * 2) 클라이언트→서버 전송 prefix
 *    - '/app/chat' 로 시작하는 목적지로 보내면 @MessageMapping 이 받는다
 *    - 예) 클라 SEND: /app/chat/rooms/10/messages → @MessageMapping("/rooms/{roomId}/messages")
 *
 * 3) 서버→클라이언트 푸시(구독) = 개인 큐만 사용
 *    - 1:1만 지원하므로 '/topic' 브로드캐스트는 제거
 *    - 개인 큐 prefix: '/user/queue/**'
 *      · 서버에서 convertAndSendToUser("<userId>", "/queue/rooms.10", payload)
 *      · 클라는 '/user/queue/rooms.10' 을 구독하면 자기 것만 받는다
 *
 * 4) INBOUND 인터셉터로 인증/권한 검사
 *    - CONNECT: 토큰에서 userId 추출하여 세션 Principal 에 저장
 *    - SUBSCRIBE: '/user/queue/rooms.{roomId}' 구독 요청이면 방 참가자인지 확인
 *    - SEND: '/app/chat/rooms/{roomId}/...' 전송 요청이면 방 참가자인지 확인
 */
@Configuration
@EnableWebSocketMessageBroker // STOMP 메시징 브로커(서버가 클라이언트에게 메시지를 배포하는 역할) 기능 활성화 (@MessageMapping 사용 가능)
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final StompAuthChannelInterceptor stompAuthChannelInterceptor;
  private final JwtHandshakeHandler jwtHandshakeHandler;

  /**
   * [1] STOMP 엔드포인트 등록 (핸드셰이크 입구)
   *
   * - 클라이언트는 이 주소로 최초 연결을 시도합니다.
   *   예) SockJS: new SockJS("/websocket/chat")
   *
   * - withSockJS():
   *   브라우저가 순수 WebSocket을 지원하지 않아도,
   *   폴백(롱폴링 등)으로 동작시키기 위해 SockJS를 허용합니다.
   *
   * - setAllowedOriginPatterns("*"):
   *   CORS 허용. 운영에선 보안상 특정 도메인만 허용하도록 바꾸기
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/websocket/chat")
        .setHandshakeHandler(jwtHandshakeHandler) // ★ 추가
        // ✅ credentials 허용 시 "*" 금지 → 명시적으로 허용 오리진 나열
        .setAllowedOriginPatterns("*")
//        .setAllowedOriginPatterns("http://localhost:5173")
        .withSockJS();
  }

  /**
   * [2] 메시지 브로커 설정 (라우팅 규칙)
   *
   * ─ 클라 → 서버로 "보내는" 경로 접두사: /app/chat
   *   - 클라는 "/app/chat/rooms/{roomId}/messages"로 SEND
   *   - 서버는 @MessageMapping("/rooms/{roomId}/messages") 로 수신
   *
   * ─ 서버 → 클라로 "푸시"하는 경로:
   *   - 1:1 전용이므로 브로드캐스트(/topic)는 제거하고,
   *     개인 큐(/user/queue/**)만 쓴다.
   *
   *   - enableSimpleBroker("/queue"):
   *     브로커가 관리하는 기본 목적지 접두사를 "/queue"로 선언.
   *     convertAndSendToUser(..., "/queue/...", ...) 를 쓸 수 있게 해줌.
   *
   *   - setUserDestinationPrefix("/user"):
   *     "개인 큐"를 구독할 때 클라는 "/user/..." 로 구독합니다.
   *     예) 서버: convertAndSendToUser("42", "/queue/rooms.10", payload)
   *         클라:  /user/queue/rooms.10  ← 요걸 구독
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // 1. 클라 → 서버 전송 prefix (서버의 @MessageMapping 과 연결됨)
    registry.setApplicationDestinationPrefixes("/app/chat");

    // 2. 서버 -> 클라 푸시 경로 : 개인 큐만 사용 (브로드캐스트 /topic은 사용하지 않음)
    registry.enableSimpleBroker("/queue");

    // 3. 사용자 개인 목적지 접두사
    registry.setUserDestinationPrefix("/user");
  }

  /**
   * [3] INBOUND 채널 인터셉터 적용
   *
   * - 클라가 보내는 모든 STOMP 프레임(연결/구독/전송)에 대해
   *   stompAuthChannelInterceptor가 인증·권한을 검사합니다.
   *
   *   CONNECT  : 토큰에서 userId 추출 → 세션 Principal 저장
   *   SUBSCRIBE: '/user/queue/rooms.{roomId}' 구독 요청 시 방 참가자만 허용
   *   SEND     : '/app/chat/rooms/{roomId}/...' 전송 요청 시 방 참가자만 허용
   */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(stompAuthChannelInterceptor);
  }



}
