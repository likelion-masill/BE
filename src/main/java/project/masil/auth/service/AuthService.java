package project.masil.auth.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.auth.dto.request.LoginRequest;
import project.masil.auth.dto.response.LoginResponse;
import project.masil.auth.mapper.AuthMapper;
import project.masil.global.exception.CustomException;
import project.masil.global.jwt.JwtProvider;
import project.masil.user.entity.User;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtProvider jwtProvider;
  private final UserRepository userRepository;
  private final AuthMapper authMapper;

  @Transactional
  public LoginResponse login(LoginRequest loginRequest) {
    User user = userRepository.findByEmail(loginRequest.getEmail())
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
        loginRequest.getEmail(), loginRequest.getPassword());

    // 인증 처리
    authenticationManager.authenticate(authenticationToken);

    // 액세스 토큰 및 리프레시 토큰 발급
    String accessToken = jwtProvider.createAccessToken(user.getEmail());
    String refreshToken = jwtProvider.createRefreshToken(user.getEmail(),
        UUID.randomUUID().toString());

    // 리프레시 토큰 저장
    user.createRefreshToken(refreshToken);

    // Access Token의 만료 시간을 가져옴
    Long expirationTime = jwtProvider.getExpiration(accessToken);

    // 로그인 성공 로깅
    log.info("로그인 성공: {}", user.getEmail());

    // 로그인 응답 반환
    return authMapper.toLoginResponse(user, accessToken, expirationTime);
  }

}