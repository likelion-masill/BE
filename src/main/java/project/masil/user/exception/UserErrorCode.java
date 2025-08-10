package project.masil.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import project.masil.global.exception.model.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
  EMAIL_ALREADY_EXISTS("USER_4001", "이미 존재하는 사용자 이메일입니다.", HttpStatus.BAD_REQUEST),
  PASSWORD_REQUIRED("USER_4002", "비밀번호는 필수입니다.", HttpStatus.BAD_REQUEST),
  USER_NOT_FOUND("USER_4003", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
  USERNAME_ALREADY_EXISTS("USER_4004", "이미 존재하는 사용자 닉네임입니다.", HttpStatus.BAD_REQUEST),
  PHONE_NUMBER_NOT_VERIFIED("USER_4005", "휴대폰 번호 인증이 완료되지 않았습니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;
}