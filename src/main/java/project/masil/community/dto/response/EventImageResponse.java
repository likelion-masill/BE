package project.masil.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "EventImageResponse : 이벤트 이미지 응답 DTO")
public class EventImageResponse {

  @Schema(description = "이미지 순서", example = "1")
  private int sequence;

  @Schema(description = "이미지 URL", example = "https://my-event01.jpg")
  private String imageUrl;
}