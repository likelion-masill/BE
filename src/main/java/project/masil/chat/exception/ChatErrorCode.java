package project.masil.chat.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import project.masil.global.exception.model.BaseErrorCode;

/**
 * [ChatErrorCode]
 * - 채팅 도메인 전용 표준 에러 코드/메시지/HTTP 상태 정의
 * - 사용 예: throw new CustomException(ChatErrorCode.ROOM_NOT_FOUND)
 *
 * 규칙
 * - 코드 포맷: CHAT-<HTTP>-<키>
 *   · 400 Bad Request  : 잘못된 입력(자기자신 채팅, 컨텍스트/타겟 불일치, 빈 메시지, 길이 초과, 미지원 타입 등)
 *   · 401 Unauthorized : WebSocket 연결/프레임에 인증 정보 없음
 *   · 403 Forbidden    : 방 참여자가 아닌 접근/구독/전송
 *   · 404 Not Found    : 리소스(방 등) 없음
 *   · 409 Conflict     : 동시성/유니크 충돌
 */
@Getter
@AllArgsConstructor
public enum ChatErrorCode implements BaseErrorCode {

  /** 자기 자신에게 채팅 시도 */
  SELF_CHAT_NOT_ALLOWED("CHAT-400-SELF", "자기 자신과는 채팅할 수 없습니다.", HttpStatus.BAD_REQUEST),

  /** 컨텍스트 ID에 따른 사용자 ID와 targetUserId가 일치하지 않음 */
  CONTEXT_TARGET_MISMATCH("CHAT-400-CONTEXT", "컨텍스트의 사용자 ID와 요청 targetUserId가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

  /** 채팅방 미존재 */
  ROOM_NOT_FOUND("CHAT-404-ROOM", "채팅방이 존재하지 않습니다.", HttpStatus.NOT_FOUND),

  /** 방 참여자가 아닌 사용자의 접근 */
  FORBIDDEN_ROOM_ACCESS("CHAT-403-ROOM", "해당 채팅방에 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

  /** 빈 메시지 전송 */
  MESSAGE_EMPTY("CHAT-400-MSG-EMPTY", "메시지 내용은 비어 있을 수 없습니다.", HttpStatus.BAD_REQUEST),

  /** 메시지 길이 초과 */
  MESSAGE_TOO_LONG("CHAT-400-MSG-LEN", "메시지 길이는 1000자를 초과할 수 없습니다.", HttpStatus.BAD_REQUEST),

  /** 지원하지 않는 컨텍스트 타입 */
  INVALID_CONTEXT_TYPE("CHAT-400-CTX-TYPE", "지원하지 않는 컨텍스트 타입입니다.", HttpStatus.BAD_REQUEST),

  /**
   * 동시성으로 유니크 제약 충돌 후 재조회까지 실패한, 매우 예외적인 상황
   * - 보통은 재조회로 해결되므로 여기 도달하면 추가 점검 필요
   */
  ROOM_CREATE_CONFLICT("CHAT-409-ROOM", "채팅방 생성 중 충돌이 발생했습니다. 잠시 후 다시 시도해 주세요.", HttpStatus.CONFLICT),

  /* ───── WebSocket/STOMP 관련 ───── */
  WEBSOCKET_UNAUTHORIZED("CHAT-401-WS", "WebSocket 인증 정보가 없습니다.", HttpStatus.UNAUTHORIZED),
  SUBSCRIPTION_FORBIDDEN("CHAT-403-SUB", "구독 권한이 없습니다(방 참여자가 아님).", HttpStatus.FORBIDDEN);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
