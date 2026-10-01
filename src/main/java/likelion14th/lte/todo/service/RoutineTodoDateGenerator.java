package likelion14th.lte.todo.service;

import likelion14th.lte.todo.entity.Todo;
import likelion14th.lte.todo.entity.TodoDate;
import likelion14th.lte.todo.repository.TodoDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoutineTodoDateGenerator {

    private final TodoDateRepository todoDateRepository;

    // Transaction boundaries are managed by the calling service or scheduler.
    public void generate(Todo todo, LocalDate startDate, LocalDate endDate, LocalDate fromDate) {
        LocalDate start = startDate.isAfter(fromDate) ? startDate : fromDate;
        LocalDate endLimit = start.plusYears(1).minusDays(1);
        LocalDate end = endDate.isBefore(endLimit) ? endDate : endLimit;

        DayOfWeek repeatDay = todo.getWeek().toDayOfWeek();
        Set<LocalDate> existingDates = todoDateRepository
                .findAllByTodo_IdAndDateBetween(todo.getId(), start, end)
                .stream()
                .map(TodoDate::getDate)
                .collect(Collectors.toSet());

        List<TodoDate> todoDates = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (!date.getDayOfWeek().equals(repeatDay) || existingDates.contains(date)) {
                continue;
            }
            todoDates.add(TodoDate.create(todo, date));
        }
        todoDateRepository.saveAll(todoDates);
    }
}
