package project.masil.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import project.masil.user.entity.Role;

@Getter
@Builder
@Schema(title = "LoginResponse DTO", description = "사용자 로그인에 대한 응답 반환")
public class LoginResponse {

  @Schema(description = "사용자 Access Token")
  private String accessToken;


  @Schema(description = "사용자 ID", example = "1")
  private Long userId;

  @Schema(description = "사용자 아이디 또는 이메일", example = "jhjk1234@gmail.com")
  private String email;

  @Schema(description = "사용자가 설정한 지역의 고유 ID", example = "1")
  private Long regionId;

  @Schema(description = "사용자 권한", example = "USER")
  private Role role;

  @Schema(description = "사용자 Access Token 만료 시간", example = "1800000")
  private Long expirationTime;

}
