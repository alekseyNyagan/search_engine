package main.service;

import main.api.response.AbstractResponse;

public interface SearchSystemService {

    AbstractResponse search(String query, String site, int offset, int limit);
}
