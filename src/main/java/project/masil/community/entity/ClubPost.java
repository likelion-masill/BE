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
import lombok.experimental.SuperBuilder;
import project.masil.community.dto.request.ClubPostRequest;

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

  @Column(name = "start_at", nullable = false)
  private LocalDateTime startAt;

  public void update(ClubPostRequest updateRequest) {
    this.title = updateRequest.getTitle();
    this.location = updateRequest.getLocation();
    this.startAt = updateRequest.getStartAt();
    this.content = updateRequest.getContent();
  }

  private static final String coverImage = "https://masilbucket.s3.ap-northeast-2.amazonaws.com/profile/de7a0231-0d36-4390-9b39-32e71a9fad4d";

  public String getCoverImage() {
    return coverImage;
  }

}