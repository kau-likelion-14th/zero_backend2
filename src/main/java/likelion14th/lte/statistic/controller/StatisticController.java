package likelion14th.lte.statistic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.statistic.dto.response.StatisticResponse;
import likelion14th.lte.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistic")
@Tag(name = "Statistic", description = "투두 수행 통계 조회 API")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping
    @Operation(summary = "통계 조회")
    public ApiResponse<StatisticResponse> getStatistic(@RequestParam Long userId) {
        return ApiResponse.onSuccess(
                SuccessCode.STATISTICS_GET_SUCCESS,
                statisticService.getStatistic(userId)
        );
    }
}
