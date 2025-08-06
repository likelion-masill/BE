package project.masil.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.global.exception.CustomException;
import project.masil.user.dto.request.SignUpRequest;
import project.masil.user.dto.response.SignUpResponse;
import project.masil.user.entity.User;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.mapper.UserMapper;
import project.masil.user.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  @Transactional
  public SignUpResponse signUp(SignUpRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new CustomException(UserErrorCode.USERNAME_ALREADY_EXISTS);
    }

    // 비밀번호 인코딩
    String encodePassword = passwordEncoder.encode(request.getPassword());

    // UserMapper를 통해 Entity 생성
    User user = userMapper.toEntity(request, encodePassword);

    // 저장 및 로깅
    User savedUser = userRepository.save(user);
    log.info("New user registered: {}", savedUser.getUsername());

    return userMapper.toSignUpResponse(savedUser);
  }

}
