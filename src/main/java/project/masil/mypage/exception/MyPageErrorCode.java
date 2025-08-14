package project.masil.mypage.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import project.masil.global.exception.model.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum MyPageErrorCode implements BaseErrorCode {
  UNKNOWN_POST_TYPE("MYPAGE_POST_TYPE_400", "알 수 없는 게시글 타입입니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
