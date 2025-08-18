package project.masil.chat.websocket;

import java.security.Principal;
import java.util.Objects;
import project.masil.global.exception.CustomException;
import project.masil.user.exception.UserErrorCode;

/**
 * [WebSocketPrincipal]
 * CONNECT에서 인증된 userId를 세션 Principal로 보관.
 */
public final class WebSocketPrincipal implements Principal {

  private final Long userId;

  public WebSocketPrincipal(Long userId) {
    if (userId == null) throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    this.userId = userId;
  }

  /** Spring 메시징 내부에서 문자열 식별자로 사용됨 */
  @Override
  public String getName() {
    return String.valueOf(userId);
  }

  public Long getUserId() {
    return userId;
  }
}