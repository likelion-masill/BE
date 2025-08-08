package project.masil.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import project.masil.global.common.BaseTimeEntity;
import project.masil.user.entity.User;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Post extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Setter
  @Column(nullable = false)
  private String title;

  @Setter
  @Column(nullable = false)
  private String location;

  @Setter
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false)
  private int favoriteCount = 0;

  @Column(nullable = false)
  private int commentCount = 0;

  @OneToMany(mappedBy = "post")
  private List<Comment> comments;

  @OneToMany(mappedBy = "post")
  private List<Favorite> favorites;

  public void incrementFavoriteCount() {
    this.favoriteCount++;
  }

  public void decrementFavoriteCount() {
    if (this.favoriteCount > 0) {
      this.favoriteCount--;
    }
  }

  public void incrementCommentCount() {
    this.commentCount++;
  }

  public void decrementCommentCount() {
    if (this.commentCount > 0) {
      this.commentCount--;
    }
  }
}
