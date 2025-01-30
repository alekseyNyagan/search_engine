package main.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Родительская сущность для всех ответов сервера")
public abstract class AbstractResponse {

    @Schema(description = "Результат ответа")
    private boolean result;

    protected AbstractResponse() {
    }

}
