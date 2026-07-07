package likelion14th.lte.follow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping("/api/follow")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Tag(name = "팔로우 Api", description = "팔로우 추가 및 삭제, 조회를 담당하는 api 입니다.")
public class FollowController {

    private final FollowService followService;


    @PostMapping
    @Operation(summary = "팔로우 추가", description = "유저 id를 통해 팔로우를 추가합니다")
    public ApiResponse<FollowUserResponse> addFollow(
            @RequestParam Long userId,
            @RequestBody FollowUserRequest followUserRequest) {
        // 서비스에 "로그인한 유저 ID"와 "팔로우할 대상 ID"를 넘겨 팔로우를 추가하고, 추가된 대상 유저 정보를 응답 DTO로 받습니다.
        FollowUserResponse response = followService.followUser(userId, followUserRequest.getToUserId());
        // 성공 코드와 응답 데이터를 담은 ApiResponse를 반환합니다. (클라이언트는 이걸 JSON으로 받습니다)
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_ADD_SUCCESS, response);
    }


    @GetMapping("/search")
    @Operation(summary = "팔로우 가능한 유저 검색", description = "닉네임으로 팔로우 가능한 유저를 검색합니다. 쿼리 파라미터로 page, size, sort를 전달할 수 있습니다. " +
            "sort 파라미터는 선택사항이며, 형식: sort=id,DESC 또는 sort=username,ASC (쉼표로 구분). " +
            "정렬 가능한 필드: id, username, userTag, createdAt, updatedAt")
    public ApiResponse<Page<FollowUserResponse>> getSearchFollows(
            @RequestParam Long userId,
            @RequestParam String nickname,
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable
    ){
        // 서비스에서 닉네임으로 팔로우 가능한 유저를 검색하고, 페이징된 결과를 받아 성공 응답으로 반환합니다.
        Page<FollowUserResponse> response = followService.searchCanFollowers(userId, nickname, pageable);
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_SEARCH_SUCCESS, response);
    }
}
