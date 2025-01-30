package main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import main.model.Status;

@Getter
@Setter
@Schema(description = "Сущность детализированной статисики сайта")
public class SiteStatisticDTO extends AbstractDTO {

    @Schema(description = "Url сайта")
    private String url;

    @Schema(description = "Название сайта")
    private String name;

    @Schema(description = "Статус индексации сайта")
    private Status status;

    @Schema(description = "Время последнего изменения статуса в формате timestamp")
    private long statusTime;

    @Schema(description = "Последняя ошибка во время индексации")
    private String error;

    @Schema(description = "Количество страниц сайта")
    private long pages;

    @Schema(description = "Количество лемм сайта")
    private long lemmas;

}
