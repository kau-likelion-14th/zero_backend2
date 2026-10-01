package likelion14th.lte.statistic.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import likelion14th.lte.Entity.BaseEntity;
import likelion14th.lte.todo.entity.WeekEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Getter
@Table(name = "statistic")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Statistic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int streak;
    private int monthPercent;

    @OneToMany(mappedBy = "statistic", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatWeek> statWeeks = new ArrayList<>();

    public static Statistic create() {
        Statistic statistic = new Statistic();
        statistic.initializeWeeks();
        return statistic;
    }

    private void initializeWeeks() {
        for (WeekEnum week : WeekEnum.values()) {
            statWeeks.add(StatWeek.create(this, week));
        }
    }

    public void increaseStreakIfSuccess(boolean success) {
        streak = success ? streak + 1 : 0;
    }

    public void updateMonthPercent(int monthPercent) {
        this.monthPercent = monthPercent;
    }

    public WeekEnum getMostTodoWeek() {
        return statWeeks.stream()
                .max(Comparator.comparingInt(StatWeek::getCount))
                .map(StatWeek::getWeek)
                .orElse(null);
    }
}
