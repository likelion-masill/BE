package project.masil.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.masil.community.dto.response.RegionIdResponse;
import project.masil.community.dto.response.SidoResponse;
import project.masil.community.dto.response.SigunguResponse;
import project.masil.community.service.RegionService;
import project.masil.global.response.BaseResponse;
import project.masil.global.response.ListResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/regions")
@Tag(name = "Region", description = "행정구역 관련 API")
@Slf4j
public class RegionController {

  private final RegionService regionService;

  @Operation(summary = "광역지방자치단체 조회", description = "한국의 광역지방자치단체 목록을 조회합니다.")
  @GetMapping("/sidos")
  public ResponseEntity<BaseResponse<ListResponse<SidoResponse>>> getSidoList() {
    List<SidoResponse> sidoList = regionService.getSidoList();
    return ResponseEntity.ok(
        BaseResponse.success("광역지방자치단체 목록 조회 성공",
            new ListResponse<>(sidoList.size(), sidoList))
    );
  }

  @Operation(summary = "기초지방자치단체 조회", description = "특정 광역지방자치단체에 속하는 기초지방자치단체 목록을 조회합니다.")
  @GetMapping("/sidos/{sido}/sigungus")
  public ResponseEntity<BaseResponse<ListResponse<SigunguResponse>>> getSigunguList(
      @PathVariable String sido) {
    List<SigunguResponse> sidoList = regionService.getSigunguList(sido);
    return ResponseEntity.ok(
        BaseResponse.success("기초지방자치단체 목록 조회 성공",
            new ListResponse<>(sidoList.size(), sidoList))
    );
  }

  @Operation(summary = "지역 ID 조회", description = "특정 광역지방자치단체와 기초지방자치단체에 해당하는 행정구역의 지역 ID를 조회합니다.")
  @GetMapping("id")
  public ResponseEntity<BaseResponse<RegionIdResponse>> getRegionId(
      @RequestParam String sido,
      @RequestParam String sigungu) {
    return ResponseEntity.ok(
        BaseResponse.success("지역 ID 조회 성공", regionService.getRegionId(sido, sigungu)));
  }
}
