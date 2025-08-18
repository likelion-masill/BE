package project.masil.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.masil.community.enums.EventType;

@Getter
@Setter
@Builder
@Schema(title = "EventPostRequest : 이벤트 요청 DTO")
@NoArgsConstructor
@AllArgsConstructor
public class EventPostRequest {

  @NotBlank(message = "지역아이디는 필수입니다.")
  @Schema(description = "지역 아이디", example = "1")
  private Long regionId;

  @NotBlank(message = "카테고리 이름은 영어로 정확히 작성해야합니다.\n"
      + "FLEA_MARKET : 플리마켓, CULTURE_ART : 문화/예술, OUTDOOR_ACTIVITY : 야외활동, FOOD : 먹거리")
  @Schema(description = "FLEA_MARKET : 플리마켓, CULTURE_ART : 문화/예술, OUTDOOR_ACTIVITY : 야외활동, FOOD : 먹거리", example = "FLEA_MARKET")
  private EventType eventType;

  @NotBlank(message = "이벤트 제목은 필수입니다.")
  @Schema(description = "이벤트 제목", example = "마실 이벤트")
  private String title;

  @NotBlank(message = "이벤트 내용은 필수입니다.")
  @Schema(description = "이벤트 내용", example = "마실 이벤트 내용입니다.")
  private String content;

  @NotBlank(message = "이벤트 세부장소는 필수입니다.")
  @Schema(description = "이벤트 세부장소", example = "서경로 184")
  private String location;

  @NotNull(message = "시작 날짜는 필수입니다.")
  @Schema(
      description = "시작 날짜 (UTC/Z 포함 ISO 8601 형식)",
      type = "string",
      example = "2025-08-08T10:00:00"
  )
  private LocalDateTime startAt; // "Z" 포함 가능

  @NotNull(message = "종료 날짜는 필수입니다.")
  @Schema(
      description = "종료 날짜 (UTC/Z 포함 ISO 8601 형식)",
      type = "string",
      example = "2025-09-01T18:00:00"
  )
  private LocalDateTime endAt;


}
