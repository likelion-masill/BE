package project.masil.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import project.masil.community.dto.request.EventPostRequest;
import project.masil.community.dto.response.EventPostResponse;
import project.masil.community.enums.EventType;
import project.masil.community.service.EventPostService;
import project.masil.global.response.BaseResponse;
import project.masil.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Slf4j
@Tag(name = "Event API", description = "이벤트 API")
public class EventPostController {

  private final EventPostService eventPostService;

  @Operation(summary = "이벤트 생성", description = "이벤트 페이지에서 이벤트 생성버튼을 눌렀을때 요청되는 API")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  // consumes에서 이미지 타입을 제거하고 multipart/form-data만 사용
  public ResponseEntity<BaseResponse<EventPostResponse>> createEvent(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestPart("request") @Valid EventPostRequest eventPostRequest,
      @RequestPart(value = "images", required = false) List<MultipartFile> images
      // ← EventErrorCode에서 예외처리
  ) {
    EventPostResponse response = eventPostService.createEvent(userDetails.getUser().getId(),
        eventPostRequest, images);
    return ResponseEntity.ok(BaseResponse.success("이벤트 생성 성공", response));
  }

  @Operation(summary = "이벤트 단일 조회", description = "이벤트 단일 조회 API")
  @GetMapping("/{eventId}")
  public ResponseEntity<BaseResponse<EventPostResponse>> getEvent(@PathVariable Long eventId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    EventPostResponse response = eventPostService.getEventPost(eventId,
        userDetails.getUser().getId());
    return ResponseEntity.ok(BaseResponse.success("이벤트 단일 조회 성공", response));
  }

  @Operation(summary = "이벤트 리스트 전체 조회", description = "전체 이벤트 리스트 조회")
  @GetMapping("/all")
  public ResponseEntity<BaseResponse<Page<EventPostResponse>>> getAllEvents(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "1") Long regionId, // ← 지역 ID를 받는 파라미터 추가
      @RequestParam(defaultValue = "1") int page,          // ← 1부터 받기
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir
  ) {
    int pageIndex = Math.max(0, page - 1);                 // ← 0 기반으로 변환
    Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
    Pageable pageable = PageRequest.of(pageIndex, size, sort);

    Page<EventPostResponse> response = eventPostService.getEventAll(regionId, pageable,
        userDetails.getUser().getId());
    return ResponseEntity.ok(BaseResponse.success("이벤트 리스트 조회 성공", response));
  }

  @Operation(summary = "특정 이벤트 타입 리스트 조회", description = "특정 이벤트 타입의 이벤트 리스트 조회")
  @GetMapping("/eventType/list")
  public ResponseEntity<BaseResponse<Page<EventPostResponse>>> getEventTypeList(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "1") Long regionId,
      @RequestParam EventType eventType,
      @RequestParam(defaultValue = "1") int page,          // ← 1부터 받기
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir
  ) {
    // 페이지 번호를 0 기반으로 변환 (최소 0 보장)
    int pageIndex = Math.max(0, page - 1);
    // 정렬 방향 및 기준 설정
    Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
    Pageable pageable = PageRequest.of(pageIndex, size, sort);
    Page<EventPostResponse> eventTypeList = eventPostService.getEventTypeList(regionId, eventType,
        pageable, userDetails.getUser().getId());
    return ResponseEntity.ok(BaseResponse.success("이벤트 카테고리별 리스트 조회 성공", eventTypeList));

  }

  @Operation(summary = "이벤트 수정", description = "이벤트 페이지에서 이벤트 수정하기 눌렀을때 실행되는 API")
  @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  // consumes에서 이미지 타입을 제거하고 multipart/form-data만 사용
  public ResponseEntity<BaseResponse<EventPostResponse>> updateEvent(
      @PathVariable Long eventId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestPart("request") @Valid EventPostRequest eventPostRequest,
      @RequestPart(value = "eventImages", required = false) List<MultipartFile> images
  ) {
    EventPostResponse response = eventPostService.updateEvent(
        eventId,
        userDetails.getUser().getId(),
        eventPostRequest,
        images);
    return ResponseEntity.ok(BaseResponse.success("이벤트 수정 성공", response));
  }

  @Operation(summary = "이벤트 삭제", description = "이벤트 페이지에서 이벤트 삭제하기 눌렀을때 실행되는 API")
  @DeleteMapping("/{eventId}")
  public ResponseEntity<BaseResponse<Boolean>> deletePost(@PathVariable Long eventId) {
    boolean ok = eventPostService.deleteEvent(eventId);
    return ResponseEntity.ok(BaseResponse.success("이벤트 삭제 성공", ok));
  }

  @Operation(summary = "이벤트 UP 시작", description = "결제 완료 후 n일 동안 상단 노출")
  @PostMapping("/{eventId}/up/start")
  public ResponseEntity<BaseResponse<EventPostResponse>> startUp(
      @PathVariable Long eventId,
      @RequestParam(defaultValue = "7") int days,                  // 예: 7일
      @AuthenticationPrincipal CustomUserDetails user
  ) {
    EventPostResponse res = eventPostService.startUp(eventId, user.getUser().getId(), days);
    return ResponseEntity.ok(BaseResponse.success("UP 시작", res));
  }

  @Operation(summary = "이벤트 UP 중지", description = "만료 전이라도 즉시 해제")
  @PostMapping("/{eventId}/up/stop")
  public ResponseEntity<BaseResponse<EventPostResponse>> stopUp(
      @PathVariable Long eventId,
      @AuthenticationPrincipal CustomUserDetails user
  ) {
    EventPostResponse response = eventPostService.stopUp(eventId, user.getUser().getId());
    return ResponseEntity.ok(BaseResponse.success("UP 중지", response));
  }

  @Operation(summary = "이벤트 UP 상태 조회", description = "isUp/남은 기간 확인")
  @GetMapping("/{eventId}/up/status")
  public ResponseEntity<BaseResponse<EventPostResponse>> getUpStatus(
      @PathVariable Long eventId,
      @AuthenticationPrincipal CustomUserDetails user
  ) {
    EventPostResponse response = eventPostService.getEventStatus(eventId, user.getUser().getId());
    return ResponseEntity.ok(BaseResponse.success("이벤트 Up 상태 조회 성공", response));
  }


}
