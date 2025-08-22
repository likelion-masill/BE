package project.masil.community.service;

import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.community.converter.EventPostConverter;
import project.masil.community.converter.RegionConverter;
import project.masil.community.dto.response.EventPostResponse;
import project.masil.community.entity.EventPost;
import project.masil.community.enums.EventType;
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

  public Page<EventPostResponse> recommendByAI(
      Long userId,
      @Nullable EventType eventType,
      boolean today,
      Pageable pageable
  ) {
    // 0) 유효성
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    Long regionId = user.getRegion().getId();

    // 1) 후보 IDs 조회 (요구사항 우선순위 적용)
    List<Long> candidateIds;
    if (today) {
      // 오늘과 일정이 겹치는 이벤트만 (Asia/Seoul 기준)
      ZoneId KST = ZoneId.of("Asia/Seoul");
      LocalDateTime startOfDay = LocalDate.now(KST).atStartOfDay();
      LocalDateTime endOfDay = LocalDate.now(KST).atTime(LocalTime.MAX);

      candidateIds = postEmbeddingRepository.findPostIdsByRegionIdAndActiveOnDate(
          regionId, startOfDay, endOfDay
      );

    } else if (eventType != null) {
      // 지역 + 이벤트 타입
      candidateIds = postEmbeddingRepository.findPostIdsByRegionIdAndEventType(
          regionId, eventType
      );

    } else {
      // 지역만 (모든 타입)
      candidateIds = postEmbeddingRepository.findPostIdsByRegionId(regionId);
    }

    long total = candidateIds.size();
    if (total == 0) {
      return Page.empty(pageable);
    }

    int page = pageable.getPageNumber();
    int size = pageable.getPageSize();
    int from = page * size;

    // 전체 후보 개수(total)보다 시작 인덱스(from)가 크거나 같으면 더 이상 데이터가 없으므로 빈 페이지 반환
    if (from >= total) {
      return Page.empty(pageable);
    }

    // 2) 사용자 임베딩 로드
    List<Float> userVec = userEmbeddingService.loadAsFloatList(userId);

    // --- 콜드스타트: 최신순 페이징 ---
    if (userVec == null) {
      List<EventPost> rows = eventPostRepository.findRecentByIdsPage(
          candidateIds, from, size
      );
      List<EventPostResponse> content = toResponses(userId, rows);
      return new PageImpl<>(content, pageable, total);
    }

    // --- 재랭킹: 과조회 후 컷 ---
    int needTop = (int) Math.min((long) (page + 1) * size, total);

    // FAISS 서브셋 검색 (Top needTop)
    List<Long> rankedTopIds = aiRerankService.rerankSubset(candidateIds, userVec, needTop);
    if (rankedTopIds.isEmpty()) {
      return Page.empty(pageable);
    }

    int to = Math.min(from + size, rankedTopIds.size());
    if (from >= to) {
      return Page.empty(pageable);
    }

    List<Long> pageIds = rankedTopIds.subList(from, to);

    // 4) 순서 보존 로드
    List<EventPost> posts = eventPostSearchService.loadInOrder(pageIds);

    // 5) 응답 변환
    List<EventPostResponse> content = toResponses(userId, posts);
    return new PageImpl<>(content, pageable, total);
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
