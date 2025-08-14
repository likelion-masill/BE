package project.masil.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(title = "RegionUpdateRequest", description = "지역 업데이트 요청 DTO")
public class RegionUpdateRequest {

  @NotBlank(message = "지역 ID는 필수입니다.")
  @Schema(description = "지역 ID", example = "5")
  private Long regionId;

}
