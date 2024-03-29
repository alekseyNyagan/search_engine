package main.service;

import main.api.response.ErrorResponse;
import main.api.response.StatisticResponse;
import main.configuration.ApplicationProperties;
import main.dto.SiteStatisticDTO;
import main.dto.StatisticDTO;
import main.dto.TotalStatisticDTO;
import main.engine.GrabberTask;
import main.engine.HTMLStorage;
import main.mapper.SiteStatisticsMapper;
import main.model.Page;
import main.model.Site;
import main.model.Status;
import main.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Service
public class IndexSystemServiceImpl implements IndexSystemService{

    private final int cores;
    private final Set<ForkJoinPool> pools;
    private final ApplicationProperties applicationProperties;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final FieldRepository fieldRepository;
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final SiteStatisticsMapper siteStatisticsMapper;
    private final ExecutorService threadPools;


    @Autowired
    public IndexSystemServiceImpl(ApplicationProperties sitesConfig, SiteRepository siteRepository, PageRepository pageRepository,
                                  FieldRepository fieldRepository, IndexRepository indexRepository, LemmaRepository lemmaRepository,
                                  SiteStatisticsMapper siteStatisticsMapper) {
        this.applicationProperties = sitesConfig;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.fieldRepository = fieldRepository;
        this.indexRepository = indexRepository;
        this.lemmaRepository = lemmaRepository;
        this.siteStatisticsMapper = siteStatisticsMapper;
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
                    List<Page> pages = site.getPage();
                    pages.forEach(this::deletePage);
                    site = siteRepository.findSiteByName(name).get();
                    site.setStatus(Status.INDEXING);
                } else {
                    site = new Site(Status.INDEXING, LocalDateTime.now(), null, url, name);
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
        String pagePath = url.replaceAll("(https?://[^/:]+)?", "");
        String siteUrl = url.replaceAll(pagePath, "");
        Optional<Site> siteByUrl = siteRepository.findSiteByUrl(siteUrl);
        if (siteByUrl.isPresent()) {
            Site site = siteByUrl.get();
            Optional<Page> pageByPath = pageRepository.findPageByPath(pagePath);

            if (pageByPath.isPresent()) {
                Page page = pageByPath.get();
                deletePage(page);
            }
            try {
                new GrabberTask(new HTMLStorage(fieldRepository, site, lemmaRepository, indexRepository, applicationProperties),
                        site.getUrl(), url, new HashSet<>(), site, pageRepository, siteRepository).parseDoc(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            errorResponse.setResult(true);
        } else {
            errorResponse.setError("Данная страница находится за переделами сайтов, указанных в конфигурационном файле");
        }
        return errorResponse;
    }

    @Override
    public StatisticResponse getStatistics() {
        StatisticResponse statisticResponse = new StatisticResponse();
        List<Site> sites = siteRepository.findAll();
        List<SiteStatisticDTO> statisticDTOS = sites.stream().map(siteStatisticsMapper::toDTO).collect(Collectors.toList());
        TotalStatisticDTO totalStatisticDTO = new TotalStatisticDTO(sites.size()
                , statisticDTOS.stream().mapToInt(SiteStatisticDTO::getPages).sum()
                , statisticDTOS.stream().mapToInt(SiteStatisticDTO::getLemmas).sum()
                , isIndexing());
        StatisticDTO statisticDTO = new StatisticDTO(totalStatisticDTO, statisticDTOS);

        statisticResponse.setResult(true);
        statisticResponse.setStatistics(statisticDTO);
        return statisticResponse;
    }

    private ForkJoinPool addSite(String url, Site site, int workers) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(workers);
        threadPools.execute(() -> {
            try {
                forkJoinPool.invoke(new GrabberTask(new HTMLStorage(fieldRepository, site, lemmaRepository, indexRepository, applicationProperties),
                        url, url + "/", new HashSet<>(Set.of(url + "/")), site, pageRepository, siteRepository));
                site.setStatus(Status.INDEXED);
                siteRepository.save(site);
                forkJoinPool.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return forkJoinPool;
    }

    private boolean isIndexing() {
        if (pools.isEmpty()) {
            return false;
        }
        return pools.stream().noneMatch(ForkJoinPool::isTerminated);
    }

    private void deletePage(Page page) {
        List<Integer> lemmasIds = pageRepository.lemmasIds(page);
        indexRepository.deleteAllByPage(page);
        lemmaRepository.deleteLemmasByIds(lemmasIds);
        pageRepository.delete(page);
    }
}
