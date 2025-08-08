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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
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
  @Column(nullable = false)
  private EventType eventType;

  @Column(nullable = false)
  private LocalDateTime startAt;

  @Column(nullable = false)
  private LocalDateTime endAt;

  @Column(name = "summary")
  private String summary;

  @Column(nullable = false)
  private int viewCount = 0;

  @ElementCollection
  @CollectionTable(name = "event_images", joinColumns = @JoinColumn(name = "event_id"))
  @Column(name = "image_url", nullable = false)         // 값 컬럼 이름 지정
  @OrderColumn(name = "sequence")                       // 순서(인덱스) 컬럼 저장
  private List<String> eventImages = new ArrayList<>();

  @OneToMany(mappedBy = "eventPost", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ClubPost> clubPosts;


}