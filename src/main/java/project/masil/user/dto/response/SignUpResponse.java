package project.masil.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "SignUpResponse DTO", description = "사용자 회원가입에 대한 응답 반환")
public class SignUpResponse {

  @Schema(description = "회원가입된 사용자 ID", example = "1")
  private Long userId;

  @Schema(description = "닉네임", example = "비쿠")
  private String nickname;

  @Schema(description = "회원가입된 사용자 아이디", example = "jhjk1234@gmail.com")
  private String email;

  @Schema(description = "지역 ID", example = "1")
  private Long regionId;

}