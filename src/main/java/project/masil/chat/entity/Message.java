package project.masil.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.masil.user.entity.User;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long messageId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private ChatRoom room;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  private User sender;

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  private LocalDateTime sentAt;

}