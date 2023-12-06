package main.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность страницы")
public record PageDTO(@Schema(description = "Url сайта") String site,
                      @Schema(description = "Название сайта") String siteName,
                      @Schema(description = "Путь до страницы на которой был найден результат запроса") String uri,
                      @Schema(description = "Заголовок страницы") String title,
                      @Schema(description = """
                              Фрагмент текста,
                              в котором найдены совпадения, выделенные
                              жирным""") String snippet,
                      @Schema(description = "Релевантность страницы") double relevance) {
}
