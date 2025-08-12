package project.masil.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(title = "NicknameUpdateRequest DTO", description = "사용자 닉네임 변경 요청 DTO")
public class NicknameUpdateRequest {

  @NotBlank
  @Schema(description = "변경할 닉네임", example = "새로운닉네임")
  private String nickname;

}
