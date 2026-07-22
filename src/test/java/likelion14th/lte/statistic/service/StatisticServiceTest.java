package likelion14th.lte.statistic.service;

import jakarta.persistence.EntityManager;
import likelion14th.lte.User.entity.User;
import likelion14th.lte.User.repository.UserRepository;
import likelion14th.lte.statistic.entity.StatWeek;
import likelion14th.lte.todo.repository.TodoDateRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatisticServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final TodoDateRepository todoDateRepository = mock(TodoDateRepository.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final StatisticService statisticService = new StatisticService(
            userRepository, todoDateRepository, entityManager
    );

    @Test
    void updateStatistic_increases_streak_and_matching_week_count_on_success() {
        User user = user();
        LocalDate yesterday = LocalDate.now().minusDays(1);

        when(todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(user.getId(), yesterday, true))
                .thenReturn(true);
        when(todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(user.getId(), yesterday, false))
                .thenReturn(false);
        when(todoDateRepository.countByTodo_User_IdAndDateBetweenAndCompleted(
                eq(user.getId()), any(LocalDate.class), eq(yesterday), eq(true)
        )).thenReturn(3L);
        when(todoDateRepository.countByTodo_User_IdAndDateBetweenAndCompleted(
                eq(user.getId()), any(LocalDate.class), eq(yesterday), eq(false)
        )).thenReturn(1L);

        statisticService.updateStatistic(user);

        assertThat(user.getStatistic().getStreak()).isEqualTo(1);
        assertThat(user.getStatistic().getMonthPercent()).isEqualTo(75);
        StatWeek yesterdayWeek = user.getStatistic().getStatWeeks().stream()
                .filter(statWeek -> statWeek.getWeek().toDayOfWeek() == yesterday.getDayOfWeek())
                .findFirst()
                .orElseThrow();
        assertThat(yesterdayWeek.getCount()).isEqualTo(1);
    }

    @Test
    void updateStatistic_resets_streak_and_returns_zero_percent_when_no_todos_exist() {
        User user = user();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        user.getStatistic().increaseStreakIfSuccess(true);

        when(todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(user.getId(), yesterday, true))
                .thenReturn(false);
        when(todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(user.getId(), yesterday, false))
                .thenReturn(false);
        when(todoDateRepository.countByTodo_User_IdAndDateBetweenAndCompleted(
                eq(user.getId()), any(LocalDate.class), eq(yesterday), any(Boolean.class)
        )).thenReturn(0L);

        statisticService.updateStatistic(user);

        assertThat(user.getStatistic().getStreak()).isZero();
        assertThat(user.getStatistic().getMonthPercent()).isZero();
        assertThat(user.getStatistic().getStatWeeks())
                .allSatisfy(statWeek -> assertThat(statWeek.getCount()).isZero());
    }

    @Test
    void updateAllStatistics_processes_a_page_of_500_and_clears_persistence_context() {
        User user = user();
        LocalDate yesterday = LocalDate.now().minusDays(1);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));
        when(todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(user.getId(), yesterday, true))
                .thenReturn(false);
        when(todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(user.getId(), yesterday, false))
                .thenReturn(false);
        when(todoDateRepository.countByTodo_User_IdAndDateBetweenAndCompleted(
                eq(user.getId()), any(LocalDate.class), eq(yesterday), any(Boolean.class)
        )).thenReturn(0L);

        statisticService.updateAllStatistics();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(500);
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    private User user() {
        User user = User.builder()
                .username("tester")
                .userTag("tester#0001")
                .introduction("test")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
