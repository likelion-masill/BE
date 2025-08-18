package project.masil.chat.websocket;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;
import project.masil.global.jwt.JwtProvider;
import project.masil.user.entity.User;
import project.masil.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

  private final JwtProvider jwtProvider;
  private final UserRepository userRepository;

  @Override
  protected Principal determineUser(ServerHttpRequest request,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes) {
    // 1) 쿼리스트링 access_token 우선
    String token = UriComponentsBuilder.fromUri(request.getURI())
        .build()
        .getQueryParams()
        .getFirst("access_token");

    // 2) 없으면 헤더 Authorization: Bearer xxx 시도 (프록시/서버 환경 따라 올 수도 있음)
    if (token == null && request instanceof ServletServerHttpRequest sshr) {
      List<String> auths = sshr.getServletRequest().getHeaders("Authorization") != null
          ? java.util.Collections.list(sshr.getServletRequest().getHeaders("Authorization"))
          : java.util.List.of();
      for (String a : auths) {
        if (a != null && a.toLowerCase().startsWith("bearer ")) {
          token = a.substring(7).trim();
          break;
        }
      }
    }

    if (token == null || token.isBlank()) {
      // 핸드셰이크에서 못 찾으면 기본 처리(=익명). 그럼 user-registry에 안 잡힘
      return super.determineUser(request, wsHandler, attributes);
    }

    // JWT 검증 및 사용자 조회
    jwtProvider.validateToken(token);
    String email = jwtProvider.extractEmail(token);
    User user = userRepository.findByEmail(email).orElseThrow();

    // 세션 Principal = userId (user-queue 라우팅의 key)
    return new WebSocketPrincipal(user.getId());
  }
}
