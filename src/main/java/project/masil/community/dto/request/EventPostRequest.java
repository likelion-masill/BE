package project.masil.community.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import project.masil.community.enums.EventType;

@Getter
@Setter
@Builder
@Schema(title = "EventPostRequest : 이벤트 요청 DTO")
public class EventPostRequest {

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

  @NotNull(message = "시작 날짜는 필수입니다.")
  @Schema(description = "시작 날짜", example = "2025-08-08T10:00:00")
  @JsonFormat(shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss",
      timezone = "Asia/Seoul")
  private LocalDateTime startDate;

  @NotNull(message = "종료 날짜는 필수입니다.")
  @Schema(description = "종료 날짜", example = "2025-09-01T18:00:00")
  @JsonFormat(shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss",
      timezone = "Asia/Seoul"
  )
  private LocalDateTime endDate;

}
