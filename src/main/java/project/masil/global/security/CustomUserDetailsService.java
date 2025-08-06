package project.masil.global.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import project.masil.global.exception.CustomException;
import project.masil.user.entity.User;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

/**
 * 사용자가 로그인할 때 입력한 이메일을 기반으로 DB에서 유저를 조회하고,
 * 그 유저 정보를 CustomUserDetails로 감싸서 Spring Security에 넘겨주는 클래스
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.info("✅ [UserDetailsService] DB에서 조회 시도한 이메일: {}", email);  // ✅ 추가
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    return new CustomUserDetails(user);
  }
}
