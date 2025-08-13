package project.masil.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.masil.community.dto.request.CommentRequest;
import project.masil.community.dto.response.CommentResponse;
import project.masil.community.enums.PostType;
import project.masil.community.service.CommentService;
import project.masil.global.response.BaseResponse;
import project.masil.global.response.ListResponse;
import project.masil.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}")
@Tag(name = "Comment API", description = "댓글 관련 API")
public class CommentController {

  private final CommentService commentService;

  @Operation(summary = "이벤트 게시글의 댓글 생성", description = "이벤트 게시글에 댓글을 생성합니다.")
  @PostMapping("/comments")
  public ResponseEntity<BaseResponse<CommentResponse>> createEventPostComment(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable("eventId") Long eventId,
      @RequestBody @Valid CommentRequest commentRequest) {
    CommentResponse response = commentService.createComment(
        eventId, userDetails.getUser().getId(), commentRequest.getContent(), PostType.EVENT);
    return ResponseEntity.ok(BaseResponse.success("이벤트 게시글의 댓글 생성 성공", response));
  }

  @Operation(summary = "이벤트 게시글의 댓글 조회", description = "이벤트 게시글에 달린 댓글을 조회합니다.")
  @GetMapping("/comments")
  public ResponseEntity<BaseResponse<ListResponse<CommentResponse>>> getEventPostComments(
      @PathVariable("eventId") Long eventId) {
    return ResponseEntity.ok(BaseResponse.success(
        "이벤트 게시글의 댓글 조회 성공",
        commentService.getCommentsByPost(eventId, PostType.EVENT)));
  }

  @Operation(summary = "이벤트 게시글의 대댓글 생성", description = "이벤트 게시글의 댓글에 대댓글을 생성합니다.")
  @PostMapping("/{commentId}/replies")
  public ResponseEntity<BaseResponse<CommentResponse>> createEventPostChildComment(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable("eventId") Long eventId,
      @PathVariable("commentId") Long commentId,
      @RequestBody @Valid CommentRequest commentRequest) {
    CommentResponse response = commentService.createChildComment(
        eventId, commentId, userDetails.getUser().getId(), commentRequest.getContent(),
        PostType.EVENT);
    return ResponseEntity.ok(BaseResponse.success("이벤트 게시글의 대댓글 생성 성공", response));
  }

  @Operation(summary = "이벤트 게시글의 대댓글 조회", description = "이벤트 게시글의 댓글에 달린 대댓글을 조회합니다.")
  @GetMapping("/{commentId}/replies")
  public ResponseEntity<BaseResponse<ListResponse<CommentResponse>>> getEventPostChildComments(
      @PathVariable("eventId") Long eventId,
      @PathVariable("commentId") Long commentId) {
    return ResponseEntity.ok(BaseResponse.success(
        "이벤트 게시글의 대댓글 조회 성공",
        commentService.getReplyCommentsByPostAndParentComment(eventId, commentId, PostType.EVENT)));
  }


  @Operation(summary = "소모임 게시글의 댓글 생성", description = "소모임 게시글에 댓글을 생성합니다.")
  @PostMapping("/clubs/{clubId}/comments")
  public ResponseEntity<BaseResponse<CommentResponse>> createClubPostComment(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable("clubId") Long clubId,
      @RequestBody @Valid CommentRequest commentRequest) {
    CommentResponse response = commentService.createComment(
        clubId, userDetails.getUser().getId(), commentRequest.getContent(), PostType.CLUB);
    return ResponseEntity.ok(BaseResponse.success("소모임 게시글의 댓글 생성 성공", response));
  }

  @Operation(summary = "소모임 게시글의 댓글 조회", description = "소모임 게시글에 달린 댓글을 조회합니다.")
  @GetMapping("/clubs/{clubId}/comments")
  public ResponseEntity<BaseResponse<ListResponse<CommentResponse>>> getClubPostComments(
      @PathVariable("clubId") Long clubId) {
    return ResponseEntity.ok(BaseResponse.success(
        "소모임 게시글의 댓글 조회 성공",
        commentService.getCommentsByPost(clubId, PostType.CLUB)));
  }

  @Operation(summary = "소모임 게시글의 대댓글 생성", description = "소모임 게시글의 댓글에 대댓글을 생성합니다.")
  @PostMapping("/clubs/{clubId}/comments/{commentId}/replies")
  public ResponseEntity<BaseResponse<CommentResponse>> createClubPostChildComment(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable("clubId") Long clubId,
      @PathVariable("commentId") Long commentId,
      @RequestBody @Valid CommentRequest commentRequest) {
    CommentResponse response = commentService.createChildComment(
        clubId, commentId, userDetails.getUser().getId(), commentRequest.getContent(),
        PostType.CLUB);
    return ResponseEntity.ok(BaseResponse.success("소모임 게시글의 대댓글 생성 성공", response));
  }

  @Operation(summary = "소모임 게시글의 대댓글 조회", description = "소모임 게시글의 댓글에 달린 대댓글을 조회합니다.")
  @GetMapping("/clubs/{clubId}/comments/{commentId}/replies")
  public ResponseEntity<BaseResponse<ListResponse<CommentResponse>>> getClubPostChildComments(
      @PathVariable("clubId") Long clubId,
      @PathVariable("commentId") Long commentId) {
    return ResponseEntity.ok(BaseResponse.success(
        "소모임 게시글의 대댓글 조회 성공",
        commentService.getReplyCommentsByPostAndParentComment(clubId, commentId, PostType.CLUB)));
  }


}
