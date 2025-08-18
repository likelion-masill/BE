package project.masil.chat.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import project.masil.chat.enums.ChatContextType;
import project.masil.global.common.BaseTimeEntity;
import project.masil.user.entity.User;

@Entity
@Table(name = "chat_rooms",
       uniqueConstraints = {
          // 같은 출처(컨텍스트)에서 같은 두 사용자 조합이면 방이 1개만 존재
          @UniqueConstraint(name = "uk_room_ctx_users",
            columnNames = {"context_type", "context_id", "user_a_id", "user_b_id"}
          )
       },
       indexes = {
          // 내 방 목록 조회 최적화
          @Index(name = "idx_room_usera", columnList = "user_a_id"),
          @Index(name = "idx_room_userb", columnList = "user_b_id"),
          // 최근 대화 순 정렬 최적화
          @Index(name = "idx_room_lastmsg", columnList = "last_message_at DESC"),
          // 컨텍스트별 통계/운영 조회에 유용
          @Index(name = "idx_room_context", columnList = "context_type, context_id")
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseTimeEntity {

  /**
   * 방 PK
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 대화 출처(이벤트/댓글/소모임)
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "context_type", nullable = false, length = 30)
  private ChatContextType contextType;

  /**
   * 출처 식별자 (EVENT_POST면 eventPostId, CLUB_POST면 clubPostId
   */
  @Column(name = "context_id", nullable = false)
  private Long contextId;

  /**
   * 항상 두 ID 중 작은 값을 userAId에, 큰 값을 userBId에 넣으면 => (1,5) 이렇게 순서가 강제돼서, 유니크 제약 조건을 걸면 중복 방 생성이 불가능.
   * - 두 사용자 쌍을 항상 같은 “기준 형태”로 저장해서 중복을 막는 방법
   */

  /**
   * 두 사용자 중 '작은' ID (중복 방지)
   */
  @Comment("참여자 A(두 사용자 중 작은 ID)")
  @Column(name = "user_a_id", nullable = false)
  private Long userAId;

  /**
   * 두 사용자 중 '큰' ID (중복 방지)
   */
  @Comment("참여자 B(두 사용자 중 큰 ID)")
  @Column(name = "user_b_id", nullable = false)
  private Long userBId;

  /**
   * 채팅방 목록 화면에서 미리보기로 보여줄 '최근 메시지' 내용을 캐싱해 두는 필드.
   * - 메시지 테이블(chat_message)에서 매번 최신 메시지를 조회하면 방이 많아질수록 성능이 저하됨.
   * - 따라서, 새 메시지가 저장될 때 해당 내용으로 이 필드를 즉시 업데이트하여 목록 조회 시 바로 사용.
   * - 예: "네, 내일 뵙겠습니다." → 채팅방 리스트에 그대로 출력.
   */
  @Comment("목록용 최근 메시지 캐시")
  @Column(length = 300)
  private String lastMessage;

  /**
   * '최근 메시지'가 작성된 시각을 저장하는 캐시 필드.
   * - 채팅방 목록을 '최근 대화 순'으로 정렬할 때 사용.
   * - 새 메시지가 오면 이 값도 즉시 갱신.
   * - 예: lastMessageAt = 2025-08-15T16:10 → 목록 정렬 시 최신순으로 배치.
   */
  @Column(name = "last_message_at")
  private LocalDateTime lastMessageAt;

  /**
   * 사용자 A 기준 안읽음 수(내가 A면 이 값이 내 unread)
   */
  @Comment("A 기준 안읽음 수")
  @Column(name = "unread_count_a", nullable = false)
  private int unreadCountA;

  /**
   * 사용자 B 기준 안읽음 수(내가 B면 이 값이 내 unread)
   */
  @Comment("B 기준 안읽음 수")
  @Column(name = "unread_count_b", nullable = false)
  private int unreadCountB;


  /**
   * 권한 체크: userId가 방의 참여자인지
   */
  public boolean hasParticipant(Long userId) {
    return userAId.equals(userId) || userBId.equals(userId);
  }

  /**
   * 보낸 사람이 senderId일 때 '상대방'의 안읽음 수 + 1
   *
   * A가 메시지를 보냈으면 → B가 아직 안 읽었으니 unreadCountB++
   * B가 메시지를 보냈으면 → A가 아직 안 읽었으니 unreadCountA++
   */
  public void increaseUnreadForOther(Long senderId) {
    if (senderId.equals(userAId)) unreadCountB++;
    else unreadCountA++;
  }

  /**
   * userId 본인 기준 안읽음 수 0으로 리셋(메시지 목록 열 때 호출)
   *
   * 사용자가 채팅방을 열어 메시지를 읽었을 때, 본인의 안읽음 수를 0으로 초기화
   * 내가 A라면 → 내 안읽음 카운트는 unreadCountA
   * 내가 B라면 → 내 안읽음 카운트는 unreadCountB
   * 해당 값을 0으로 바꿔서 "읽음" 상태로 표시
   */
  public void resetUnreadFor(Long userId) {
    if (userId.equals(userAId)) unreadCountA = 0;
    else unreadCountB = 0;
  }


}