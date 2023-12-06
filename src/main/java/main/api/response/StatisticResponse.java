package main.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import main.dto.StatisticDTO;

@Schema(description = "Сущность ответа сервера со статистикой")
public class StatisticResponse extends AbstractResponse {

    @Schema(description = "Статистика по поисковой системе")
    private StatisticDTO statistics;

    public StatisticDTO getStatistics() {
        return statistics;
    }

    public void setStatistics(StatisticDTO statistics) {
        this.statistics = statistics;
    }
}
