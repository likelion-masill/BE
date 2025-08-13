package project.masil.community.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import project.masil.global.exception.model.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum PostErrorCode implements BaseErrorCode {
  POST_NOT_FOUND("POST_404", "게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INVALID_POST_TYPE("POST_400", "잘못된 게시글 유형입니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;

}
