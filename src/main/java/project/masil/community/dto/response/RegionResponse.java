package project.masil.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
@Schema(title = "지역 응답 DTO", description = "지역 정보를 담는 응답 형식")
public class RegionResponse {

  @Schema(description = "지역 ID", example = "1")
  private Long regionId;

  @Schema(description = "광역지방자치단체 이름", example = "서울특별시")
  private String sido;

  @Schema(description = "기초지방자치단체 이름", example = "종로구")
  private String sigungu;

}
