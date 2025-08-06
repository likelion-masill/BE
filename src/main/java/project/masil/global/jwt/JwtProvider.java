package project.masil.global.jwt;

import org.springframework.beans.factory.annotation.Value;
import project.masil.auth.exception.AuthErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import project.masil.global.exception.CustomException;

/**
 * JWT(Json Web Token)를 생성하고 검증하고 파싱하는 핵심 유틸리티 컴포넌트
 * - Spring Security + JWT 기반 인증 시스템에서 인증 관련 핵심 로직
 */
@Slf4j
@Component
public class JwtProvider {

  private final Key key;
  private final long accessTokenExpireTime;
  private final long refreshTokenExpireTime;

  public JwtProvider(
      @Value("${spring.jwt.secret}") String secretKey,
      @Value("${spring.jwt.access-token-expire-time}") long accessTokenExpireTime,
      @Value("${spring.jwt.refresh-token-expire-time}") long refreshTokenExpireTime) {
    byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.accessTokenExpireTime = accessTokenExpireTime;
    this.refreshTokenExpireTime = refreshTokenExpireTime;
  }

  public String createAccessToken(String email) {
    Date now = new Date();
    return Jwts.builder()
        .setSubject(email)
        .setId(String.valueOf(email))
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + accessTokenExpireTime))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public long getExpiration(String accessToken) {
    Claims claims = parseClaims(accessToken);

    Date expiration = claims.getExpiration();
    long now = System.currentTimeMillis();
    return expiration.getTime() - now;
  }

  private Key getSigningKey() {
    return key;
  }

  public String createRefreshToken(String username, String tokenId) {
    Date now = new Date();
    return Jwts.builder()
        .setSubject(username)
        .setId(tokenId)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + refreshTokenExpireTime))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (ExpiredJwtException e) {
      throw new CustomException(AuthErrorCode.JWT_TOKEN_EXPIRED);
    } catch (UnsupportedJwtException e) {
      throw new CustomException(AuthErrorCode.UNSUPPORTED_TOKEN);
    } catch (MalformedJwtException e) {
      throw new CustomException(AuthErrorCode.MALFORMED_JWT_TOKEN);
    } catch (io.jsonwebtoken.SignatureException e) {
      throw new CustomException(AuthErrorCode.INVALID_SIGNATURE);
    } catch (IllegalArgumentException e) {
      throw new CustomException(AuthErrorCode.ILLEGAL_ARGUMENT);
    }
  }

  public String extractEmail(String token) {
    return parseClaims(token).getSubject(); // ✅ subject에서 이메일 추출
  }

  public String extractSocialId(String token) {
    return parseClaims(token).getSubject();
  }

  public String extractTokenId(String token) {
    return parseClaims(token).getId();
  }

  private Claims parseClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}