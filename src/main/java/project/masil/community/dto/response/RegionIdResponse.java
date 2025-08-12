package project.masil.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
@Schema(title = "지역 ID 응답 DTO", description = "지역 id 정보를 담는 응답 형식")
public class RegionIdResponse {

  @Schema(description = "지역 ID", example = "141")
  private Long regionId;

}
