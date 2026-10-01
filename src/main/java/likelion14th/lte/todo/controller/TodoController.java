package likelion14th.lte.todo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.todo.dto.request.TodoCompleteUpdateRequest;
import likelion14th.lte.todo.dto.request.TodoCreateRequest;
import likelion14th.lte.todo.dto.request.TodoUpdateRequest;
import likelion14th.lte.todo.dto.response.TodoDetailResponse;
import likelion14th.lte.todo.dto.response.TodoListResponse;
import likelion14th.lte.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/todos")
@Tag(name = "Todo", description = "투두 생성, 조회, 수정, 삭제 및 완료 처리 API")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    /** 투두 리스트 조회 **/
    @GetMapping
    @Operation(summary = "날짜별 투두 목록 조회", description = "선택한 날짜의 투두 목록을 조회합니다.")
    public ApiResponse<List<TodoListResponse>> getTodosByDate(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        List<TodoListResponse> todos = todoService.getTodosByDate(userId, date);
        return ApiResponse.onSuccess(SuccessCode.TODO_LIST_GET_SUCCESS, todos);
    }

    /** 투두 추가 **/
    @PostMapping
    @Operation(summary = "투두 생성", description = "일반/루틴 투두를 생성합니다.")
    public ApiResponse<TodoDetailResponse> createTodo(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TodoCreateRequest todoCreateRequest,
            // 일반 투두는 필수, 루틴 투두는 startDate, endDate 사용
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        TodoDetailResponse createdResponse = todoService.createTodo(userId, todoCreateRequest, date);
        return ApiResponse.onSuccess(SuccessCode.TODO_CREATE_SUCCESS, createdResponse);
    }

    /** 투두 삭제 **/
    @DeleteMapping("/{todoId}/dates/{date}")
    @Operation(summary = "투두 삭제", description = "선택한 날짜 기준으로 투두를 삭제합니다.")
    public ApiResponse<String> deleteTodo(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long todoId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        todoService.deleteTodo(userId, todoId, date);
        return ApiResponse.onSuccess(SuccessCode.TODO_DELETE_SUCCESS, "OK");
    }

    /** 투두 완료 처리 **/
    @PatchMapping("/{todoId}/dates/{date}/complete")
    @Operation(summary = "투두 완료 상태 변경", description = "선택한 날짜의 투두 완료 상태를 변경합니다.")
    public ApiResponse<TodoListResponse> updateTodoComplete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long todoId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Valid @RequestBody TodoCompleteUpdateRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        TodoListResponse updatedResponse = todoService.todoComplete(userId, todoId, date, request.getCompleted());
        return ApiResponse.onSuccess(SuccessCode.TODO_COMPLETE_SUCCESS, updatedResponse);
    }

    /** 투두 상세 조회 **/
    @GetMapping("/{todoId}")
    @Operation(summary = "투두 상세 조회", description = "투두의 상세 정보를 조회합니다.")
    public ApiResponse<TodoDetailResponse> getTodoDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long todoId
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        TodoDetailResponse todo = todoService.getTodoDetail(userId, todoId);
        return ApiResponse.onSuccess(SuccessCode.TODO_DETAIL_GET_SUCCESS, todo);
    }

    /** 투두 상세 수정 **/
    @PutMapping("/{todoId}")
    @Operation(summary = "투두 수정", description = "투두의 상세 정보를 수정합니다.")
    public ApiResponse<TodoDetailResponse> updateTodoDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long todoId,
            @Valid @RequestBody TodoUpdateRequest todoUpdateRequest
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        TodoDetailResponse updatedResponse = todoService.updateTodoDetail(
                userId,
                todoId,
                todoUpdateRequest
        );
        return ApiResponse.onSuccess(SuccessCode.TODO_DETAIL_UPDATE_SUCCESS, updatedResponse);
    }
}
