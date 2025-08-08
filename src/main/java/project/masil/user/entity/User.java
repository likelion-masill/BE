package project.masil.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "users") //user테이블 이름은 꼭 users로 하기
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "username", nullable = false)
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

//  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//  @Builder.Default
//  private List<Favorite> likesList = new ArrayList<>();

//  @Column
//  private boolean isBusinessVerified;
//
//  @Column
//  private String profileImageUrl;
//
//  @Column
//  private String phoneNumber;

  public void createRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }


}