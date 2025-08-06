package project.masil.global.exception;

import lombok.Getter;
import project.masil.global.exception.model.BaseErrorCode;

@Getter
public class CustomException extends RuntimeException {

  private final BaseErrorCode errorCode;

  public CustomException(BaseErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

}
