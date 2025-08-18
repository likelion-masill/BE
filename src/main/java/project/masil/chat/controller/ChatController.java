package project.masil.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.masil.chat.dto.request.CreateRoomRequest;
import project.masil.chat.dto.request.SendMessageRequest;
import project.masil.chat.dto.response.ChatMessageResponse;
import project.masil.chat.dto.response.ChatRoomResponse;
import project.masil.chat.dto.response.ChatTargetResponse;
import project.masil.chat.enums.ChatContextType;
import project.masil.chat.service.ChatService;
import project.masil.community.service.ClubPostService;
import project.masil.community.service.CommentService;
import project.masil.community.service.EventPostService;
import project.masil.global.response.BaseResponse;
import project.masil.global.security.CustomUserDetails;

/**
 * 채팅 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/chats")
@Tag(name = "Chat API", description = "이벤트/댓글/소모임 1:1 채팅 API")
public class ChatController {

  private final ChatService chatService;

  // 채팅방 진입할때 어떤 채팅방인지에 따라 상대 ID 산출에 사용하는 도메인 서비스가 다름
  private final EventPostService eventPostService;
  private final CommentService commentService;
  private final ClubPostService clubPostService;

  /**
   * 채팅방 생성 전 컨텍스트 작성자 ID 확인용
   */
  @Operation(summary = "컨텍스트 작성자 ID 반환",
      description = "contextType(EVENT_POST/COMMENT/CLUB_POST)과 contextId를 주면 해당 글의 작성자 ID(targetUserId)를 반환합니다.")
  @GetMapping("/target-user")
  public ResponseEntity<BaseResponse<ChatTargetResponse>> getTargetUserId(
      @RequestParam ChatContextType contextType,
      @RequestParam Long contextId) {

    Long targetUserId = chatService.findTargetUserId(contextType, contextId);

    ChatTargetResponse body = ChatTargetResponse.builder()
        .contextType(contextType)
        .contextId(contextId)
        .targetUserId(targetUserId)
        .build();

    return ResponseEntity.ok(BaseResponse.success("작성자 ID 조회 성공", body));
  }


  /**
   *  [공통] 채팅방 생성/조회 : 컨텍스트 + 상대ID를 직접 전달
   *    - 없으면 생성, 있으면 기존 방 반환 (멱등)
   */
  @Operation(
      summary = "채팅방 생성/조회(공통)",
      description = """
            컨텍스트와 상대 사용자 ID를 직접 전달하는 범용 API.
            - EVENT_POST : contextId=eventPostId, targetUserId=이벤트 작성자 ID
            - COMMENT    : contextId=commentId   , targetUserId=댓글 작성자 ID
            - CLUB_POST  : contextId=clubPostId  , targetUserId=소모임장 ID\n
            같은 컨텍스트에서 동일한 두 사용자 조합이면 기존 방을 그대로 반환합니다.
            """
  )
  @PostMapping("/rooms")
  public ResponseEntity<BaseResponse<ChatRoomResponse>> openRoom(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid CreateRoomRequest request
  ) {
    Long userId = userDetails.getUser().getId();
    ChatRoomResponse response = chatService.openChatRoom(
        request.getContextType(),
        request.getContextId(),
        request.getTargetUserId(),
        userId
    );
    return ResponseEntity.ok(BaseResponse.success("채팅방 생성/조회 성공", response));
  }

  /**
   * 이벤트 ID로 → 작성자와 1:1 대화
   *     - 프론트는 eventId만 넘기면 서버가 작성자 ID를 찾아 targetUserId로 사용
   */
  @Operation(summary = "이벤트 작성자와 대화 시작", description = "이벤트 상세에서 '대화하기' 버튼 클릭 시 호출")
  @PostMapping("/events/{eventId}/rooms")
  public ResponseEntity<BaseResponse<ChatRoomResponse>> openRoomWithEventAuthor(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long eventId
  ) {
    Long userId = userDetails.getUser().getId();
    Long authorId = eventPostService.getEventAuthorId(eventId);
    ChatRoomResponse response = chatService.openChatRoom(
        ChatContextType.EVENT_POST, eventId, authorId, userId
    );
    return ResponseEntity.ok(BaseResponse.success("이벤트 작성자와의 채팅방 생성/조회 성공", response));
  }

  /**
   * 댓글 → 댓글 작성자와 1:1 대화
   */
  @Operation(summary = "댓글 작성자와 대화 시작", description = "댓글 프로필에서 '채팅 신청' 클릭 시 호출")
  @PostMapping("/comments/{commentId}/rooms")
  public ResponseEntity<BaseResponse<ChatRoomResponse>> openRoomWithCommentAuthor(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long commentId
  ) {
    Long userId = userDetails.getUser().getId();
    Long authorId = commentService.getCommentAuthorId(commentId);
    ChatRoomResponse response = chatService.openChatRoom(
        ChatContextType.COMMENT, commentId, authorId, userId
    );
    return ResponseEntity.ok(BaseResponse.success("댓글 작성자와의 채팅방 생성/조회 성공", response));
  }

  /**
   * 소모임 → 소모임장과 1:1 대화
   */
  @Operation(summary = "소모임장과 대화 시작", description = "소모임 상세에서 '대화하기' 클릭 시 호출")
  @PostMapping("/clubs/{clubPostId}/rooms")
  public ResponseEntity<BaseResponse<ChatRoomResponse>> openRoomWithClubLeader(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long clubPostId
  ) {
    Long userId = userDetails.getUser().getId();
    Long leaderId = clubPostService.getClubLeaderUserId(clubPostId);
    ChatRoomResponse response = chatService.openChatRoom(
        ChatContextType.CLUB_POST, clubPostId, leaderId, userId
    );
    return ResponseEntity.ok(BaseResponse.success("소모임장과의 채팅방 생성/조회 성공", response));
  }

  /**
   * 내 채팅방 목록(정렬/페이징은 쿼리 파라미터로) 조회
   */
  @Operation(summary = "내 채팅방 목록", description = "현재 로그인 사용자의 채팅방 목록을 페이징으로 조회합니다.")
  @GetMapping("/rooms")
  public ResponseEntity<BaseResponse<Page<ChatRoomResponse>>> getMyRooms(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "lastMessageAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir
  ) {
    int pageIndex = Math.max(0, page - 1);
    Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));

    Page<ChatRoomResponse> response = chatService.getMyRooms(userDetails.getUser().getId(), pageable);

    return ResponseEntity.ok(BaseResponse.success("내 채팅방 목록 조회 성공", response));
  }

  /**
   * 특정 방 메시지 조회(조회 시 내 unread=0)
   */
  @Operation(summary = "특정 채팅방 메시지 조회(조회 시 내 안읽음수 = 0)",
      description = "특정 채팅방의 메시지를 시간 오름차순으로 페이징 조회합니다. 이 호출 시 내 안읽음수가 0으로 초기화됩니다.")
  @GetMapping("/rooms/{roomId}/messages")
  public ResponseEntity<BaseResponse<Page<ChatMessageResponse>>> getMessages(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long roomId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    int pageIndex = Math.max(0, page - 1);
    Pageable pageable = PageRequest.of(pageIndex, size);
    Page<ChatMessageResponse> response = chatService.getMessages(roomId, userDetails.getUser().getId(), pageable);

    return ResponseEntity.ok(BaseResponse.success("메시지 조회 성공", response));
  }

  // --------------------------------------------------------------------
  // 메시지 전송(TEXT) — Swagger에서 기능 확인용(실서비스는 WebSocket 전송 권장)
  // --------------------------------------------------------------------
  @Operation(summary = "메시지 전송(테스트용 REST)", description = "TEXT 메시지를 전송합니다. 전송 후 최근 메시지/시각이 갱신되고 상대 안읽음 수가 증가합니다. (실시간 사용은 WebSocket 권장)")
  @PostMapping("/rooms/{roomId}/messages")
  public ResponseEntity<BaseResponse<ChatMessageResponse>> sendMessage(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long roomId,
      @RequestBody @Valid SendMessageRequest request
  ) {
    ChatMessageResponse resp =
        chatService.sendMessage(roomId, userDetails.getUser().getId(), request);

    return ResponseEntity.ok(BaseResponse.success("메시지 전송 성공", resp));
  }


}
