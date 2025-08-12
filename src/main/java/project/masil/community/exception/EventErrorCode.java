package project.masil.community.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import project.masil.global.exception.model.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum EventErrorCode implements BaseErrorCode {

  EVENT_NOT_FOUND("EVENT_4041", "해당 이벤트는 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  EVENT_FORBIDDEN("EVENT_403", "해당 이벤트를 작성자가 아니므로 권한이 없습니다", HttpStatus.FORBIDDEN);

  private final String code;
  private final String message;
  private final HttpStatus status;

}
