package main.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Общая статистика по всей поисковой системе")
public record TotalStatisticDTO(@Schema(description = "Количество сайтов") int sites,
                                @Schema(description = "Количество страниц на всех сайтах") long pages,
                                @Schema(description = "Количество лемм на всех сайтах") long lemmas,
                                @Schema(description = "Поле показывающее происходит ли индексация в данный момент") boolean isIndexing) {
}
