package project.masil.community.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import project.masil.global.exception.model.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum EventErrorCode implements BaseErrorCode {

  EVENT_NOT_FOUND("EVENT_4041", "해당 이벤트는 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  EVENT_FORBIDDEN("EVENT_403", "해당 이벤트를 작성자가 아니므로 권한이 없습니다", HttpStatus.FORBIDDEN),
  IMAGE_REQUIRED("EVENT_4001", "이미지 파일은 최소 1장 이상 필요합니다.", HttpStatus.BAD_REQUEST),
  EMPTY_IMAGE("EVENT_4002", "비어있는 이미지가 포함되어 있습니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;

}
