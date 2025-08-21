package project.masil.community.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.community.converter.EventPostConverter;
import project.masil.community.converter.RegionConverter;
import project.masil.community.dto.response.EventPostResponse;
import project.masil.community.entity.EventPost;
import project.masil.community.entity.Region;
import project.masil.community.enums.EventType;
import project.masil.community.repository.EventPostRepository;
import project.masil.community.repository.FavoriteRepository;
import project.masil.community.repository.spec.EventPostSpecs;
import project.masil.embedding.service.EmbeddingPipelineService;
import project.masil.global.exception.CustomException;
import project.masil.global.util.parser.EventTypeParser;
import project.masil.global.util.parser.KoreanTimeParser;
import project.masil.global.util.parser.KoreanTimeParser.TimeSpan;
import project.masil.global.util.parser.RegionParser;
import project.masil.infrastructure.client.ai.AiRerankService;
import project.masil.user.entity.User;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPostSearchService {

  private final EventPostRepository eventPostRepository;
  private final EventPostRepository repo;
  private final KoreanTimeParser koreanTimeParser;
  private final EventTypeParser eventTypeParser;
  private final RegionParser regionParser;

  private final EmbeddingPipelineService embeddingPipelineService;
  private final AiRerankService aiRerankService;

  private final EventPostConverter converter;
  private final FavoriteRepository favoriteRepository;

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public Page<EventPostResponse> search(Long userId, String keyword, Pageable pageable) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    Long regionId = user.getRegion().getId();
    if (regionId == null) {
      return Page.empty(pageable);
    }

    int size = pageable.getPageSize();
    int offset = (int) pageable.getOffset();

    List<Object[]> rows = repo.searchPostIdsByKeywordInRegion(keyword, regionId, size, offset);
    List<Long> ids = rows.stream()
        .map(r -> ((Number) r[0]).longValue())
        .toList();
    if (ids.isEmpty()) {
      return Page.empty(pageable);
    }

    String orderCsv = ids.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(","));

    List<EventPost> posts = repo.findAllByIdInOrder(ids, orderCsv);
    long total = repo.countByKeywordInRegion(keyword, regionId);

    List<EventPostResponse> content = posts.stream()
        .map(post -> {
          boolean isLiked = favoriteRepository.existsByUserIdAndPostId(userId, post.getId());
          boolean isMine = post.getUser().getId().equals(userId);
          return converter.toResponse(
              post, isLiked, isMine, RegionConverter.toRegionResponse(post.getRegion()));
        })
        .toList();

    return new PageImpl<>(content, pageable, total);
  }

  @Transactional(readOnly = true)
  public List<Long> findEventPostIds(
      LocalDateTime from, LocalDateTime to,
      String sido, String sigungu,
      EventType category,
      int limit
  ) {
    var spec = Specification.allOf(EventPostSpecs.startEndBetween(from, to))
        .and(EventPostSpecs.matchSido(sido))
        .and(EventPostSpecs.matchSigungu(sigungu))
        .and(EventPostSpecs.matchCategory(category));

    Sort sort = Sort.by(Sort.Direction.DESC, "startAt");
    Pageable pageable = PageRequest.of(0, Math.max(1, limit), sort);

    return repo.findAll(spec, pageable)
        .stream()
        .map(EventPost::getId)
        .toList();
  }

  public List<EventPostResponse> searchByAI(Long userId, String query) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    Region region = user.getRegion();
    String sido = region.getSido();
    String sigungu = region.getSigungu();

    // 쿼리 정보 추출

    // 1) 시간 정보 추출
    TimeSpan str = KoreanTimeParser.parse(query, LocalDateTime.now());
    // 2) 이벤트 종류 추출
    EventType category = eventTypeParser.parseTop1(query);

    // 3) 필수 슬롯 이용하여 DB에서 후보 ID 뽑기
    List<Long> candidateIds = findEventPostIds(str.start(), str.end(), sido,
        sigungu, category, 100);

    log.info("후보 id 널 확인 : {}", candidateIds.isEmpty());
    if (candidateIds.isEmpty()) {
      return List.of();
    }

    // 4) 쿼리 텍스트 임베딩 구하기
    List<Float> q = embeddingPipelineService.requestEmbedding(query);

    // 5) 파이썬 서버에서 코사인 유사도 재랭킹 (topK=10)
    List<Long> rankedIds = aiRerankService.rerankSubset(candidateIds, q, 10);

    if (rankedIds.isEmpty()) {
      return List.of();
    }
    for (Long id : rankedIds) {
      log.debug("랭크된 ID: {}", id);
    }

    // 6) 순서 보존 로딩 + 응답 변환
    List<EventPost> posts = loadInOrder(rankedIds);

    return posts.stream()
        .map(post -> {
          boolean isLiked = favoriteRepository.existsByUserIdAndPostId(
              post.getUser().getId(), post.getId());
          boolean isMine = post.getUser().getId().equals(userId);
          return converter.toResponse(
              post,
              isLiked,
              isMine,
              RegionConverter.toRegionResponse(post.getRegion()));
        })
        .toList();
  }

  public List<EventPost> loadInOrder(List<Long> orderedIds) {
    if (orderedIds.isEmpty()) {
      return List.of();
    }
    List<EventPost> rows = eventPostRepository.findAllById(orderedIds);
    Map<Long, Integer> pos = new HashMap<>();
    for (int i = 0; i < orderedIds.size(); i++) {
      pos.put(orderedIds.get(i), i);
    }
    rows.sort(
        Comparator.comparingInt(r -> pos.getOrDefault(r.getId(), Integer.MAX_VALUE)));
    return rows;
  }

}