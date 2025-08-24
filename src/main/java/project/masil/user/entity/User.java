package project.masil.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.util.StringUtils;
import project.masil.community.entity.Favorite;
import project.masil.community.entity.Region;
import project.masil.global.common.BaseTimeEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "users") //user테이블 이름은 꼭 users로 하기
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(name = "username", nullable = false, unique = true)
  private String username; //사용자 이름(별칭)

  @Column(nullable = false, unique = true)
  private String email; //이메일(아이디)

  @JsonIgnore //실수로 프론트 측에게 보내더라도 넘어가지 않게 하겠다(민감한 정보)
  @Column(name = "password", nullable = false)
  private String password;

  @JsonIgnore
  @Column(name = "refresh_token")
  private String refreshToken;

  @Column(name = "role", nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Role role = Role.User;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Favorite> likesList = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id", nullable = false)
  private Region region;

  @Builder.Default
  @Column(nullable = false)
  @ColumnDefault("0")
  private boolean businessVerified = false;

  @Column(unique = true)
  private String businessNumber;

  @Setter
  @Column
  private String profileImageUrl;


  public String getProfileImageUrlOrDefault() {
    return (StringUtils.hasText(profileImageUrl))
        ? profileImageUrl
        : "https://masilbucket.s3.ap-northeast-2.amazonaws.com/profile/3eaf3db0-863b-4475-95d4-7d4dc9caba05";
  }

  public void createRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void verifyBusiness(String businessNumber) {
    this.businessVerified = true;
    this.businessNumber = businessNumber;
  }

  public void updateRegion(Region region) {
    this.region = region;
  }

}