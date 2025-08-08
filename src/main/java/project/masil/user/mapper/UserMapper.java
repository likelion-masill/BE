package project.masil.user.mapper;

import org.springframework.stereotype.Component;
import project.masil.user.dto.request.SignUpRequest;
import project.masil.user.dto.response.SignUpResponse;
import project.masil.user.entity.Role;
import project.masil.user.entity.User;

@Component
public class UserMapper {

  public SignUpResponse toSignUpResponse(User user) {
    return SignUpResponse.builder()
        .userId(user.getId())
        .nickname(user.getUsername())
        .email(user.getEmail())
        .build();
  }

  public User toEntity(SignUpRequest request, String encodedPassword) {
    return User.builder()
        .email(request.getEmail())
        .password(encodedPassword)
        .username(request.getUsername())
        .role(Role.User)
        .build();
  }

}