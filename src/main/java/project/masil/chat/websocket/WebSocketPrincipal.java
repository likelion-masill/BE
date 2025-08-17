package project.masil.chat.websocket;

import java.security.Principal;
import java.util.Objects;
import project.masil.global.exception.CustomException;
import project.masil.user.exception.UserErrorCode;

/**
 * [WebSocketPrincipal]
 *
 * - STOMP CONNECT 시점에 인증을 통과한 사용자 식별자(userId)를 세션에 실어두기 위한 Principal 구현체.
 * - 이후 @MessageMapping 핸들러에서 Principal.getName() 또는 getUserId()로 userId를 꺼내 쓸 수 있다.
 * => 이제 인터셉터에서 new WebSocketPrincipal(userId)로 세팅하면, 컨트롤러에서 안전하게 getUserId()로 꺼내 사용할 수 있어요.
 *
 * 사용 예)
 *   // 인터셉터에서 세팅
 *   accessor.setUser(new WebSocketPrincipal(userId));
 *
 *   // 컨트롤러에서 사용
 *   @MessageMapping("/rooms/{roomId}/messages")
 *   public void send(@DestinationVariable Long roomId, SendMessageRequest req, Principal principal) {
 *       Long me = ((WebSocketPrincipal) principal).getUserId(); // 또는 Long.valueOf(principal.getName())
 *       ...
 *   }
 */
public final class WebSocketPrincipal implements Principal {

  private final Long userId;

  public WebSocketPrincipal(Long userId) {
    if (userId == null) throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    this.userId = userId;
  }

  /** Spring 메시징 내부에서 문자열로 쓰이므로 userId를 문자열로 반환 */
  @Override
  public String getName() {
    return String.valueOf(userId);
  }

  /** 타입 안전하게 userId가 필요할 때 사용 */
  public Long getUserId() {
    return userId;
  }
}
