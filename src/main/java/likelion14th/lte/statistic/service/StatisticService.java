package likelion14th.lte.statistic.service;

import jakarta.persistence.EntityManager;
import likelion14th.lte.User.entity.User;
import likelion14th.lte.User.repository.UserRepository;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.statistic.dto.response.StatisticResponse;
import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.statistic.entity.StatWeek;
import likelion14th.lte.todo.repository.TodoDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private static final int BATCH_SIZE = 500;

    private final UserRepository userRepository;
    private final TodoDateRepository todoDateRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public StatisticResponse getStatistic(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
        return StatisticResponse.from(user.getStatistic());
    }

    public void updateStatistic(User user) {
        user.initializeStatisticIfAbsent();
        Statistic statistic = user.getStatistic();
        LocalDate day = LocalDate.now().minusDays(1);

        boolean hasCompletedTodo = todoDateRepository
                .existsByTodo_User_IdAndDateAndCompleted(user.getId(), day, true);
        boolean hasIncompleteTodo = todoDateRepository
                .existsByTodo_User_IdAndDateAndCompleted(user.getId(), day, false);
        boolean success = hasCompletedTodo && !hasIncompleteTodo;

        statistic.increaseStreakIfSuccess(success);
        if (success) {
            statistic.getStatWeeks().stream()
                    .filter(statWeek -> statWeek.getWeek().toDayOfWeek() == day.getDayOfWeek())
                    .findFirst()
                    .ifPresent(StatWeek::increaseCount);
        }

        LocalDate startDate = day.minusDays(30);
        long completedCount = todoDateRepository
                .countByTodo_User_IdAndDateBetweenAndCompleted(user.getId(), startDate, day, true);
        long incompleteCount = todoDateRepository
                .countByTodo_User_IdAndDateBetweenAndCompleted(user.getId(), startDate, day, false);
        long totalCount = completedCount + incompleteCount;

        int monthPercent = totalCount == 0 ? 0 : (int) ((completedCount * 100) / totalCount);
        statistic.updateMonthPercent(monthPercent);
    }

    @Transactional
    public void updateAllStatistics() {
        int page = 0;
        Page<User> userPage;

        do {
            userPage = userRepository.findAll(PageRequest.of(page, BATCH_SIZE));
            userPage.getContent().forEach(this::updateStatistic);

            entityManager.flush();
            entityManager.clear();
            page++;
        } while (userPage.hasNext());
    }
}
