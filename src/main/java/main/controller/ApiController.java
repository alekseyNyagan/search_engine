package main.controller;

import main.api.response.ErrorResponse;
import main.api.response.SearchResponse;
import main.api.response.StatisticResponse;
import main.service.IndexSystemServiceImpl;
import main.service.SearchSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class ApiController {

    private final IndexSystemServiceImpl indexSystemService;
    private final SearchSystemService searchSystemService;

    @Autowired
    public ApiController(IndexSystemServiceImpl indexSystemService, SearchSystemService searchSystemService) {
        this.indexSystemService = indexSystemService;
        this.searchSystemService = searchSystemService;
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<ErrorResponse> startIndexing() {
        return new ResponseEntity<>(indexSystemService.startIndexing(), HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<ErrorResponse> stopIndexing() {
        return new ResponseEntity<>(indexSystemService.stopIndexing(), HttpStatus.OK);
    }

    @PostMapping ("/indexPage")
    public ResponseEntity<ErrorResponse> indexPage(@RequestParam String url) {
        return new  ResponseEntity<>(indexSystemService.indexPage(url), HttpStatus.OK);
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticResponse> getStatistics() {
        return new ResponseEntity<>(indexSystemService.getStatistics(), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam String query, @RequestParam(required = false) String site, @RequestParam int offset,
                                                 @RequestParam int limit) throws IOException {
        return new  ResponseEntity<>(searchSystemService.search(query, site, offset, limit), HttpStatus.OK);
    }
}
