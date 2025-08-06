package project.masil.global.security;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import project.masil.user.entity.User;

/**
 * Spring Security가 로그인 후 인증된 유저 정보를 다룰 수 있도록 만든
 * User -> UserDetails 변환 어댑터
 *
 * Spring Security는 로그인 후 사용자 정보를 세션(SecurityContext)에 저장하는데,
 * 이때 User 엔티티를 그대로 저장하지 않고, UserDetails 인터페이스를 구현한 객체만 저장
 */
@Getter
public class CustomUserDetails implements UserDetails {

  private final User user;

  public CustomUserDetails(User user) {
    this.user = user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(user.getRole().name()));
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }



}
