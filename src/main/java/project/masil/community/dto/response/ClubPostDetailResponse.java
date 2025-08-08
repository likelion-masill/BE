package project.masil.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "ClubPostDetailResponse", description = "소모임 게시글 상세 조회 결과를 담는 응답")
public class ClubPostDetailResponse {

  @Schema(description = "소모임 ID", example = "3")
  private Long clubId;
  
  @Schema(description = "작성자 ID", example = "5")
  private Long authorId;

  @Schema(description = "작성자 이름", example = "성북구 청년봉사자")
  private String authorName;

  @Schema(description = "작성자 프로필 이미지 URL", example = "https://my-event01.jpg")
  private String authorImg;

  @Schema(description = "소모임 게시글 제목", example = "성북 청년의 날 행사 소모임")
  private String title;

  @Schema(description = "소모임 장소", example = "서울특별시 성북구 서경로 124")
  private String location;

  @Schema(description = "소모임 시작 시간", example = "2025-08-06T14:30:00")
  private String startAt;

  @Schema(description = "게시글 내용", example = "ENFP 환영!!\n심심하신분 편하게 오셔서 같이 행사 즐겨요~")
  private String content;

  @Schema(description = "관심목록 수", example = "129")
  private Long favoriteCount;

  @Schema(description = "댓글 수", example = "34")
  private Long commentCount;

  @Schema(description = "조회 수", example = "351")
  private Long viewCount;

  @Schema(description = "이미지 URL 목록", example = "[\"https://my-event01.jpg\", \"https://my-event02.jpg\"]")
  private String[] images;
}
