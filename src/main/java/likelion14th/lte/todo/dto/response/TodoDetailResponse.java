package likelion14th.lte.todo.dto.response;

import likelion14th.lte.todo.entity.Todo;
import likelion14th.lte.todo.entity.WeekEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoDetailResponse {

    private Long todoId;
    private String description;
    private String categoryName;

    private boolean routineEnabled;
    private LocalDate startDate;
    private LocalDate endDate;
    private WeekEnum week;

    public static TodoDetailResponse from(Todo todo) {
        return new TodoDetailResponse(
                todo.getId(),
                todo.getDescription(),
                todo.getCategory().getCategoryName(),
                todo.isRoutineEnabled(),
                todo.getStartDate(),
                todo.getEndDate(),
                todo.getWeek()
        );
    }
}
