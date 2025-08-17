package project.masil.community.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "ClubPostDetailResponse", description = "소모임 게시글 상세 조회 결과를 담는 응답")
public class ClubPostDetailResponse {

  @Schema(description = "소모임 ID", example = "3")
  private Long clubId;

  @Schema(description = "작성자 이름", example = "성북구 청년봉사자")
  private String username;

  @Schema(description = "작성자 프로필 이미지 URL", example = "https://my-event01.jpg")
  private String userImage;

  @Schema(description = "소모임 게시글 제목", example = "성북 청년의 날 행사 소모임")
  private String title;

  @Schema(description = "소모임 장소", example = "서울특별시 성북구 서경로 124")
  private String location;

  @Schema(description = "소모임 시작 시간", example = "2025-08-06T14:30:00")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime startAt;

  @Schema(description = "게시글 내용", example = "ENFP 환영!!\n심심하신분 편하게 오셔서 같이 행사 즐겨요~")
  private String content;

  @Schema(description = "관심목록 수", example = "129")
  private int favoriteCount;

  @Schema(description = "댓글 수", example = "34")
  private int commentCount;

  @Schema(description = "이미지 URL 목록", example = "[\"https://my-event01.jpg\", \"https://my-event02.jpg\"]")
  private List<String> images;

  @Schema(description = "게시글 생성 시간", example = "2025-08-01T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "좋아요 여부")
  private boolean isLiked;
}
