package project.masil.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.masil.community.dto.request.ClubPostCreateRequest;
import project.masil.community.dto.response.ClubPostDetailResponse;
import project.masil.community.service.ClubPostService;
import project.masil.global.response.BaseResponse;
import project.masil.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}/clubs")
@Tag(name = "ClubPost", description = "소모임 게시글 관련 API")
@Slf4j
public class ClubPostController {

  private final ClubPostService clubPostService;

  @Operation(summary = "소모임 게시글 생성", description = "새로운 소모임 게시글을 생성합니다.")
  @PostMapping
  public ResponseEntity<BaseResponse<Void>> createClubPost(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long eventId,
      @RequestBody ClubPostCreateRequest createRequest) {
    ClubPostDetailResponse result = clubPostService.createClubPost(
        userDetails.getUser().getId(), eventId, createRequest);

    return ResponseEntity.ok(BaseResponse.success("음식 추가 성공", null));
  }

}
