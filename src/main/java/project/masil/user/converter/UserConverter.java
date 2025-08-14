package project.masil.user.converter;

import org.springframework.stereotype.Component;
import project.masil.community.entity.Region;
import project.masil.user.dto.request.SignUpRequest;
import project.masil.user.dto.response.SignUpResponse;
import project.masil.user.entity.Role;
import project.masil.user.entity.User;

@Component
public class UserConverter {

  public SignUpResponse toSignUpResponse(User user) {
    return SignUpResponse.builder()
        .userId(user.getId())
        .nickname(user.getUsername())
        .email(user.getEmail())
        .regionId(user.getRegion().getId())
        .build();
  }

  public User toEntity(SignUpRequest request, String encodedPassword, Region region) {
    return User.builder()
        .email(request.getEmail())
        .password(encodedPassword)
        .username(request.getUsername())
        .role(Role.User)
        .region(region)
        .build();
  }
}
