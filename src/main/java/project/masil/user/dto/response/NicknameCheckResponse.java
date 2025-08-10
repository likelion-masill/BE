package project.masil.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NicknameCheckResponse {

  @Schema(description = "사용자 별명", example = "비쿠")
  private String nickname;

  @Schema(description = "중복 여부", example = "false")
  private boolean duplicate;

}
