package project.masil.mypage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.masil.global.response.BaseResponse;
import project.masil.global.security.CustomUserDetails;
import project.masil.mypage.dto.response.PostResponse;
import project.masil.mypage.service.MyPageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
@Tag(name = "MyPageController", description = "마이페이지 관련 API")
@Slf4j
public class MyPageController {

  private final MyPageService myPageService;

  @Operation(summary = "내가 작성한 게시글 목록 조회",
      description = "내가 작성한 게시글 목록을 조회합니다.")
  @GetMapping("/posts")
  public ResponseEntity<BaseResponse<Page<PostResponse>>> getClubPostListByEventId(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    int pageIndex = Math.max(0, page - 1);
    Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
    Pageable pageable = PageRequest.of(pageIndex, size, sort);

    Page<PostResponse> result = myPageService.getMyPostList(
        userDetails.getUser().getId(), pageable);
    return ResponseEntity.ok(BaseResponse.success("내가 작성한 게시글 목록 조회 성공", result));
  }

  @Operation(summary = "내가 관심 있는 게시글 목록 조회",
      description = "내가 관심 있는 게시글 목록을 조회합니다.")
  @GetMapping("/favorites")
  public ResponseEntity<BaseResponse<Page<PostResponse>>> getMyFavoritePostList(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    int pageIndex = Math.max(0, page - 1);
    Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
    Pageable pageable = PageRequest.of(pageIndex, size, sort);

    Page<PostResponse> result = myPageService.getMyFavoritePostList(
        userDetails.getUser().getId(), pageable);
    return ResponseEntity.ok(BaseResponse.success("내가 관심 있는 게시글 목록 조회 성공", result));
  }


}
