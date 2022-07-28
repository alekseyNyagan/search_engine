package main.service;

import main.api.response.ErrorResponse;
import main.configuration.ApplicationProperties;
import main.engine.GrabberTask;
import main.engine.HTMLStorage;
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
import java.util.concurrent.ForkJoinPool;

@Service
public class IndexSystemServiceImpl implements IndexSystemService{

    private final Set<ForkJoinPool> pools;
    private final ApplicationProperties applicationProperties;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final FieldRepository fieldRepository;
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;

    @Autowired
    public IndexSystemServiceImpl(ApplicationProperties sitesConfig, SiteRepository siteRepository, PageRepository pageRepository,
                                  FieldRepository fieldRepository, IndexRepository indexRepository, LemmaRepository lemmaRepository) {
        this.applicationProperties = sitesConfig;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.fieldRepository = fieldRepository;
        this.indexRepository = indexRepository;
        this.lemmaRepository = lemmaRepository;
        pools = new HashSet<>();
    }

    @Transactional
    public ErrorResponse startIndexing() {
        ErrorResponse errorResponse = new ErrorResponse();
        List<Map<String, String>> sitesProperties = applicationProperties.getSites();
        int cores = Runtime.getRuntime().availableProcessors();

        if (isCanIndexing()) {
            pools.clear();
            sitesProperties.forEach(property -> {
                String url = property.get("url");
                String name = property.get("name");
                Optional<Site> optionalSite = siteRepository.findSiteByName(name);
                Site site;
                if (optionalSite.isPresent()) {
                    site = optionalSite.get();
                    List<Page> pages = site.getPage();
                    pages.forEach(page -> {
                        List<Integer> lemmasIds = pageRepository.lemmasIds(page.getId());
                        indexRepository.deleteAllByPage(page);
                        lemmaRepository.deleteLemmasByIds(lemmasIds);
                        pageRepository.delete(page);
                    });
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

    private ForkJoinPool addSite(String url, Site site, int workers) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(workers);
        new Thread(() -> {
            try {
                forkJoinPool.invoke(new GrabberTask(new HTMLStorage(fieldRepository, site, lemmaRepository, indexRepository, applicationProperties),
                        url, url + "/", new HashSet<>(Set.of(url + "/")), site, pageRepository, siteRepository));
                site.setStatus(Status.INDEXED);
                siteRepository.save(site);
                forkJoinPool.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return forkJoinPool;
    }

    private boolean isCanIndexing() {
        return pools.stream().allMatch(ForkJoinPool::isTerminated);
    }
}
