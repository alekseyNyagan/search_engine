package main.service;

import main.api.response.ErrorResponse;

public interface IndexSystemService {
    public ErrorResponse startIndexing();
    public ErrorResponse stopIndexing();
}
