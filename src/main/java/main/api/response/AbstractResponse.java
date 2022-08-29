package main.api.response;

public abstract class AbstractResponse {

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
