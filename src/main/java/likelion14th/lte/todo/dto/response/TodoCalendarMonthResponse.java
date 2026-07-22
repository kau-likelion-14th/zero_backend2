package likelion14th.lte.todo.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoCalendarMonthResponse {
    private List<DayInfo> days;

    public static TodoCalendarMonthResponse of(List<DayInfo> days) {
        return new TodoCalendarMonthResponse(days);
    }

    @Getter
    @AllArgsConstructor
    public static class DayInfo {
        private LocalDate date;
        private long remaining;
        private boolean hasTodo;
    }
}
