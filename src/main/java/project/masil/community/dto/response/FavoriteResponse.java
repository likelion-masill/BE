package project.masil.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(title = "FavoriteResponse", description = "게시글의 관심 목록 상태 응답 DTO")
public class FavoriteResponse {

  @Schema(description = "게시물에 대한 사용자의 관심 목록 상태", example = "true")
  private boolean isFavorite;

  @Schema(description = "게시글의 관심 목록 수", example = "42")
  private int favoriteCount;

}
