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
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        List<TodoListResponse> todos = todoService.getTodosByDate(userId, date);
        return ApiResponse.onSuccess(SuccessCode.TODO_LIST_GET_SUCCESS, todos);
    }

    /** 투두 추가 **/
    @PostMapping
    @Operation(summary = "투두 생성", description = "일반/루틴 투두를 생성합니다.")
    public ApiResponse<TodoDetailResponse> createTodo(
            @RequestParam Long userId,
            @Valid @RequestBody TodoCreateRequest todoCreateRequest,
            // 일반 투두는 필수, 루틴 투두는 startDate, endDate 사용
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        TodoDetailResponse createdResponse = todoService.createTodo(userId, todoCreateRequest, date);
        return ApiResponse.onSuccess(SuccessCode.TODO_CREATE_SUCCESS, createdResponse);
    }

    /** 투두 삭제 **/
    @DeleteMapping("/{todoId}/dates/{date}")
    @Operation(summary = "투두 삭제", description = "선택한 날짜 기준으로 투두를 삭제합니다.")
    public ApiResponse<String> deleteTodo(
            @RequestParam Long userId,
            @PathVariable Long todoId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        todoService.deleteTodo(userId, todoId, date);
        return ApiResponse.onSuccess(SuccessCode.TODO_DELETE_SUCCESS, "OK");
    }

    /** 투두 완료 처리 **/
    @PatchMapping("/{todoId}/dates/{date}/complete")
    @Operation(summary = "투두 완료 상태 변경", description = "선택한 날짜의 투두 완료 상태를 변경합니다.")
    public ApiResponse<TodoListResponse> updateTodoComplete(
            @RequestParam Long userId,
            @PathVariable Long todoId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Valid @RequestBody TodoCompleteUpdateRequest request
    ) {
        TodoListResponse updatedResponse = todoService.todoComplete(userId, todoId, date, request.getCompleted());
        return ApiResponse.onSuccess(SuccessCode.TODO_COMPLETE_SUCCESS, updatedResponse);
    }

    /** 투두 상세 조회 **/
    @GetMapping("/{todoId}")
    @Operation(summary = "투두 상세 조회", description = "투두의 상세 정보를 조회합니다.")
    public ApiResponse<TodoDetailResponse> getTodoDetail(
            @RequestParam Long userId,
            @PathVariable Long todoId
    ) {
        TodoDetailResponse todo = todoService.getTodoDetail(userId, todoId);
        return ApiResponse.onSuccess(SuccessCode.TODO_DETAIL_GET_SUCCESS, todo);
    }

    /** 투두 상세 수정 **/
    @PutMapping("/{todoId}")
    @Operation(summary = "투두 수정", description = "투두의 상세 정보를 수정합니다.")
    public ApiResponse<TodoDetailResponse> updateTodoDetail(
            @RequestParam Long userId,
            @PathVariable Long todoId,
            @Valid @RequestBody TodoUpdateRequest todoUpdateRequest
    ) {
        TodoDetailResponse updatedResponse = todoService.updateTodoDetail(
                userId,
                todoId,
                todoUpdateRequest
        );
        return ApiResponse.onSuccess(SuccessCode.TODO_DETAIL_UPDATE_SUCCESS, updatedResponse);
    }
}

