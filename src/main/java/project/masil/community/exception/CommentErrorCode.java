package project.masil.community.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import project.masil.global.exception.model.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum CommentErrorCode implements BaseErrorCode {
  COMMENT_NOT_FOUND("COMMENT_404", "존재하지 않는 댓글입니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;

}