package project.masil.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
@Schema(title = "기초지방자치단체(시/군/구) 응답 DTO", description = "기초지방자치단체(시/군/구) 정보를 담는 응답 형식")
public class SigunguResponse {

  @Schema(description = "기초지방자치단체(시/군/구)", example = "성북구")
  private String sigungu;

}
