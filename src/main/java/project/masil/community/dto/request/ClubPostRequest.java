package project.masil.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(title = "ClubPostRequest", description = "소모임 게시글 생성/수정을 위한 데이터 전송")
public class ClubPostRequest {

  @NotBlank(message = "소모임 게시글 제목은 필수 항목입니다.")
  @Schema(description = "소모임 게시글 제목", example = "성북 청년의 날 행사 소모임")
  private String title;

  @NotBlank(message = "소모임 장소는 필수 항목입니다.")
  @Schema(description = "소모임 장소", example = "서울특별시 성북구 서경로 124")
  private String location;

  @NotNull(message = "소모임 시작 시간은 필수 항목입니다.")
  @Schema(
      description = "소모임 시작 시간",
      type = "string",
      example = "2025-08-06T14:30:00"
  )
  private LocalDateTime startAt;

  @NotBlank(message = "게시글 내용은 필수 항목입니다.")
  @Schema(description = "게시글 내용", example = "ENFP 환영!!\n심심하신분 편하게 오셔서 같이 행사 즐겨요~")
  private String content;

}
