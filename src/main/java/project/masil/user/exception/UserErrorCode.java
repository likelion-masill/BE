package project.masil.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import project.masil.global.exception.model.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
  USERNAME_ALREADY_EXISTS("USER_4001", "이미 존재하는 사용자 아이디입니다.", HttpStatus.BAD_REQUEST),
  PASSWORD_REQUIRED("USER_4002", "비밀번호는 필수입니다.", HttpStatus.BAD_REQUEST),
  USER_NOT_FOUND("USER_4003", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
  INTRODUCTION_ALREADY_EXISTS("USER_4004", "이미 자기소개가 등록되어 있습니다.", HttpStatus.BAD_REQUEST),
  INTRODUCTION_NOT_FOUND("USER_4005", "자기소개가 등록되어 있지 않습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}