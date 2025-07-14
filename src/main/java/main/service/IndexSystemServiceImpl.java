package main.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.api.response.ErrorResponse;
import main.api.response.StatisticResponse;
import main.dto.SiteStatisticDTO;
import main.dto.StatisticDTO;
import main.dto.TotalStatisticDTO;
import main.engine.WebPageClient;
import main.mapper.SiteStatisticsMapper;
import main.model.Site;
import main.repository.HtmlPageRepository;
import main.repository.SiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class IndexSystemServiceImpl implements IndexSystemService {

    private final CrawlerOrchestrator crawlerOrchestrator;
    private final WebPageClient webPageClient;
    private final LemmaService lemmaService;
    private final SiteRepository siteRepository;
    private final HtmlPageRepository htmlPageRepository;
    private final SiteStatisticsMapper siteStatisticsMapper;

    @Override
    @Transactional
    public ErrorResponse startIndexing() {
        ErrorResponse errorResponse = new ErrorResponse();

        if (crawlerOrchestrator.isIndexing()) {
            errorResponse.setError("Индексация уже запущена");
            return errorResponse;
        }

        crawlerOrchestrator.startIndexing();
        errorResponse.setResult(true);
        return errorResponse;
    }

    @Override
    public ErrorResponse stopIndexing() {
        ErrorResponse errorResponse = new ErrorResponse();
        if (crawlerOrchestrator.isIndexing()) {
            crawlerOrchestrator.stopIndexing();
            errorResponse.setResult(true);
        } else {
            errorResponse.setError("Индексация не запущена");
        }
        return errorResponse;
    }

    @Override
    @Transactional
    public ErrorResponse indexPage(String url) {
        ErrorResponse errorResponse = new ErrorResponse();

        String htmlPagePath = url.replaceAll("(https?://[^/]+)", "");
        String siteUrl = url.substring(0, url.length() - htmlPagePath.length());

        Optional<Site> siteByUrl = siteRepository.findSiteByUrl(siteUrl);
        if (siteByUrl.isEmpty()) {
            errorResponse.setError("URL не входит в список разрешённых сайтов");
            return errorResponse;
        }
        Site site = siteByUrl.get();

        htmlPageRepository.deleteByPath(htmlPagePath);
        try {
            webPageClient.savePage(url, site);
        } catch (IOException e) {
            log.error("I/O error while indexing {}", url, e);
            errorResponse.setError("Ошибка загрузки страницы");
            return errorResponse;
        }

        long lemmaCount = lemmaService.recalculate(site);
        site.setLemmasCount(lemmaCount);
        siteRepository.save(site);

        errorResponse.setResult(true);
        return errorResponse;
    }

    @Override
    public StatisticResponse getStatistics() {
        StatisticResponse statisticResponse = new StatisticResponse();
        List<Site> sites = siteRepository.findAll();
        List<SiteStatisticDTO> statisticDTOS = sites.stream().map(siteStatisticsMapper::toDTO).toList();
        TotalStatisticDTO totalStatisticDTO = new TotalStatisticDTO(sites.size()
                , statisticDTOS.stream().mapToLong(SiteStatisticDTO::getPages).sum()
                , statisticDTOS.stream().mapToLong(SiteStatisticDTO::getLemmas).sum()
                , crawlerOrchestrator.isIndexing());

        statisticResponse.setResult(true);
        statisticResponse.setStatistics(new StatisticDTO(totalStatisticDTO, statisticDTOS));
        return statisticResponse;
    }
}
