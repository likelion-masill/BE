package project.masil.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_event_log")
@Getter
@Setter
public class UserEventLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false)
  private Long userId;
  @Column(nullable = false)
  private Long postId;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserActionType action;
  @Column(nullable = false)
  private LocalDateTime occurredAt = LocalDateTime.now();
}