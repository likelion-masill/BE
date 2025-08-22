package project.masil.community.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import project.masil.community.enums.EventType;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EventPost extends Post {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id", nullable = false)
  private Region region;

  @Enumerated(EnumType.STRING)
  @Column(name = "eventType", length = 32, nullable = false)
  private EventType eventType;

  @Column(nullable = false)
  private LocalDateTime startAt;

  @Column(nullable = false)
  private LocalDateTime endAt;

  @Column(name = "summary")
  private String summary;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private int viewCount = 0;

  @Column(name = "is_UP", nullable = false)
  private boolean isUp; //Up 게시물인지 아닌지

  @Column(name = "up_at")
  private LocalDateTime upAt; // Up 시작 시각 (isUp=true일 때만 채움)

  @Column(name = "up_expires_at")
  private LocalDateTime upExpiresAt; // Up 만료 시각

  public void startUpForDays(int days) {
    this.isUp = true;
    this.upAt = LocalDateTime.now();
    this.upExpiresAt = this.upAt.plusDays(days);
  }

  public void stopUp() {
    this.isUp = false;
    this.upExpiresAt = null;
    this.upAt = null;
  }

  // 조회 시 만료된 경우 자동 해제하고 싶다면 헬퍼
  public void refreshUpStatusByNow() {
    if (this.isUp && this.upExpiresAt != null && this.upExpiresAt.isBefore(LocalDateTime.now())) {
      stopUp();
    }
  }



  @ElementCollection
  @CollectionTable(name = "event_images", joinColumns = @JoinColumn(name = "event_id"))
  @Column(name = "image_url", nullable = false)         // 값 컬럼 이름 지정
  @OrderColumn(name = "sequence")                       // 순서(인덱스) 컬럼 저장
  @Builder.Default
  private List<String> eventImages = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "eventPost", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ClubPost> clubPosts = new ArrayList<>();


  //이벤트 수정 메소드
  public void updateEventPost(Region region, EventType type, String title, String content,
      String location, LocalDateTime startAt, LocalDateTime endAt) {
    this.region = region;
    this.eventType = type;
    this.title = title;
    this.content = content;
    this.location = location;
    this.startAt = startAt;
    this.endAt = endAt;
  }

  //이미지 추가 메소드(수정에서 사용)
  public void addImages(List<String> urls) {
    if (urls == null || urls.isEmpty()) return;
    this.eventImages.addAll(urls);
  }


}