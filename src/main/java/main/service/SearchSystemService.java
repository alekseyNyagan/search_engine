package main.service;

import main.api.response.AbstractResponse;

import java.io.IOException;

public interface SearchSystemService {

    public AbstractResponse search(String query, String site, int offset, int limit) throws IOException;
}
