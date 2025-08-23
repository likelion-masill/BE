package project.masil.community.service;

import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

  private long hourlySeed() {
    String time = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
    return Long.parseLong(time);
  }


  public Page<EventPostResponse> recommendByAI(
      Long userId,
      @Nullable EventType eventType,
      boolean today,
      Pageable pageable
  ) {
    long seed = hourlySeed();
    // 0) 유효성
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    Long regionId = user.getRegion().getId();

    // 광고 추가
    List<Long> adIds;
    if (today) {
      ZoneId KST = ZoneId.of("Asia/Seoul");
      LocalDateTime startOfDay = LocalDate.now(KST).atStartOfDay();
      LocalDateTime endOfDay = LocalDate.now(KST).atTime(LocalTime.MAX);

      adIds = eventPostRepository.findActiveAdPostIds(regionId, eventType, startOfDay, endOfDay,
          seed);

    } else if (eventType != null) {
      adIds = eventPostRepository.findAdPostIdsByType(regionId, eventType, seed);
    } else {
      adIds = eventPostRepository.findAdPostIds(regionId, seed);
    }

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

    candidateIds.removeAll(adIds);

    long total = adIds.size() + candidateIds.size();
    if (total == 0) {
      return Page.empty(pageable);
    }

    // 3) 사용자 임베딩
    List<Float> userVec = userEmbeddingService.loadAsFloatList(userId);

    // --- 광고 먼저 로드 ---
    List<EventPost> adPosts = eventPostSearchService.loadInOrder(adIds);
    List<EventPostResponse> adResponses = toResponses(userId, adPosts);

    // --- 이후 AI 추천 (기존 로직 그대로) ---
    List<EventPostResponse> aiResponses;
    if (userVec == null) {
      List<EventPost> rows = eventPostRepository.findRecentByIdsPage(candidateIds, 0,
          pageable.getPageSize());
      aiResponses = toResponses(userId, rows);
    } else {
      int needTop = (int) Math.min((long) (pageable.getPageNumber() + 1) * pageable.getPageSize(),
          candidateIds.size());
      List<Long> rankedTopIds = aiRerankService.recommendByAI(candidateIds, userVec, needTop);
      List<EventPost> posts = eventPostSearchService.loadInOrder(rankedTopIds);
      aiResponses = toResponses(userId, posts);
    }

    // 4) 광고 + 추천 합치기
    List<EventPostResponse> content = new ArrayList<>();
    content.addAll(adResponses);
    content.addAll(aiResponses);

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
