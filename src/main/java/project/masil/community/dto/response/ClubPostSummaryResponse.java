package project.masil.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "ClubPostSummaryResponse", description = "소모임 게시글 목록 조회 시 반환되는 간략 정보 응답")
public class ClubPostSummaryResponse {

  @Schema(description = "소모임 ID", example = "3")
  private Long clubId;

  @Schema(description = "소모임 게시글 제목", example = "성북 청년의 날 행사 소모임")
  private String title;

  @Schema(description = "소모임 장소", example = "서울특별시 성북구 서경로 124")
  private String location;

  @Schema(description = "소모임 시작 시간", example = "2025-08-06T14:30:00")
  private String startAt;

  @Schema(description = "관심목록 수", example = "129")
  private Long favoriteCount;

  @Schema(description = "댓글 수", example = "34")
  private Long commentCount;

  @Schema(description = "커버 이미지 URL", example = "https://my-event01.jpg")
  private String coverImage;


}
