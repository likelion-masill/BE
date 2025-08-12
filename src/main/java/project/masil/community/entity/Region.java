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
import project.masil.global.common.BaseTimeEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "regions")
public class Region extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 광역지방자치단체 (시·도) 이름 예: 서울특별시, 경기도, 전라남도
   */
  @Column(nullable = false)
  private String sido;

  /**
   * 기초지방자치단체 (시·군·구) 이름 예: 종로구, 수원시, 무안군
   */
  @Column(nullable = false)
  private String sigungu;


}