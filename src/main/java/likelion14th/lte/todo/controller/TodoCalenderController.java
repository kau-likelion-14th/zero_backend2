@RestController
@RequestMapping("/api/todos")
@Tag(name = "Todo Calendar", description = "월별 투두 캘린더 조회 API")
@RequiredArgsConstructor
public class TodoCalendarController {

    private final TodoCalendarService todoCalendarService;

    /** 월별 캘린더 집계 **/
    @GetMapping("/calendar")
    @Operation(summary = "월별 투두 캘린더 조회", description = "해당 월의 날짜별 남은 투두 개수와 투두 존재 여부를 반환합니다.")
    public ApiResponse<TodoCalendarMonthResponse> getCalendarMonth(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        TodoCalendarMonthResponse response =
                todoCalendarService.getMonthRemainingCounts(userId, year, month);
        return ApiResponse.onSuccess(SuccessCode.TODO_CALENDAR_MONTH_GET_SUCCESS, response);
    }
}