package main.service;

import main.api.response.ErrorResponse;
import main.api.response.StatisticResponse;

public interface IndexSystemService {
    ErrorResponse startIndexing();
    ErrorResponse stopIndexing();
    ErrorResponse indexPage(String url);
    StatisticResponse getStatistics();
}
