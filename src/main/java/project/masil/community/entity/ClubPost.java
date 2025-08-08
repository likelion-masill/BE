package project.masil.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "clubs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClubPost extends Post {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", nullable = false)
  private EventPost eventPost;

  @Setter
  @Column(name = "start_at", nullable = false)
  private LocalDateTime startAt;

}