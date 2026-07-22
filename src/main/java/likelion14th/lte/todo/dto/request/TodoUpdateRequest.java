package likelion14th.lte.todo.dto.request;

import jakarta.validation.constraints.NotBlank;
import likelion14th.lte.todo.entity.WeekEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoUpdateRequest {

    @NotBlank(message = "Category is required.")
    private String categoryName;

    @NotBlank(message = "Description is required.")
    private String description;

    private boolean routineEnabled;
    private LocalDate startDate;
    private LocalDate endDate;
    private WeekEnum week;
}
