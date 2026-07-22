package likelion14th.lte.todo.service;

import likelion14th.lte.User.entity.User;
import likelion14th.lte.User.repository.UserRepository;
import likelion14th.lte.category.entity.Category;
import likelion14th.lte.category.repository.CategoryRepository;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.todo.dto.request.TodoCreateRequest;
import likelion14th.lte.todo.dto.request.TodoUpdateRequest;
import likelion14th.lte.todo.dto.response.TodoDetailResponse;
import likelion14th.lte.todo.dto.response.TodoListResponse;
import likelion14th.lte.todo.entity.Todo;
import likelion14th.lte.todo.entity.TodoDate;
import likelion14th.lte.todo.entity.WeekEnum;
import likelion14th.lte.todo.repository.TodoDateRepository;
import likelion14th.lte.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final CategoryRepository categoryRepository;
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final TodoDateRepository todoDateRepository;
    private final RoutineTodoDateGenerator routineTodoDateGenerator;

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
    }

    private Todo getTodoOrThrow(Long todoId) {
        return todoRepository.findById(todoId)
                .orElseThrow(() -> new GeneralException(ErrorCode.TODO_NOT_FOUND));
    }

    private void assertOwner(User user, Todo todo) {
        if (!todo.getUser().getId().equals(user.getId())) {
            throw new GeneralException(ErrorCode.TODO_ACCESS_DENIED);
        }
    }

    private Category getCategoryOrThrow(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new GeneralException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private void validateRoutineRequest(LocalDate startDate, LocalDate endDate, WeekEnum week) {
        if (endDate == null) {
            throw new GeneralException(ErrorCode.TODO_ROUTINE_END_DATE_REQUIRED);
        }
        if (week == null) {
            throw new GeneralException(ErrorCode.TODO_ROUTINE_WEEK_REQUIRED);
        }
        if (startDate != null && startDate.isAfter(endDate)) {
            throw new GeneralException(ErrorCode.TODO_ROUTINE_DATE_RANGE_INVALID);
        }
    }

    @Transactional(readOnly = true)
    public List<TodoListResponse> getTodosByDate(Long userId, LocalDate date) {
        User user = getUserOrThrow(userId);
        return todoDateRepository.findAllByTodo_User_IdAndDate(user.getId(), date).stream()
                .map(todoDate -> TodoListResponse.from(todoDate.getTodo(), todoDate.isCompleted()))
                .toList();
    }

    @Transactional(readOnly = true)
    public TodoDetailResponse getTodoDetail(Long userId, Long todoId) {
        User user = getUserOrThrow(userId);
        Todo todo = getTodoOrThrow(todoId);
        assertOwner(user, todo);
        return TodoDetailResponse.from(todo);
    }

    @Transactional
    public TodoDetailResponse createTodo(Long userId, TodoCreateRequest request, LocalDate date) {
        User user = getUserOrThrow(userId);
        Category category = getCategoryOrThrow(request.getCategoryName());

        boolean routineEnabled = request.isRoutineEnabled();
        if (!routineEnabled && date == null) {
            throw new GeneralException(ErrorCode.TODO_DATE_REQUIRED);
        }

        LocalDate startDate = null;
        LocalDate endDate = null;
        WeekEnum week = null;
        if (routineEnabled) {
            startDate = request.getStartDate();
            endDate = request.getEndDate();
            week = request.getWeek();
            validateRoutineRequest(startDate, endDate, week);
        }

        Todo todo = Todo.create(user, request.getDescription(), category,
                routineEnabled, startDate, endDate, week);
        todoRepository.save(todo);

        if (routineEnabled) {
            routineTodoDateGenerator.generate(todo, startDate, endDate, startDate);
        } else {
            todoDateRepository.save(TodoDate.create(todo, date));
        }
        return TodoDetailResponse.from(todo);
    }

    @Transactional
    public TodoDetailResponse updateTodoDetail(Long userId, Long todoId, TodoUpdateRequest request) {
        User user = getUserOrThrow(userId);
        Todo todo = getTodoOrThrow(todoId);
        Category category = getCategoryOrThrow(request.getCategoryName());

        assertOwner(user, todo);

        boolean routineEnabled = request.isRoutineEnabled();
        if (todo.isRoutineEnabled() != routineEnabled) {
            throw new GeneralException(ErrorCode.TODO_ROUTINE_TYPE_CHANGE_NOT_SUPPORTED);
        }

        LocalDate startDate = null;
        LocalDate endDate = null;
        WeekEnum week = null;
        if (routineEnabled) {
            startDate = request.getStartDate();
            endDate = request.getEndDate();
            week = request.getWeek();
            validateRoutineRequest(startDate, endDate, week);
        }

        todo.update(request.getDescription(), category, routineEnabled, startDate, endDate, week);
        if (routineEnabled) {
            LocalDate today = LocalDate.now();
            todoDateRepository.deleteAllByTodo_IdAndDateGreaterThanEqual(todoId, today);
            routineTodoDateGenerator.generate(todo, startDate, endDate, today);
        }
        return TodoDetailResponse.from(todo);
    }

    @Transactional
    public void deleteTodo(Long userId, Long todoId, LocalDate date) {
        User user = getUserOrThrow(userId);
        Todo todo = getTodoOrThrow(todoId);
        assertOwner(user, todo);

        TodoDate todoDate = todoDateRepository.findByTodo_IdAndDate(todoId, date)
                .orElseThrow(() -> new GeneralException(ErrorCode.TODO_DATE_NOT_FOUND));
        if (todo.isRoutineEnabled()) {
            todoDateRepository.delete(todoDate);
        } else {
            todoRepository.delete(todo);
        }
    }

    @Transactional
    public TodoListResponse todoComplete(Long userId, Long todoId, LocalDate date, boolean completed) {
        User user = getUserOrThrow(userId);
        Todo todo = getTodoOrThrow(todoId);
        assertOwner(user, todo);

        TodoDate todoDate = todoDateRepository.findByTodo_IdAndDate(todoId, date)
                .orElseThrow(() -> new GeneralException(ErrorCode.TODO_DATE_NOT_FOUND));
        if (todoDate.isCompleted() != completed) {
            todoDate.setCompleted(completed);
        }
        return TodoListResponse.from(todo, todoDate.isCompleted());
    }
}
