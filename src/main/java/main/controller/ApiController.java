package main.controller;

import main.api.response.ErrorResponse;
import main.service.IndexSystemServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final IndexSystemServiceImpl indexSystemService;

    @Autowired
    public ApiController(IndexSystemServiceImpl indexSystemService) {
        this.indexSystemService = indexSystemService;
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<ErrorResponse> startIndexing() {
        return new ResponseEntity<>(indexSystemService.startIndexing(), HttpStatus.OK);
    }
}
