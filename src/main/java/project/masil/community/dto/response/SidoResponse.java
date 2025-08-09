package project.masil.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
@Schema(title = "광역지방자치단체(시/도) 응답 DTO", description = "단일 광역지방자치단체(시/도) 정보를 담는 응답 형식")
public class SidoResponse {

  @Schema(description = "광역지방자치단체(시/도) 명칭", example = "서울특별시")
  private String sido;

}
