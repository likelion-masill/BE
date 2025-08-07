package project.masil.community.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.masil.community.enums.PostType;
import project.masil.user.entity.User;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long commentId;

  @Enumerated(EnumType.STRING)
  private PostType postType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "club_id")
  private Club club;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_comment_id")
  private Comment parentComment;

  @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> replies;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String comment;
}