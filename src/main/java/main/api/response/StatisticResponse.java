package main.api.response;

import main.dto.StatisticDTO;

public class StatisticResponse {

    private boolean result;

    private StatisticDTO statistics;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public StatisticDTO getStatistics() {
        return statistics;
    }

    public void setStatistics(StatisticDTO statistics) {
        this.statistics = statistics;
    }
}
