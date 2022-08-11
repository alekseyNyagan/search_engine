package main.service;

import main.api.response.ErrorResponse;
import main.api.response.StatisticResponse;

public interface IndexSystemService {
    public ErrorResponse startIndexing();
    public ErrorResponse stopIndexing();
    public ErrorResponse indexPage(String url);
    public StatisticResponse getStatistics();
}
