package project.masil.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import project.masil.global.security.JwtAuthenticationFilter;

//Security Config 설정
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CorsConfig corsConfig;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // CSRF 보호 기능 비활성화 (REST API에서는 필요없음)
        .csrf(AbstractHttpConfigurer::disable)
        // CORS 설정 활성화(보통은 CORS 설정 활성화 하지 않음. 서버에서 NginX로 CORS 검증)
        .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
        // 기본 인증(HTTP Basic)과 폼 로그인 비활성화
        // → 브라우저 팝업 및 기본 로그인 페이지 차단, JWT 인증만 사용
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        // 세션을 생성하지 않음 (JWT 사용으로 인한 Stateless 설정)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // HTTP 요청에 대한 권한 설정
        .authorizeHttpRequests(
            request ->
                request
                    // Swagger 경로 인증 허용
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    // 인증 없이 허용할 경로
                    .requestMatchers("/users/sign-up", "/auth/login", "/users/nickname/check")
                    .permitAll()

                    // WebSocket/SockJS 핸드셰이크 & 내부 전송 경로 전부 허용
                    // context-path(/api) 붙이지 않습니다!
                    .requestMatchers("/websocket/**", "/ws/**").permitAll()

                    // 테스트 페이지(정적) 허용 (정적 리소스도 context-path 제거)
                    .requestMatchers("/ws-test.html").permitAll()

                    // EventPost 조회 관련 API 인증 허용
                    .requestMatchers(HttpMethod.GET, "/events/*")
                    .permitAll()

                    // ClubPost 조회 관련 API 인증 허용
                    .requestMatchers(HttpMethod.GET, "/events/*/clubs/*")
                    .permitAll()

                    // 지역 정보 조회 API 인증 허용
                    .requestMatchers("/regions/**")
                    .permitAll()

                    // 이벤트 대댓글 조회
                    .requestMatchers(HttpMethod.GET, "/events/{eventId}/{commentId}/replies")
                    .permitAll()
                    // 이벤트 댓글 조회
                    .requestMatchers(HttpMethod.GET, "/events/{eventId}/comments").permitAll()
                    // 소모임 대댓글 조회
                    .requestMatchers(HttpMethod.GET,
                        "/events/{eventId}/clubs/{clubId}/{commentId}/replies").permitAll()
                    // 소모임 댓글 조회
                    .requestMatchers(HttpMethod.GET, "/events/{eventId}/clubs/{clubId}/comments")
                    .permitAll()

                    // 그 외 모든 요청은 모두 인증 필요
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /**
   * 비밀번호 인코더 Bean 등록
   **/
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * 인증 관리자 Bean 등록
   **/
  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

}