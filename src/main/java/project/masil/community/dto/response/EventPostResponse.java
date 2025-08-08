package project.masil.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "EventPostResponse : 이벤트 응답 DTO")
public class EventPostResponse {

  @Schema(description = "이벤트 ID", example = "1")
  private Long eventId;

  @Schema(description = "작성자 이름", example = "성북구 청년봉사자")
  private String username;

  @Schema(description = "작성자 프로필 이미지", example = "https://my-e~~~~~vent01.jpg")
  private String userImage;

  @Schema(description = "이벤트 제목", example = "성북 청년의 날")
  private String title;

  @Schema(description = "이벤트 내용", example = "성북 청년의 날 이벤트 내용")
  private String content;

  @Schema(description = "이벤트 장소", example = "서울특별시 성북구 서경로 124")
  private String location;

  @Schema(description = "이벤트 시작 날짜", example = "2025-08-08T10:00:00")
  private LocalDateTime startDate;

  @Schema(description = "종료 날짜", example = "2025-09-01T18:00:00")
  private LocalDateTime endDate;

  @Schema(description = "AI 이벤트 요약", example = "성북 청년의 날 이벤트 요약")
  private String summary;

  @Schema(description = "관심 목록 개수", example = "해당 이벤트 관심목록 개수")
  private Integer favoriteCount;

  @Schema(description = "댓글 개수", example = "해당 이벤트 댓글 개수")
  private Integer commentCount;

  @Schema(description = "조회수", example = "해당 이벤트 조회수")
  private Integer viewCount;

  @Schema(description = "이벤트 이미지 리스트", example = "https://my-e~~~~~vent01.jpg")
  private List<String> images;
}
