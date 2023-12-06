package main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import main.api.response.AbstractResponse;
import main.api.response.ErrorResponse;
import main.api.response.StatisticResponse;
import main.service.IndexSystemServiceImpl;
import main.service.SearchSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Tag(name = "Поисковая система", description = "Индексация сайтов и поиск")
public class ApiController {

    private final IndexSystemServiceImpl indexSystemService;
    private final SearchSystemService searchSystemService;

    @Autowired
    public ApiController(IndexSystemServiceImpl indexSystemService, SearchSystemService searchSystemService) {
        this.indexSystemService = indexSystemService;
        this.searchSystemService = searchSystemService;
    }

    @GetMapping("/startIndexing")
    @Operation(
            summary = "Запуск индексации",
            description = "Запускает индексацию сайтов, указанных в кофигурационном файле"
    )
    public ResponseEntity<ErrorResponse> startIndexing() {
        return new ResponseEntity<>(indexSystemService.startIndexing(), HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    @Operation(
            summary = "Остановка индексации",
            description = "Останавливает индексацию сайтов, указанных в кофигурационном файле"
    )
    public ResponseEntity<ErrorResponse> stopIndexing() {
        return new ResponseEntity<>(indexSystemService.stopIndexing(), HttpStatus.OK);
    }

    @PostMapping ("/indexPage")
    @Operation(
            summary = "Идексация отдельной страницы",
            description = "Запускает индексацию страницы, переданную в качестве параметра"
    )
    public ResponseEntity<ErrorResponse> indexPage(@RequestParam @Parameter(description = "Путь страницы для индексации") String url) {
        return new  ResponseEntity<>(indexSystemService.indexPage(url), HttpStatus.OK);
    }

    @GetMapping("/statistics")
    @Operation(
            summary = "Получение статистики",
            description = "Получение статистки по индексированным сайтам"
    )
    public ResponseEntity<StatisticResponse> getStatistics() {
        return new ResponseEntity<>(indexSystemService.getStatistics(), HttpStatus.OK);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Поиск",
            description = """
                    Запуск поиска по всем сайтам, если параметр site не был передан,\s
                    или запуск поиска по переданному параметру site
                    """
    )
    public ResponseEntity<AbstractResponse> search(@RequestParam @Parameter(description = "Поисковый запрос") String query,
                                                   @RequestParam(required = false) @Parameter(description = "Сайт") String site,
                                                   @RequestParam @Parameter(description = "Сдвиг для постраничного вывода") int offset,
                                                   @RequestParam @Parameter(description = "Количество результатов, которое нужно вывести") int limit) throws IOException {
        return new  ResponseEntity<>(searchSystemService.search(query, site, offset, limit), HttpStatus.OK);
    }
}
