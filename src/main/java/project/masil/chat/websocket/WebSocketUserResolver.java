package project.masil.chat.websocket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import project.masil.chat.exception.ChatErrorCode;
import project.masil.global.exception.CustomException;
import project.masil.global.jwt.JwtProvider;
import project.masil.user.entity.User;
import project.masil.user.repository.UserRepository;

/**
 * [WsUserResolver]
 * - WebSocket(STOMP) 연결 시 들어온 STOMP 헤더에서 로그인한 사용자 ID를 꺼내옵니다.
 *
 * - STOMP CONNECT 헤더의 Authorization: Bearer <JWT> 를 읽어
 *   → 토큰 검증 → 이메일(subject) 추출 → DB에서 사용자 조회 → userId 반환
 *
 * 전제: JwtProvider.createAccessToken() 이 subject에 email을 넣고 있음.
 *    (현재 네 JwtProvider는 subject=email 구조로 동작)
 *
 * 클라이언트(예: StompJS) 연결 예:
 *   client.connect(
 *     { Authorization: 'Bearer ' + accessToken },
 *     onConnect,
 *     onError
 *   );
 */
@Component
@RequiredArgsConstructor
public class WebSocketUserResolver {

  private static final Pattern BEARER = Pattern.compile("^Bearer\\s+(.+)$", Pattern.CASE_INSENSITIVE);

  private final JwtProvider jwtProvider;
  private final UserRepository userRepository;

  public Long resolve(StompHeaderAccessor accessor) {
    // 1) Authorization 헤더 추출
    String auth = accessor.getFirstNativeHeader("Authorization");
    if (auth == null || !auth.toLowerCase().startsWith("bearer ")) {
      throw new CustomException(ChatErrorCode.WEBSOCKET_UNAUTHORIZED);
    }

    // 2) Bearer 토큰 파싱
    Matcher m = BEARER.matcher(auth.trim());
    if (!m.find()) {
      throw new CustomException(ChatErrorCode.WEBSOCKET_UNAUTHORIZED);
    }
    String token = m.group(1).trim();

    // 3) 토큰 유효성 검증
    jwtProvider.validateToken(token);

    // 4) 이메일 추출
    String email = jwtProvider.extractEmail(token);
    if (email == null || email.isBlank()) {
      throw new CustomException(ChatErrorCode.WEBSOCKET_UNAUTHORIZED);
    }

    // 5) 사용자 조회
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ChatErrorCode.WEBSOCKET_UNAUTHORIZED));

    return user.getId();
  }
}
