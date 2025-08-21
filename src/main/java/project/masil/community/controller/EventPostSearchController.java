package project.masil.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
  public ResponseEntity<BaseResponse<List<EventPostResponse>>> getAIRecommendedEvents(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ResponseEntity.ok(
        BaseResponse.success("AI 추천 이벤트 조회 성공",
            recommendationService.recommendByAI(
                userDetails.getUser().getId(), size

            )));
  }


}
