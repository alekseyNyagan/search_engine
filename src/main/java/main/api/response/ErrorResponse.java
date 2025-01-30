package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Сущность ответа сервера при ошибке")
public class ErrorResponse extends AbstractResponse {
    @Schema(description = "Описание ошибки")
    private String error;
}
