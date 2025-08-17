package project.masil.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.masil.community.dto.response.FavoriteResponse;
import project.masil.community.enums.PostType;
import project.masil.community.service.FavoriteService;
import project.masil.global.response.BaseResponse;
import project.masil.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}")
@Tag(name = "Favorite", description = "관심목록 관련 API")
public class FavoriteController {

  private final FavoriteService favoriteService;

  @Operation(summary = "이벤트 게시글 관심목록 토글", description = "이벤트 게시글의 관심 목록 상태를 토글합니다.")
  @PostMapping("/favorites")
  public ResponseEntity<BaseResponse<FavoriteResponse>> toggleEventFavorite(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long eventId) {
    return ResponseEntity.ok(
        BaseResponse.success("이벤트 게시글 토글 성공",
            favoriteService.toggleFavorite(userDetails.getUser().getId(), eventId,
                PostType.EVENT)));
  }

  @Operation(summary = "소모임 게시글 관심목록 토글", description = "소모임 게시글의 관심 목록 상태를 토글합니다.")
  @PostMapping("/clubs/{clubId}/favorites")
  public ResponseEntity<BaseResponse<FavoriteResponse>> toggleClubFavorite(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long eventId,
      @PathVariable Long clubId) {
    return ResponseEntity.ok(
        BaseResponse.success("소모임 게시글 토글 성공",
            favoriteService.toggleFavorite(userDetails.getUser().getId(), clubId,
                PostType.CLUB)));
  }

}
