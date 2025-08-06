package project.masil.auth.mapper;

import org.springframework.stereotype.Component;
import project.masil.auth.dto.response.LoginResponse;
import project.masil.user.entity.User;

@Component
public class AuthMapper {

  public LoginResponse toLoginResponse(User user, String accessToken, Long expirationTime) {
    return LoginResponse.builder()
        .accessToken(accessToken)
        .userId(user.getId())
        .email(user.getEmail())
        .role(user.getRole())
        .expirationTime(expirationTime)
        .build();
  }

}
