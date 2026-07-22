package likelion14th.lte.todo.repository;

import likelion14th.lte.todo.entity.TodoDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TodoDateRepository extends JpaRepository<TodoDate, Long> {

    // 투두-날짜 조회
    Optional<TodoDate> findByTodo_IdAndDate(Long todoId, LocalDate date);

    // 유저의 특정 날짜 전체 투두 조회
    List<TodoDate> findAllByTodo_User_IdAndDate(Long userId, LocalDate date);

    // 특정 투두의 날짜 범위 TodoDate 전부 조회
    List<TodoDate> findAllByTodo_IdAndDateBetween(Long todoId, LocalDate start, LocalDate end);

    // 과거는 남기고 미래만 삭제
    void deleteAllByTodo_IdAndDateGreaterThanEqual(Long todoId, LocalDate from);

    // 유저의 날짜 범위 전체 투두 조회 (캘린더용)
    List<TodoDate> findAllByTodo_User_IdAndDateBetween(
            Long userId, LocalDate start, LocalDate end
    );

    boolean existsByTodo_User_IdAndDateAndCompleted(Long userId, LocalDate date, boolean completed);

    long countByTodo_User_IdAndDateBetweenAndCompleted(
            Long userId, LocalDate start, LocalDate end, boolean completed
    );
}
