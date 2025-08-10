package project.masil.user.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.masil.global.config.S3.AmazonS3Manager;
import project.masil.global.config.S3.Uuid;
import project.masil.global.config.S3.UuidRepository;
import project.masil.global.exception.CustomException;
import project.masil.user.dto.request.NicknameUpdateRequest;
import project.masil.user.dto.request.SignUpRequest;
import project.masil.user.dto.response.NicknameCheckResponse;
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
  private final UuidRepository uuidRepository;
  private final AmazonS3Manager s3Manager;

  @Transactional
  public SignUpResponse signUp(SignUpRequest request) {
    // 이메일과 사용자 이름(닉네임) 중복 검사
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new CustomException(UserErrorCode.EMAIL_ALREADY_EXISTS);
    }
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new CustomException(UserErrorCode.USERNAME_ALREADY_EXISTS);
    }

    // TODO: 휴대폰 번호 인증 여부 검증 로직 추가 필요

    // 비밀번호 인코딩
    String encodePassword = passwordEncoder.encode(request.getPassword());

    // UserMapper를 통해 Entity 생성
    User user = userMapper.toEntity(request, encodePassword);

    // 저장 및 로깅
    User savedUser = userRepository.save(user);
    log.info("[서비스] 회원가입 성공: Email = {}, Username = {}", savedUser.getEmail(),
        savedUser.getUsername());

    return userMapper.toSignUpResponse(savedUser);
  }

  public NicknameCheckResponse checkNickname(String nickname) {
    boolean exists = userRepository.existsByUsername(nickname);
    return NicknameCheckResponse.builder()
        .nickname(nickname)
        .duplicate(exists)
        .build();
  }

  @Transactional
  public String changeNickname(Long userId, NicknameUpdateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    if (userRepository.existsByUsername(request.getNickname())) {
      throw new CustomException(UserErrorCode.USERNAME_ALREADY_EXISTS);
    }

    user.setUsername(request.getNickname());
    log.info("[서비스] 닉네임 변경 성공: UserId = {}, NewNickname = {}", userId, request.getNickname());

    return user.getUsername();
  }

  @Transactional
  public String uploadProfileImage(Long userId, MultipartFile image) {
    String uuid = UUID.randomUUID().toString();
    Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
    String imageUrl = s3Manager.uploadFile(s3Manager.generateProfile(savedUuid), image);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    user.setProfileImageUrl(imageUrl);

    return imageUrl;

  }


}
