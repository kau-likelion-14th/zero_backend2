package likelion14th.lte.todo.service;

import likelion14th.lte.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class RoutineTodoScheduler {

    private final TodoRepository todoRepository;
    private final RoutineTodoDateGenerator routineTodoDateGenerator;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void generateUpcomingRoutineTodoDates() {
        LocalDate today = LocalDate.now();
        todoRepository.findAllByRoutineEnabledTrue().forEach(todo ->
                routineTodoDateGenerator.generate(todo, todo.getStartDate(), todo.getEndDate(), today)
        );
    }
}
