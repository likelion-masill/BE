package project.masil.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import project.masil.community.dto.request.EventPostRequest;
import project.masil.community.dto.response.EventPostResponse;
import project.masil.community.service.EventPostService;
import project.masil.global.response.BaseResponse;
import project.masil.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventPostController {

  private final EventPostService eventPostService;

  @Operation(summary = "이벤트 생성", description = "이벤트 페이지에서 이벤트 생성버튼을 눌렀을때 요청되는 API")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // consumes에서 이미지 타입을 제거하고 multipart/form-data만 사용
  public ResponseEntity<BaseResponse<EventPostResponse>> createEvent(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestPart("request") @Valid EventPostRequest eventPostRequest,
      @RequestPart(value = "eventImages", required = false) List<MultipartFile> images
  ) {
    EventPostResponse response = eventPostService.createEvent(userDetails.getUser().getId(),
        eventPostRequest, images);
    return ResponseEntity.ok(BaseResponse.success("이벤트 생성 성공", response));
  }


}
