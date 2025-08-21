package project.masil.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileImageUpdateResponse {

  @Schema(description = "프로필 이미지 URL", example = "false")
  private String profileImageUrl;

}
