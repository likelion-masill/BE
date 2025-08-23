package project.masil.mypage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import project.masil.community.dto.response.EventImageResponse;
import project.masil.community.enums.PostType;

@Getter
@Builder
@Schema(title = "PostResponse", description = "마이페이지 게시글 응답 DTO")
public class PostResponse {

  @Schema(description = "이벤트 게시글 ID", example = "30")
  private Long eventId;

  @Schema(description = "소모임 게시글 ID", example = "70")
  private Long clubId;

  @Schema(description = "게시글 유형", example = "EVENT")
  private PostType postType;

  @Schema(description = "이벤트 이미지 리스트")
  private List<EventImageResponse> images;

  @Schema(description = "게시글 작성자 이름", example = "성북구 청년봉사자")
  private String username;

  @Schema(description = "게시글 작성자 프로필 이미지 URL", example = "https://my-event01.jpg")
  private String userImage;

  @Schema(description = "게시글 제목", example = "성북 청년의 날 행사 소모임")
  private String title;

  @Schema(description = "이벤트 장소", example = "서울특별시 성북구 서경로 124")
  private String location;

  @Schema(description = "시작 시간", example = "2025-08-01T10:00:00")
  private LocalDateTime startAt;

  @Schema(description = "종료 시간", example = "2025-08-02T10:00:00")
  private LocalDateTime endAt;

  @Schema(description = "관심 목록 수", example = "129")
  private int favoriteCount;

  @Schema(description = "댓글 수", example = "68")
  private int commentCount;

  @Schema(description = "사장님 인증 여부", example = "true")
  private boolean isBusinessVerified;

  @Schema(description = "좋아요 여부")
  private boolean isLiked;

  @Schema(description = "UP 이벤트인지 여부")
  private boolean isUp;

  @Schema(description = "Up 시작 시각")
  private LocalDateTime upStartedAt;

  @Schema(description = "UP 종료 시각")
  private LocalDateTime upEndAt;

  @Schema(description = "Up 남은 시간(초)")
  private Long upRemainingSeconds;   // ← LocalDateTime → Long 로 변경

}
