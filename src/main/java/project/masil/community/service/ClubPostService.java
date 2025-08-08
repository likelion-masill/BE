package project.masil.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.masil.community.dto.request.ClubPostCreateRequest;
import project.masil.community.dto.response.ClubPostDetailResponse;
import project.masil.community.entity.ClubPost;
import project.masil.community.repository.ClubPostRepository;
import project.masil.global.exception.CustomException;
import project.masil.user.entity.User;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubPostService {

  private final ClubPostRepository clubPostRepository;
  private final UserRepository userRepository;

  public ClubPostDetailResponse createClubPost(Long userId, Long eventId,
      ClubPostCreateRequest createRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    ClubPost clubPost = ClubPost.builder()
        .user(user)
        .build();

    return null;

  }


}
