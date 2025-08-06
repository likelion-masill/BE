package project.masil.global.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import project.masil.global.exception.model.BaseErrorCode;
import project.masil.global.response.BaseResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 커스텀 예외
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<BaseResponse<Object>> handleCustomException(CustomException ex) {
    BaseErrorCode errorCode = ex.getErrorCode();
    log.error("Custom 오류 발생: {}", ex.getMessage());
    return ResponseEntity
        .status(errorCode.getStatus())
        .body(BaseResponse.error(errorCode.getStatus().value(), ex.getMessage()));
  }

  // Validation 실패
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseResponse<Object>> handleValidationException(
      MethodArgumentNotValidException ex) {
    String errorMessages =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> String.format("[%s] %s", e.getField(), e.getDefaultMessage()))
            .collect(Collectors.joining(" / "));
    log.warn("Validation 오류 발생: {}", errorMessages);
    return ResponseEntity.badRequest().body(BaseResponse.error(400, errorMessages));
  }

  // JSON 파싱 오류 (JSON 파싱 실패, 요청 본문이 비었거나 잘못된 경우)
  // @RequestBody 잘못된 JSON
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<BaseResponse<Object>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {
    log.warn("요청 바디 JSON 파싱 오류: {}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(BaseResponse.error(400, "요청 형식이 잘못되었습니다. JSON을 확인해주세요."));
  }

  // 필수 요청 파라미터 누락 (ex: @RequestParam 빠짐)
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<BaseResponse<Object>> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException ex) {
    log.warn("필수 요청 파라미터 누락: {}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(BaseResponse.error(400, "필수 파라미터 누락: " + ex.getParameterName()));
  }

  // 제약 조건 위반 (ex: @Validated + @RequestParam 유효성 실패)
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<BaseResponse<Object>> handleConstraintViolationException(
      ConstraintViolationException ex) {
    String message = ex.getConstraintViolations().iterator().next().getMessage(); // 첫 메시지만
    log.warn("제약 조건 위반: {}", message);
    return ResponseEntity.badRequest()
        .body(BaseResponse.error(400, message));
  }


  // 예상치 못한 예외
  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<Object>> handleException(Exception ex) {
    log.error("Server 오류 발생: ", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(BaseResponse.error(500, "예상치 못한 서버 오류가 발생했습니다."));
  }
}