package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Сущность ответа сервера при ошибке")
public class ErrorResponse extends AbstractResponse {
    @Schema(description = "Описание ошибки")
    private String error;

    public ErrorResponse() {
    }

    public ErrorResponse(boolean result, String error) {
        super(result);
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
