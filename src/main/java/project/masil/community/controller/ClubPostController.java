package project.masil.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.masil.community.dto.request.ClubPostRequest;
import project.masil.community.dto.response.ClubPostDetailResponse;
import project.masil.community.dto.response.ClubPostSummaryResponse;
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
  public ResponseEntity<BaseResponse<ClubPostDetailResponse>> createClubPost(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long eventId,
      @RequestBody @Valid ClubPostRequest createRequest) {
    ClubPostDetailResponse result = clubPostService.createClubPost(
        userDetails.getUser().getId(), eventId, createRequest);
    return ResponseEntity.ok(BaseResponse.success("소모임 게시글 생성 성공", result));
  }

  @Operation(summary = "소모임 게시글 상세 조회", description = "소모임 게시글의 상세 정보를 조회합니다.")
  @PostMapping("/{clubId}")
  public ResponseEntity<BaseResponse<ClubPostDetailResponse>> getClubPostDetail(
      @PathVariable Long clubId) {
    ClubPostDetailResponse result = clubPostService.getClubPostDetail(clubId);
    return ResponseEntity.ok(BaseResponse.success("소모임 게시글 생성 성공", result));
  }

  @Operation(summary = "소모임 게시글 수정", description = "소모임 게시글을 수정합니다.")
  @PutMapping("/{clubId}")
  public ResponseEntity<BaseResponse<ClubPostDetailResponse>> updateClubPost(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long clubId,
      @RequestBody @Valid ClubPostRequest updateRequest) {
    ClubPostDetailResponse result = clubPostService.updateClubPost(userDetails.getUser().getId(),
        clubId, updateRequest);
    return ResponseEntity.ok(BaseResponse.success("소모임 게시글 수정 성공", result));
  }

  @Operation(summary = "소모임 게시글 삭제", description = "소모임 게시글을 삭제합니다.")
  @DeleteMapping("/{clubId}")
  public ResponseEntity<BaseResponse<Void>> deleteClubPost(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long clubId) {
    clubPostService.deleteClubPost(userDetails.getUser().getId(), clubId);
    return ResponseEntity.ok(BaseResponse.success("소모임 게시글 삭제 성공", null));
  }

  @Operation(summary = " 특정 이벤트 게시그르이 소모임 게시글 목록 조회",
      description = "특정 이벤트에 대한 소모임 게시글 목록을 조회합니다.")
  @PostMapping("/all")
  public ResponseEntity<BaseResponse<Page<ClubPostSummaryResponse>>> getClubPostListByEventId(
      @PathVariable Long eventId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    int pageIndex = Math.max(0, page - 1);
    Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
    Pageable pageable = PageRequest.of(pageIndex, size, sort);

    Page<ClubPostSummaryResponse> result = clubPostService.getClubPostListByEventId(eventId,
        pageable);
    return ResponseEntity.ok(BaseResponse.success("소모임 게시글 목록 조회 성공", result));
  }


}
