package project.masil.mypage.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import project.masil.global.exception.model.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum MyPageErrorCode implements BaseErrorCode {
  UNKNOWN_POST_TYPE("MYPAGE_POST_TYPE_400", "알 수 없는 게시글 타입입니다.", HttpStatus.BAD_REQUEST),
  OWNER_VERIFICATION_FAILED("MYPAGE_OWNER_VERIFICATION_400", "사업자 정보 조회에 실패했습니다.",
      HttpStatus.BAD_REQUEST),
  OWNER_VERIFICATION_API_ERROR("MYPAGE_OWNER_VERIFICATION_API_500",
      "사업자 정보 조회 API 호출 중 오류가 발생했습니다.",
      HttpStatus.INTERNAL_SERVER_ERROR),
  OWNER_ALREADY_VERIFIED("MYPAGE_OWNER_ALREADY_VERIFIED_400",
      "이미 사업자 번호가 인증된 상태입니다.",
      HttpStatus.BAD_REQUEST),

  OWNER_ALREADY_REGISTERED("MYPAGE_OWNER_ALREADY_REGISTERED_400",
      "이미 등록된 사업자번호입니다.",
      HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
