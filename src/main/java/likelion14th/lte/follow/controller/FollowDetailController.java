package likelion14th.lte.follow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.todo.dto.response.TodoCalendarMonthResponse;
import likelion14th.lte.todo.dto.response.TodoListResponse;
import likelion14th.lte.todo.service.TodoCalendarService;
import likelion14th.lte.todo.service.TodoService;
import likelion14th.lte.user.dto.response.UserProfileResponse;
import likelion14th.lte.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/follow/{followId}")
@Tag(name = "친구 페이지 관리", description = "친구 페이지 조회 api")
@RequiredArgsConstructor
public class FollowDetailController {
    private final UserProfileService userProfileService;
    private final TodoCalendarService todoCalendarService;
    private final TodoService todoService;

    @GetMapping
    @Operation(summary = "친구 프로필을 조회합니다", description = "친구 프로필 조회 / 친구 페이지 상단")
    public ApiResponse<UserProfileResponse> getUserProfile(@PathVariable Long followId) {
        UserProfileResponse response = userProfileService.getUserProfile(followId);
        return ApiResponse.onSuccess(SuccessCode.USER_INFO_GET_SUCCESS, response);
    }

    @GetMapping("/calendar")
    @Operation(summary = "친구의 캘린더를 조회합니다.", description = "친구 캘린더 조회")
    public ApiResponse<TodoCalendarMonthResponse> getCalendarMonth(
            @PathVariable Long followId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        TodoCalendarMonthResponse response =
                todoCalendarService.getMonthRemainingCounts(followId, year, month);
        return ApiResponse.onSuccess(SuccessCode.TODO_CALENDAR_MONTH_GET_SUCCESS, response);
    }

    @GetMapping("/todos")
    @Operation(summary = "친구 투두 리스트 조회", description = "선택한 날짜의 친구 투두리스트를 조회합니다.")
    public ApiResponse<List<TodoListResponse>> getTodosByDate(
            @PathVariable Long followId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        List<TodoListResponse> todos = todoService.getTodosByDate(followId, date);
        return ApiResponse.onSuccess(SuccessCode.TODO_LIST_GET_SUCCESS, todos);
    }
}
