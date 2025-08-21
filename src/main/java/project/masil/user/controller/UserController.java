package project.masil.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import project.masil.community.dto.request.RegionUpdateRequest;
import project.masil.community.dto.response.RegionIdResponse;
import project.masil.global.response.BaseResponse;
import project.masil.global.security.CustomUserDetails;
import project.masil.user.dto.request.NicknameUpdateRequest;
import project.masil.user.dto.request.SignUpRequest;
import project.masil.user.dto.response.NicknameCheckResponse;
import project.masil.user.dto.response.ProfileImageUpdateResponse;
import project.masil.user.dto.response.SignUpResponse;
import project.masil.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Users", description = "User 관리 API")
public class UserController {

  private final UserService userService;

  @Operation(summary = "회원가입 API", description = "사용자 회원가입을 위한 API")
  @PostMapping("/sign-up")
  public ResponseEntity<BaseResponse<SignUpResponse>> signUp(
      @RequestBody @Valid SignUpRequest signUpRequest) {
    SignUpResponse signUpResponse = userService.signUp(signUpRequest);
    return ResponseEntity.ok(BaseResponse.success("회원가입에 성공했습니다.", signUpResponse));
  }

  @Operation(summary = "별명 중복 확인 API", description = "사용자 별명 중복 확인을 위한 API")
  @GetMapping("/nickname/check")
  public ResponseEntity<BaseResponse<NicknameCheckResponse>> checkNickname(
      @RequestParam String nickname) {
    NicknameCheckResponse response = userService.checkNickname(nickname);
    return ResponseEntity.ok(BaseResponse.success("별명 중복 확인 성공", response));
  }

  @Operation(summary = "닉네임 변경 API", description = "사용자 닉네임 변경을 위한 API")
  @PatchMapping("/me/nickname")
  public ResponseEntity<BaseResponse<String>> updateNickname(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid NicknameUpdateRequest nicknameUpdateRequest) {
    String updatedNickname = userService.changeNickname(
        userDetails.getUser().getId(),
        nicknameUpdateRequest);
    return ResponseEntity.ok(BaseResponse.success("닉네임 변경 성공", updatedNickname));
  }

  @Operation(summary = "프로필 이미지 업로드 API", description = "사용자 프로필 이미지 업로드를 위한 API")
  @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseResponse<ProfileImageUpdateResponse>> uploadProfileImage(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestPart(value = "profileImage", required = true) MultipartFile image) {
    return ResponseEntity.ok(BaseResponse.success("프로필 이미지 업로드 성공",
        userService.uploadProfileImage(userDetails.getUser().getId(), image)));

  }

  @Operation(summary = "사용자 지역 정보 변경 API", description = "사용자의 지역 정보를 변경하는 API")
  @PatchMapping("/me/region")
  public ResponseEntity<BaseResponse<RegionIdResponse>> updateRegion(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid RegionUpdateRequest regionUpdateRequest) {
    RegionIdResponse updatedRegion = userService.updateRegion(
        userDetails.getUser().getId(),
        regionUpdateRequest);
    return ResponseEntity.ok(BaseResponse.success("지역 정보 변경 성공", updatedRegion));
  }

}
