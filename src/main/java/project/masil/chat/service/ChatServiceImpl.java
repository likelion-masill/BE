package project.masil.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.chat.converter.ChatConverter;
import project.masil.chat.dto.request.SendMessageRequest;
import project.masil.chat.dto.response.ChatMessageResponse;
import project.masil.chat.dto.response.ChatRoomResponse;
import project.masil.chat.dto.response.ChatTargetResponse;
import project.masil.chat.entity.ChatMessage;
import project.masil.chat.entity.ChatRoom;
import project.masil.chat.enums.ChatContextType;
import project.masil.chat.exception.ChatErrorCode;
import project.masil.chat.repository.ChatMessageRepository;
import project.masil.chat.repository.ChatRoomRepository;
import project.masil.community.exception.ClubPostErrorCode;
import project.masil.community.exception.CommentErrorCode;
import project.masil.community.exception.PostErrorCode;
import project.masil.community.service.ClubPostService;
import project.masil.community.service.CommentService;
import project.masil.community.service.EventPostService;
import project.masil.global.exception.CustomException;
import project.masil.user.entity.User;
import project.masil.user.repository.UserRepository;

/**
 * - 메세지 전송 시 메시지 저장/ 방 캐시 / 안읽음 갱신을 하나의 트랜잭션으로 처리
 * - 컨텍스트 기반으로 단일 방을 보장
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService{

  private final ChatRoomRepository roomRepository;
  private final ChatMessageRepository messageRepository;
  private final ChatConverter converter;

  // 컨텍스트 ID 검증을 위한 의존성
  private final EventPostService eventPostService;
  private final CommentService commentService;
  private final ClubPostService clubPostService;

  // 메시지 저장 시 sender 참조 주입
  private final UserRepository userRepository;

  /**
   * 두 사용자 ID를 (작은, 큰)으로 정규화 하는 메소드 -> 단일 방 보장 키에 사용
   * @param user1
   * @param user2
   * @return
   */
  private static long[] normalizePair(Long user1, Long user2) {
    long userAId = Math.min(user1, user2); // 두 사용자중 작은 ID -> userAId
    long userBId = Math.max(user1, user2); // 두 사용자중 큰 ID -> userBId
    return new long[]{userAId, userBId};
  }

  /**
   * 컨텍스트 ID에 따른 사용자 ID가 존재하는지, targetUserId와 일치하는지 검증
   * - type에 따른 contextId로 기대되는 대상 사용자 ID를 조회하고 targetUserId와 일치하는지 확인
   * - 존재하지 않거나 일치하지 않으면 예외
   * @param type
   * @param contextId
   * @param targetUserId
   */
  private void validateContextAndTarget(ChatContextType type, Long contextId, Long targetUserId) {
    Long expectedTargetUserId; // 예상되는 사용자ID
    /**
     * 1. 컨텍스트 ID에 따른 사용자 ID가 존재하는지
     */
    switch (type) {
      case EVENT_POST -> {
        expectedTargetUserId = eventPostService.getEventAuthorId(contextId);
        if (expectedTargetUserId == null) {
          throw new CustomException(PostErrorCode.POST_NOT_FOUND);
        }
      }

      case COMMENT -> {
        expectedTargetUserId = commentService.getCommentAuthorId(contextId);
        if (expectedTargetUserId == null) {
          throw new CustomException(CommentErrorCode.COMMENT_NOT_FOUND);
        }
      }

      case CLUB_POST -> {
        expectedTargetUserId = clubPostService.getClubLeaderUserId(contextId);
        if (expectedTargetUserId == null) {
          throw new CustomException(ClubPostErrorCode.CLUB_POST_NOT_FOUND);
        }
      }

      default -> throw new IllegalArgumentException("지원하지 않는 ChatContextType : " + type);
    }

    /**
     * 2. 컨텍스트 ID에 따른 사용자 ID가 targetUserId와 일치하는지 검증
     */
    if (!expectedTargetUserId.equals(targetUserId)) {
      throw new CustomException(ChatErrorCode.CONTEXT_TARGET_MISMATCH);

    }

  }

  /**
   * targetUserId 조회
   * @param type
   * @param contextId
   * @return
   */
  @Transactional(readOnly = true)
  public Long findTargetUserId(ChatContextType type, Long contextId) {
    return switch (type) {
      case EVENT_POST -> {
        Long id = eventPostService.getEventAuthorId(contextId);
        if (id == null) throw new CustomException(PostErrorCode.POST_NOT_FOUND);
        yield id;
      }
      case COMMENT -> {
        Long id = commentService.getCommentAuthorId(contextId);
        if (id == null) throw new CustomException(CommentErrorCode.COMMENT_NOT_FOUND);
        yield id;
      }
      case CLUB_POST -> {
        Long id = clubPostService.getClubLeaderUserId(contextId);
        if (id == null) throw new CustomException(ClubPostErrorCode.CLUB_POST_NOT_FOUND);
        yield id;
      }
      default -> throw new IllegalArgumentException("지원하지 않는 ChatContextType: " + type);
    };
  }

  /**
   * 채팅방 생성/조회
   * - 채팅방이 없다면 채팅방 생성
   * - 채팅방이 있다면 기존 채팅방 반환
   * @param contextType
   * @param contextId
   * @param targetUserId
   * @param currentUserId
   * @return
   */
  @Override
  public ChatRoomResponse openChatRoom(ChatContextType contextType, Long contextId,
      Long targetUserId, Long currentUserId) {

    if (currentUserId.equals(targetUserId)) {
      throw new CustomException(ChatErrorCode.SELF_CHAT_NOT_ALLOWED);
    }

    // 컨텍스트 ID에 따른 사용자 ID가 존재하는지, targetUserId와 일치하는지 검증
    validateContextAndTarget(contextType, contextId, targetUserId);

    // 두 사용자 ID를 정규화한 값을 pair에 저장
    long[] pair = normalizePair(currentUserId, targetUserId);

    ChatRoom room = roomRepository.findByContextTypeAndContextIdAndUserAIdAndUserBId(
        contextType, contextId, pair[0], pair[1]
    ).orElseGet(() -> {
      try {
        // 채팅방 없을때만 생성 시도
        return roomRepository.save(
            ChatRoom.builder()
                .contextType(contextType)
                .contextId(contextId)
                .userAId(pair[0])
                .userBId(pair[1])
                .unreadCountA(0)
                .unreadCountB(0)
                .build());
      } catch (DataIntegrityViolationException e) {
        /**
         * 동시에 여러 요청이 채팅방 생성을 시도했을 경우,
         * 1. 가장 먼저 성공한 요청 A가 INSERT 성공 -> DB에 유니크키로 방 1개 생성
         * 2. 나머지 요청들은 INSERT 시점에 유니크 제약 위반으로 DataIntegrityViolationException 발생
         * 3. 이미 A가 만든 동일한 방이 DB에 존재하므로, 요청 B는 새로 만들 필요가 없고 그 방을 "다시 조회해서 반환"하면 됌.
         */
        return roomRepository.findByContextTypeAndContextIdAndUserAIdAndUserBId(
            contextType, contextId, pair[0], pair[1]
        ).orElseThrow(
            () -> new CustomException(ChatErrorCode.ROOM_CREATE_CONFLICT)); // 재조회까지 안되면 비정상 상황
      }
    });

    return converter.toRoomResponse(room, currentUserId);
  }

  /**
   * 내 채팅방 목록 조회
   * @param currentUserId
   * @param pageable
   * @return
   */
  @Override
  @Transactional(readOnly = true)
  public Page<ChatRoomResponse> getMyRooms(Long currentUserId, Pageable pageable) {
    return roomRepository.findMyRooms(currentUserId, pageable)
        .map(room -> converter.toRoomResponse(room, currentUserId));
  }

  /**
   * 메시지 조회
   * - 접근 권한(참여자) 검증
   * - 조회 시 내 unread를 0으로 리셋(읽음 처리)
   * @param roomId
   * @param currentUserId
   * @param pageable
   * @return
   */
  @Override
  public Page<ChatMessageResponse> getMessages(Long roomId, Long currentUserId, Pageable pageable) {
    ChatRoom room = roomRepository.findById(roomId)
        .orElseThrow(() -> new CustomException(ChatErrorCode.ROOM_NOT_FOUND));

    if (!room.hasParticipant(currentUserId)) {
      throw new CustomException(ChatErrorCode.FORBIDDEN_ROOM_ACCESS);
    }

    room.resetUnreadFor(currentUserId);

    return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId, pageable)
        .map(converter::toMessageResponse);
  }

  /**
   * 메시지 전송(TEXT)
   *
   * 처리 순서(트랜잭션 내):
   *  1) 검증: 방 존재/참여자 권한, 메시지 본문(공백/길이)
   *  2) sender 연관 주입:
   *     - 숫자 ID → User 엔티티(프록시)로 변환(getReferenceById)
   *     - ChatMessage.sender에 ManyToOne으로 연결 → DB 외래키(sender_id) 정확히 세팅
   *     - 지연로딩이라 실제 엔티티 조회는 필요할 때만 발생(성능 이점)
   *  3) 저장: messageRepository.save(...) → 커밋/flush 시 INSERT, createdAt은 JPA Auditing으로 자동
   *  4) 방 캐시 갱신:
   *     - ChatRoom.lastMessage / lastMessageAt 즉시 갱신
   *     - 목록 화면에서 “최근 대화”를 빠르게 표시(메시지 테이블 재조회 비용 절감)
   *  5) 상대 unread+1:
   *     - 보낸 사람이 A면 B의 unread++, B면 A의 unread++
   *     - room.increaseUnreadForOther(senderId) 호출로 ‘상대방’의 안읽음만 증가
   *
   * @param roomId   채팅방 ID
   * @param senderId 보낸 사용자 ID
   * @param request  메시지 내용(TEXT)
   * @return 저장된 메시지의 응답 DTO
   */
  @Override
  public ChatMessageResponse sendMessage(Long roomId, Long senderId, SendMessageRequest request) {
    // 채팅방 존재 확인
    ChatRoom room = roomRepository.findById(roomId)
        .orElseThrow(() -> new CustomException(ChatErrorCode.ROOM_NOT_FOUND));

    // 접근 권한 확인(해당 방의 참여자가 맞는지)
    if (!room.hasParticipant(senderId)) {
      throw new CustomException(ChatErrorCode.FORBIDDEN_ROOM_ACCESS);
    }

    // 1) 메시지 본문 유효성(공백/길이) 검사
    String content = (request.getContent() == null) ? "": request.getContent().trim();
    if (content.isEmpty()) throw new CustomException(ChatErrorCode.MESSAGE_EMPTY);
    if (content.length() > 1000) throw new CustomException(ChatErrorCode.MESSAGE_TOO_LONG);

    /**
     * 2) sender 연관 주입: senderID -> User 엔티티 프록시 반환
     *     - getReferenceById는 실제 SELECT 없이 프록시 반환 (필요 시점에 쿼리)
     *     - 외래키(sender_id)만 정확히 넣고 싶을 때 성능상 유리
     */
    User senderReference = userRepository.getReferenceById(senderId);

    /**
     * 3) 메시지 저장: room(연관), sender(연관), content만 세팅
     *    - createdAt은 BaseTimeEntity + @EnableJpaAuditing으로 자동입력
     */
    ChatMessage saved = messageRepository.save(
        ChatMessage.builder()
            .room(room)  // ← @ManyToOne(채팅방)
            .sender(senderReference) // ← @ManyToOne(보낸 사람)
            .content(content)
            .build());

    // 4) 방 캐시 캥신: 목록 화면에서 빠르게 '최근 메시지/시간'을 보여주기 위함
    room.setLastMessage(saved.getContent());
    room.setLastMessageAt(saved.getCreatedAt());

    // 5) 상대 unread + 1: 보낸 사람이 A면 B가 +1, 보낸 사람이 B면 A가 +1
    room.increaseUnreadForOther(senderId);

    return converter.toMessageResponse(saved);
  }

  /**
   * [읽음 처리]
   * - 방 존재/권한 확인 후, 현재 사용자 기준 unread를 0으로 만든다.
   * - WebSocket(/app/chat/rooms/{roomId}/read)이나 REST 조회 시 호출 가능.
   */
  @Override
  public void markAsRead(Long roomId, Long userId) {
    ChatRoom room = roomRepository.findById(roomId)
        .orElseThrow(() -> new CustomException(ChatErrorCode.ROOM_NOT_FOUND));

    if (!room.hasParticipant(userId)) {
      throw new CustomException(ChatErrorCode.FORBIDDEN_ROOM_ACCESS);
    }

    room.resetUnreadFor(userId); // 사용자가 채팅방을 열어 메시지를 읽었을 때, 본인의 안읽음 수를 0으로 초기화

  }

  /**
   * 내 채팅방 목록보기에서 한 줄(row) DTO를 사용자 관점으로 조회(unread/상대ID/최신메시지/최신메시지 시각 포함)
   * @param roomId
   * @param userId
   * @return
   */
  @Override
  public ChatRoomResponse getRoomRowFor(Long roomId, Long userId) {
    ChatRoom room = roomRepository.findById(roomId)
        .orElseThrow(() -> new CustomException(ChatErrorCode.ROOM_NOT_FOUND));

    if (!room.hasParticipant(userId)) {
      throw new CustomException(ChatErrorCode.FORBIDDEN_ROOM_ACCESS);
    }

    return converter.toRoomResponse(room, userId);
  }

  /**
   * 1:1 방에서 내 상대 userId 반환 (권한 검증 포함)
   * @param roomId
   * @param userId
   * @return
   */
  @Override
  @Transactional(readOnly = true)
  public Long getOtherParticipantId(Long roomId, Long userId) {
    ChatRoom room = roomRepository.findById(roomId)
        .orElseThrow(() -> new CustomException(ChatErrorCode.ROOM_NOT_FOUND));

    if (!room.hasParticipant(userId)) {
      throw new CustomException(ChatErrorCode.FORBIDDEN_ROOM_ACCESS);
    }

    // 내가 A면 B 반환, 내가 B면 A 반환
    return room.getUserAId().equals(userId) ? room.getUserBId() : room.getUserAId();
  }


}
