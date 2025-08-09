package project.masil.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.masil.auth.dto.request.LoginRequest;
import project.masil.auth.dto.response.LoginResponse;
import project.masil.auth.service.AuthService;
import project.masil.global.exception.CustomException;
import project.masil.global.response.BaseResponse;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auths")
@Tag(name = "Auth", description = "Auth 관리 API")
public class AuthController {

  private final AuthService authService;
  private final UserRepository userRepository;

  @Operation(summary = "사용자 로그인", description = "사용자 로그인을 위한 API")
  @PostMapping("/login")
  public ResponseEntity<BaseResponse<LoginResponse>> login(
      @RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
    LoginResponse loginResponse = authService.login(loginRequest);

    // refreshToken 가져오기
    String refreshToken = userRepository.findByEmail(loginResponse.getEmail())
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND))
        .getRefreshToken();

    // Set-Cookie 설정 (HttpOnly + Secure)
    Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    //refreshTokenCookie.setSecure(tre); // HTTPS일 때만
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7); // 예: 7일

    response.addCookie(refreshTokenCookie);

    return ResponseEntity.ok(BaseResponse.success("로그인에 성공했습니다.", loginResponse));

  }
}
