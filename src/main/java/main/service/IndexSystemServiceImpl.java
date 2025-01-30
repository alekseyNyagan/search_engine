package main.service;

import lombok.extern.slf4j.Slf4j;
import main.api.response.ErrorResponse;
import main.api.response.StatisticResponse;
import main.configuration.ApplicationProperties;
import main.dto.SiteStatisticDTO;
import main.dto.StatisticDTO;
import main.dto.TotalStatisticDTO;
import main.engine.GrabberTask;
import main.engine.HTMLStorage;
import main.mapper.SiteStatisticsMapper;
import main.model.HtmlPage;
import main.model.Site;
import main.model.Status;
import main.opennlp.OpenNLPLemmatizer;
import main.repository.HtmlPageRepository;
import main.repository.SiteRepository;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Service
@Slf4j
public class IndexSystemServiceImpl implements IndexSystemService {

    private final int cores;
    private final Set<ForkJoinPool> pools;
    private final ApplicationProperties applicationProperties;
    private final SiteRepository siteRepository;
    private final ExecutorService threadPools;
    private final HtmlPageRepository htmlPageRepository;
    private final SiteStatisticsMapper siteStatisticsMapper;
    private final OpenNLPLemmatizer openNLPLemmatizer;


    @Autowired
    public IndexSystemServiceImpl(ApplicationProperties sitesConfig, SiteRepository siteRepository, HtmlPageRepository htmlPageRepository,
                                  SiteStatisticsMapper siteStatisticsMapper, OpenNLPLemmatizer openNLPLemmatizer) {
        this.applicationProperties = sitesConfig;
        this.siteRepository = siteRepository;
        this.htmlPageRepository = htmlPageRepository;
        this.siteStatisticsMapper = siteStatisticsMapper;
        this.openNLPLemmatizer = openNLPLemmatizer;
        cores = Runtime.getRuntime().availableProcessors();
        pools = new HashSet<>();
        threadPools = Executors.newFixedThreadPool(cores + 1);
    }

    @Transactional
    public ErrorResponse startIndexing() {
        ErrorResponse errorResponse = new ErrorResponse();
        List<Map<String, String>> sitesProperties = applicationProperties.getSites();

        if (!isIndexing()) {
            pools.clear();
            sitesProperties.forEach(property -> {
                String url = property.get("url");
                String name = property.get("name");
                Optional<Site> optionalSite = siteRepository.findSiteByName(name);
                Site site;
                if (optionalSite.isPresent()) {
                    site = optionalSite.get();
                    htmlPageRepository.deleteAllBySiteName(site.getName());
                    site.setStatus(Status.INDEXING);
                } else {
                    site = new Site(Status.INDEXING, LocalDateTime.now(), null, url, name, 0L, 0L);
                }
                siteRepository.save(site);
                pools.add(addSite(url, site, cores / sitesProperties.size()));
            });
            errorResponse.setResult(true);
        } else {
            errorResponse.setError("Страница уже индексируется");
        }
        return errorResponse;
    }

    @Override
    public ErrorResponse stopIndexing() {
        ErrorResponse errorResponse = new ErrorResponse();
        if (isIndexing()) {
            pools.forEach(ForkJoinPool::shutdownNow);
            errorResponse.setResult(true);
        } else {
            errorResponse.setError("Индексация не запущена");
        }
        return errorResponse;
    }

    @Override
    public ErrorResponse indexPage(String url) {
        ErrorResponse errorResponse = new ErrorResponse();
        String htmlPagePath = url.replaceAll("(https?://[^/:]+)?", "");
        String siteUrl = url.replaceAll(htmlPagePath, "");
        Optional<Site> siteByUrl = siteRepository.findSiteByUrl(siteUrl);
        siteByUrl.ifPresentOrElse(site -> {
                    Optional<HtmlPage> htmlPageByPath = htmlPageRepository.findPageByPath(htmlPagePath);
                    htmlPageByPath.ifPresent(htmlPage -> {
                        htmlPageRepository.deleteByPath(htmlPage.getPath());
                        HtmlPage newHtmlPage = new HtmlPage();
                        try {
                            newHtmlPage = new GrabberTask(new HTMLStorage(applicationProperties),
                                    site.getName(), url, new HashSet<>(), site, htmlPageRepository, siteRepository).parseDoc(url);
                        } catch (IOException e) {
                            log.error("Ошибка ввода-вывода: {}", e.getMessage());
                        }
                        site.setLemmasCount(site.getLemmasCount() - countLemmas(htmlPage.getContent()) + countLemmas(newHtmlPage.getContent()));
                        siteRepository.save(site);
                    });
                    errorResponse.setResult(true);
                },
                () -> errorResponse.setError("Данная страница находится за переделами сайтов, указанных в конфигурационном файле"));
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
                , isIndexing());
        StatisticDTO statisticDTO = new StatisticDTO(totalStatisticDTO, statisticDTOS);

        statisticResponse.setResult(true);
        statisticResponse.setStatistics(statisticDTO);
        return statisticResponse;
    }

    private ForkJoinPool addSite(String url, Site site, int workers) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(workers);
        String siteName = site.getName();
        threadPools.execute(() -> {
            forkJoinPool.invoke(new GrabberTask(
                    new HTMLStorage(applicationProperties),
                    site.getName(),
                    url + "/",
                    new HashSet<>(Set.of(url + "/")),
                    site,
                    htmlPageRepository,
                    siteRepository));
            forkJoinPool.shutdown();
            threadPools.submit(() -> {
                site.setLemmasCount(countLemmasBySiteName(siteName));
                siteRepository.save(site);
            });
            site.setStatus(Status.INDEXED);
            site.setHtmlPagesCount(htmlPageRepository.countBySiteName(siteName));
            siteRepository.save(site);
        });
        return forkJoinPool;
    }

    private boolean isIndexing() {
        if (pools.isEmpty()) {
            return false;
        }
        return pools.stream().noneMatch(ForkJoinPool::isTerminated);
    }

    private int countLemmas(String content) {
        return new HashSet<>(Arrays.asList(openNLPLemmatizer.getLemmas(content))).size();
    }

    private long countLemmasBySiteName(String siteName) {
        SearchHits<HtmlPage> htmlPageSearchHits = htmlPageRepository.findBySiteName(siteName);
        Set<String> lemmas = new HashSet<>();

        for (SearchHit<HtmlPage> searchHit : htmlPageSearchHits) {
            lemmas.addAll(Arrays.asList(openNLPLemmatizer.getLemmas(Jsoup.clean(searchHit.getContent().getContent(), Safelist.none()))));
        }
        return lemmas.size();
    }
}
