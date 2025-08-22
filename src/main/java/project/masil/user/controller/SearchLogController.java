package project.masil.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.masil.global.response.BaseResponse;
import project.masil.global.response.ListResponse;
import project.masil.user.service.SearchLogService;

@Tag(name = "Search Log API", description = "검색 기록 API")
@RestController
@AllArgsConstructor
@RequestMapping("/search-logs")
public class SearchLogController {

  private final SearchLogService searchLogService;

  @Operation(summary = "최근 검색어 10개(최신순)", description = "최근 검색어 10개를 최신순으로 조회하는 API")
  @GetMapping
  public ResponseEntity<BaseResponse<ListResponse<String>>> recent() {
    return ResponseEntity.ok(
        BaseResponse.success(
            "최근 검색어 10개 조회 성공",
            searchLogService.getRecent10()
        )
    );
  }
}
