package likelion14th.lte.statistic.entity;

import likelion14th.lte.User.entity.User;
import likelion14th.lte.todo.entity.WeekEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticTest {

    @Test
    void create_initializes_all_seven_weeks_with_zero_count() {
        Statistic statistic = Statistic.create();

        assertThat(statistic.getStatWeeks()).hasSize(7);
        assertThat(statistic.getStatWeeks())
                .extracting(StatWeek::getWeek)
                .containsExactly(WeekEnum.values());
        assertThat(statistic.getStatWeeks())
                .allSatisfy(statWeek -> assertThat(statWeek.getCount()).isZero());
    }

    @Test
    void user_creation_initializes_statistic() {
        User user = User.builder()
                .username("tester")
                .userTag("tester#0001")
                .introduction("test")
                .build();

        assertThat(user.getStatistic()).isNotNull();
        assertThat(user.getStatistic().getStatWeeks()).hasSize(7);
    }

    @Test
    void streak_increases_only_on_success_and_resets_on_failure() {
        Statistic statistic = Statistic.create();

        statistic.increaseStreakIfSuccess(true);
        statistic.increaseStreakIfSuccess(true);
        statistic.increaseStreakIfSuccess(false);

        assertThat(statistic.getStreak()).isZero();
    }
}
