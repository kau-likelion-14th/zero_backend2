package likelion14th.lte.todo.dto.request;

import jakarta.validation.constraints.NotBlank;
import likelion14th.lte.todo.entity.WeekEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoCreateRequest {

    @NotBlank(message = "카테고리 선택은 필수입니다.")
    private String categoryName;

    @NotBlank(message = "내용 입력은 필수입니다.")
    private String description;

    // 프론트에서 루틴 팝업 창의 [저장]을 누르면 true
    private boolean routineEnabled;

    private LocalDate startDate;

    private LocalDate endDate;

    private WeekEnum week;
}
