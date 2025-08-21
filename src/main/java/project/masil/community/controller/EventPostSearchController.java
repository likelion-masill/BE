package project.masil.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.masil.community.dto.response.EventPostResponse;
import project.masil.community.service.EventPostSearchService;
import project.masil.community.service.RecommendationService;
import project.masil.global.response.BaseResponse;
import project.masil.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Tag(name = "Event Search API", description = "이벤트 검색 API")
public class EventPostSearchController {

  private final EventPostSearchService eventPostSearchService;
  private final RecommendationService recommendationService;

  @Operation(summary = "키워드 검색", description = "키워드로 이벤트를 검색하는 API")
  @GetMapping("/search")
  public ResponseEntity<BaseResponse<Page<EventPostResponse>>> search(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam String query,
      @RequestParam(defaultValue = "1") int page,          // ← 1부터 받기
      @RequestParam(defaultValue = "20") int size
  ) {
    int pageIndex = Math.max(0, page - 1);
    Pageable pageable = PageRequest.of(pageIndex, size);
    return ResponseEntity.ok(
        BaseResponse.success(
            "AI 검색 결과 조회 성공",
            eventPostSearchService.search(userDetails.getUser().getId(), query, pageable)
        )
    );
  }

  @Operation(summary = "AI 검색", description = "AI 검색을 통해 이벤트를 검색하는 API")
  @GetMapping("/search-ai")
  public ResponseEntity<BaseResponse<List<EventPostResponse>>> searchEventsByAi(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam String query) {
    return ResponseEntity.ok(
        BaseResponse.success(
            "AI 검색 결과 조회 성공",
            eventPostSearchService.searchByAI(userDetails.getUser().getId(), query)
        )
    );
  }


  @Operation(summary = "AI 추천 이벤트 조회", description = "AI 추천 이벤트를 조회하는 API")
  @GetMapping("/ai-recommendations")
  public ResponseEntity<BaseResponse<Page<EventPostResponse>>> getAIRecommendedEvents(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "1") int page,          // ← 1부터 받기
      @RequestParam(defaultValue = "20") int size
  ) {
    int pageIndex = Math.max(0, page - 1);
    Pageable pageable = PageRequest.of(pageIndex, size);
    return ResponseEntity.ok(
        BaseResponse.success("AI 추천 이벤트 조회 성공",
            recommendationService.recommendByAI(
                userDetails.getUser().getId(), pageable
            )));
  }


}
