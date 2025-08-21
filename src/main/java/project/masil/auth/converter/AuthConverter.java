package project.masil.auth.converter;

import org.springframework.stereotype.Component;
import project.masil.auth.dto.response.LoginResponse;
import project.masil.user.entity.User;

@Component
public class AuthConverter {

  public LoginResponse toLoginResponse(User user, String accessToken, Long expirationTime) {
    return LoginResponse.builder()
        .accessToken(accessToken)
        .userId(user.getId())
        .nickname(user.getUsername())
        .profileImageUrl(user.getProfileImageUrlOrDefault())
        .email(user.getEmail())
        .regionId(user.getRegion().getId())
        .role(user.getRole())
        .expirationTime(expirationTime)
        .build();
  }

}
