package project.masil.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

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
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    registry.addEndpoint("/websocket/chat").setAllowedOriginPatterns("*").withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app/chat");
    registry.enableSimpleBroker("/queue");
    registry.setUserDestinationPrefix("/user");
  }

  // ★ STOMP로 들어오고 나가는 payload를 변환할 컨버터 등록
  @Override
  public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
    messageConverters.add(stompJacksonMessageConverter());
    return false; // 기본 컨버터들도 유지
  }

  @Bean
  public MappingJackson2MessageConverter stompJacksonMessageConverter() {
    MappingJackson2MessageConverter conv = new MappingJackson2MessageConverter();
    conv.setObjectMapper(stompObjectMapper());
    conv.setSerializedPayloadClass(String.class);
    conv.setContentTypeResolver(msg -> MimeTypeUtils.APPLICATION_JSON);
    return conv;
  }

  @Bean
  public ObjectMapper stompObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }

  @Override
  public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
    registration.interceptors(stompAuthChannelInterceptor);
  }
}