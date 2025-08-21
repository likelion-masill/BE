package project.masil.community.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.community.converter.EventPostConverter;
import project.masil.community.converter.RegionConverter;
import project.masil.community.dto.response.EventPostResponse;
import project.masil.community.entity.EventPost;
import project.masil.community.repository.EventPostRepository;
import project.masil.community.repository.FavoriteRepository;
import project.masil.community.repository.PostEmbeddingRepository;
import project.masil.embedding.service.UserEmbeddingService;
import project.masil.global.exception.CustomException;
import project.masil.infrastructure.client.ai.AiRerankService;
import project.masil.user.entity.User;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

  private final UserRepository userRepository;
  private final PostEmbeddingRepository postEmbeddingRepository;
  private final UserEmbeddingService userEmbeddingService;
  private final AiRerankService aiRerankService;
  private final EventPostRepository eventPostRepository;
  private final FavoriteRepository favoriteRepository;
  private final EventPostConverter converter;

  private final EventPostSearchService eventPostSearchService;

  public List<EventPostResponse> recommendByRegion(Long userId, int topK) {
    // 0) 유효성
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    Long regionId = user.getRegion().getId();

    // 1) 후보 id 조회 (region 기준)
    List<Long> candidateIds = postEmbeddingRepository.findPostIdsByRegionId(regionId);
    if (candidateIds.isEmpty()) {
      return List.of();
    }

    // 2) 사용자 임베딩 로드
    List<Float> userVec = userEmbeddingService.loadAsFloatList(userId);
    if (userVec == null) {
      // 콜드스타트: 최신/인기 등으로 대체 가능. 여기선 그냥 후보 최근순 로드 예시
      List<EventPost> rows = eventPostRepository.findRecentByIds(candidateIds, topK);
      return toResponses(userId, rows);
    }

    // 3) FAISS 서브셋 검색 (TopK)
    List<Long> rankedIds = aiRerankService.rerankSubset(candidateIds, userVec, topK);
    if (rankedIds.isEmpty()) {
      return List.of();
    }

    // 4) 순서 보존 로드
    List<EventPost> posts = eventPostSearchService.loadInOrder(rankedIds);

    // 5) 응답 변환
    return toResponses(userId, posts);
  }


  private List<EventPostResponse> toResponses(Long userId, List<EventPost> posts) {
    return posts.stream().map(p -> {
      boolean isLiked = favoriteRepository.existsByUserIdAndPostId(userId, p.getId());
      boolean isMine = p.getUser().getId().equals(userId);
      return converter.toResponse(p, isLiked, isMine,
          RegionConverter.toRegionResponse(p.getRegion()));
    }).toList();
  }
}
