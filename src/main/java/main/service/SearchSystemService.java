package main.service;

import main.api.response.SearchResponse;

import java.io.IOException;

public interface SearchSystemService {

    public SearchResponse search(String query, String site, int offset, int limit) throws IOException;
}
