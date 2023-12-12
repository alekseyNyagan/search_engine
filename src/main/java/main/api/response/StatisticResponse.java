package main.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import main.dto.StatisticDTO;

@Getter
@Setter
@Schema(description = "Сущность ответа сервера со статистикой")
public class StatisticResponse extends AbstractResponse {

    @Schema(description = "Статистика по поисковой системе")
    private StatisticDTO statistics;

}
