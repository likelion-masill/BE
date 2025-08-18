package project.masil.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.masil.global.common.BaseTimeEntity;
import project.masil.user.entity.User;

/**
 * [채팅 메시지 : TEXT 전용]
 * - 타임 단순화를 위해 content만 저장
 * - (room_id, created_at) 인덱스로 시간순 페이징 최적화
 */
@Entity
@Table(name = "chat_message",
       indexes = {
          @Index(name = "idx_msg_room_created", columnList = "room_id, createdAt"),
          @Index(name = "idx_msg_sender", columnList = "sender_id")
       }

)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseTimeEntity {

  /**
   * 메시지 PK
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 소속 채팅방 ID
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private ChatRoom room;

  /**
   * 보낸 사용자 ID
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  private User sender;

  /**
   * 메시지 본문(TEXT)
   */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

}