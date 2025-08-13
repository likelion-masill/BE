package project.masil.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(title = "CommentRequest", description = "댓글/대댓글 생성 위한 데이터 전송")
public class CommentRequest {

  @NotBlank(message = "댓글 내용은 필수입니다.")
  @Schema(description = "댓글 내용", example = "재밌겠다!!")
  private String content;
}
