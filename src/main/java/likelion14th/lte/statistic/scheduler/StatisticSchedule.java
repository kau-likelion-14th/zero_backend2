package likelion14th.lte.statistic.scheduler;

import likelion14th.lte.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatisticSchedule {

    private final StatisticService statisticService;

    @Scheduled(cron = "0 10 0 * * *")
    public void updateAllStatistics() {
        statisticService.updateAllStatistics();
    }
}
