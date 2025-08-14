package project.masil.mypage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerVerifyRequest {

  @Schema(description = "대표자 성명", example = "홍길동")
  @NotBlank(message = "대표자 성명은 필수입니다.")
  private String businessName;

  @Schema(description = "개업일자(YYYYMMDD)", example = "20250101")
  @NotBlank(message = "개업일자는 필수입니다.")
  // 연-월-일 형식(YYYYMMDD)  — 기본 유효성. (월/일 조합 상세검증은 서비스단에서 LocalDate 파싱으로 최종 체크 권장)
  @Pattern(
      regexp = "^(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])$",
      message = "개업일자는 YYYYMMDD 형식이어야 합니다."
  )
  private String openingDate;

  @Schema(description = "사업자등록번호(10자리 숫자)", example = "1234567890")
  @NotBlank(message = "사업자등록번호는 필수입니다.")
  @Pattern(regexp = "^\\d{10}$", message = "사업자등록번호는 10자리 숫자여야 합니다.")
  private String businessNumber;
}