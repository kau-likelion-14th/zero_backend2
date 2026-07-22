package likelion14th.lte.todo.dto.response;

import likelion14th.lte.todo.entity.Todo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoListResponse {

    /** 필요한 것 : id, 설명, 카테고리, 완료 여부 **/
    private Long todoId;
    private String description;
    private String categoryName;
    private boolean isCompleted;

    public static TodoListResponse from(Todo todo, boolean completed) {
        return new TodoListResponse(
                todo.getId(),
                todo.getDescription(),
                todo.getCategory().getCategoryName(),
                completed
        );
    }
}