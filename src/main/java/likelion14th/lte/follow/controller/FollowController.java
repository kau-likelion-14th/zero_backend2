package likelion14th.lte.follow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion14th.lte.follow.dto.FollowUserRequest;
import likelion14th.lte.follow.dto.FollowUserResponse;
import likelion14th.lte.follow.service.FollowService;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/follow")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Tag(name = "Follow API", description = "팔로우 추가, 취소, 목록 조회 API")
public class FollowController {

    private final FollowService followService;

    @PostMapping
    @Operation(summary = "팔로우 추가", description = "userId 사용자가 toUserId 사용자를 팔로우합니다.")
    public ApiResponse<FollowUserResponse> addFollow(
            @RequestParam Long userId,
            @Valid @RequestBody FollowUserRequest followUserRequest
    ) {
        FollowUserResponse response = followService.followUser(userId, followUserRequest.getToUserId());
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_ADD_SUCCESS, response);
    }

    @DeleteMapping
    @Operation(summary = "언팔로우", description = "userId 사용자가 toUserId 사용자와의 팔로우 관계를 취소합니다.")
    public ApiResponse<Void> deleteFollow(
            @RequestParam Long userId,
            @Valid @RequestBody FollowUserRequest followUserRequest
    ) {
        followService.unfollowUser(userId, followUserRequest.getToUserId());
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_DELETE_SUCCESS, null);
    }

    @GetMapping("/followers")
    @Operation(summary = "팔로워 목록 조회", description = "userId 사용자를 팔로우하는 사용자 목록을 조회합니다.")
    public ApiResponse<List<FollowUserResponse>> getFollowerList(
            @RequestParam Long userId
    ) {
        List<FollowUserResponse> response = followService.getFollowers(userId);
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_LIST_GET_SUCCESS, response);
    }

    @GetMapping("/followings")
    @Operation(summary = "팔로잉 목록 조회", description = "userId 사용자가 팔로우하는 사용자 목록을 조회합니다.")
    public ApiResponse<List<FollowUserResponse>> getFollowingList(
            @RequestParam Long userId
    ) {
        List<FollowUserResponse> response = followService.getFollowings(userId);
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_LIST_GET_SUCCESS, response);
    }

    @GetMapping
    @Operation(summary = "팔로우 가능한 사용자 목록 조회", description = "아직 팔로우하지 않은 사용자 목록을 페이징하여 조회합니다.")
    public ApiResponse<Page<FollowUserResponse>> getCanFollowUsers(
            @RequestParam Long userId,
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        Page<FollowUserResponse> response = followService.getCanFollowUsers(userId, pageable);
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_SEARCH_SUCCESS, response);
    }
}
