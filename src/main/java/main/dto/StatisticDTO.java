package main.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Статистика сайта и служебная информация")
public record StatisticDTO(@Schema(description = "Общая статистика по поисковой системе") TotalStatisticDTO total,
                           @Schema(description = "Детализированная статистика по каждому сайту") List<SiteStatisticDTO> detailed) {
}
