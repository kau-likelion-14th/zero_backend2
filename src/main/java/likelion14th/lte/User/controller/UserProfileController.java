package likelion14th.lte.User.controller;


import io.swagger.v3.oas.annotations.Operation;
import likelion14th.lte.User.dto.request.CreateTestUserRequest;
import likelion14th.lte.User.dto.responce.UserProfileResponse;
import likelion14th.lte.User.service.UserProfileService;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/api/profile")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)

public class UserProfileController {
    public final UserProfileService userProfileService;

    @GetMapping
    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필을 반환합니다.")
    public ApiResponse<UserProfileResponse> getUserProfile(
            @AuthenticationPrincipal Jwt jwt
    ){
        Long userId = Long.valueOf(jwt.getSubject());
        UserProfileResponse userProfileResponse = userProfileService.getUserProfile(userId);

        return ApiResponse.onSuccess(SuccessCode.OK, userProfileResponse);
    }
    @PostMapping
    @Operation(summary = "테스트 유저를 생성", description = "이름, 한줄소개, 유저 태그를 받아 유저를 생성")
    public ApiResponse<UserProfileResponse> createUserProfile(
            @RequestBody CreateTestUserRequest createTestUserRequest
    ){
        UserProfileResponse response = userProfileService.createTestUser(createTestUserRequest);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지 등록 및 수정", description = "로그인한 사용자의 프로필 이미지를 등록하거나 교체합니다.")
    public ApiResponse<UserProfileResponse> putUserProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("image") MultipartFile file
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        UserProfileResponse response = userProfileService.putProfileImage(userId, file);
        return ApiResponse.onSuccess(SuccessCode.PROFILE_PUT_SUCCESS, response);
    }

    @DeleteMapping
    @Operation(summary = "프로필 이미지 삭제", description = "로그인한 사용자의 S3 프로필 이미지와 이미지 정보를 삭제합니다.")
    public ApiResponse<Void> deleteUserProfileImage(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        userProfileService.deleteProfileImage(userId);
        return ApiResponse.onSuccess(SuccessCode.PROFILE_DELETE_SUCCESS, null);
    }
}
