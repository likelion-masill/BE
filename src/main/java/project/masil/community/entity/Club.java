package project.masil.community.entity;

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
@Table(name = "clubs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Club {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long clubId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String location;

  @Column(nullable = false)
  private int likeCount = 0;

  @Column(nullable = false)
  private int commentCount = 0;

  @Column(name = "start_at", nullable = false)
  private LocalDateTime startAt;

  @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
  private List<Comment> comments;


}