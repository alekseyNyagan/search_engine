package main.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Родительская сущность для всех ответов сервера")
public abstract class AbstractResponse {

    @Schema(description = "Результат ответа")
    private boolean result;

    public AbstractResponse() {
    }

    public AbstractResponse(boolean result) {
        this.result = result;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
