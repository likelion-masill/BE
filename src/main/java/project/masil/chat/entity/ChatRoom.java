package project.masil.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.masil.user.entity.User;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long roomId;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user1_id", nullable = false)
  private User user1;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user2_id", nullable = false)
  private User user2;

  @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
  private List<Message> messages;

}