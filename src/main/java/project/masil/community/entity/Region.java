package project.masil.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "regions")
public class Region {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long regionId;

  @Column(nullable = false)
  private String sido;

  @Column(nullable = false)
  private String sigungu;


}