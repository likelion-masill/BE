package project.masil.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "CommentResponse", description = "댓글 조회 결과를 담는 응답")
public class CommentResponse {

  @Schema(description = "댓글 ID", example = "1")
  private Long commentId;

  @Schema(description = "댓글 내용", example = "재밌겠다!!")
  private String content;

  @Schema(description = "댓글 작성자 닉네임", example = "user123")
  private String username;

  @Schema(description = "댓글 작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String userProfileImageUrl;

  @Schema(description = "댓글 작성 시간", example = "2024-08-01T12:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "대댓글 개수", example = "5")
  private int replyCommentCount;

}