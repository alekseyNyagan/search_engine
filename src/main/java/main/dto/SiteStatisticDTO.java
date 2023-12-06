package main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import main.model.Status;

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
    private int pages;

    @Schema(description = "Количество лемм сайта")
    private int lemmas;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(long statusTime) {
        this.statusTime = statusTime;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getLemmas() {
        return lemmas;
    }

    public void setLemmas(int lemmas) {
        this.lemmas = lemmas;
    }
}
