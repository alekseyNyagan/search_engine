package main.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.configuration.ApplicationProperties;
import main.engine.GrabberTask;
import main.engine.WebPageClient;
import main.model.Site;
import main.model.Status;
import main.repository.HtmlPageRepository;
import main.repository.SiteRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerOrchestrator {

    private final HtmlPageRepository htmlPageRepository;

    private final ApplicationProperties applicationProperties;
    private final SiteRepository siteRepository;
    private final WebPageClient webPageClient;
    private final LemmaService lemmaService;
    private final ObjectProvider<GrabberTask> tasksProvider;

    private final Map<Site, ForkJoinPool> active = new ConcurrentHashMap<>();

    @Transactional
    public void startIndexing() {
        if (!active.isEmpty()) {
            throw new IllegalStateException("Already indexing");
        }

        applicationProperties.getSites().forEach(cfg -> {
            Site site = siteRepository.findSiteByName(cfg.get("name"))
                    .orElseGet(() -> new Site(Status.INDEXING,
                            LocalDateTime.now(),
                            null,
                            cfg.get("url"),
                            cfg.get("name"),
                            0L,
                            0L));

            site.setStatus(Status.INDEXING);
            siteRepository.save(site);

            ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() / applicationProperties.getSites().size());
            active.put(site, pool);

            GrabberTask rootTask = tasksProvider.getObject(
                    cfg.get("url") + "/",
                    new ConcurrentSkipListSet<>(),
                    site,
                    webPageClient);

            pool.submit(() -> {
                rootTask.invoke();
                lemmaService.recalculate(site);
                site.setStatus(Status.INDEXED);
                site.setHtmlPagesCount(htmlPageRepository.countBySiteName(site.getName()));
                siteRepository.save(site);
            });
            pool.shutdown();
            active.clear();
        });
    }

    public void stopIndexing() {
        active.values().forEach(ForkJoinPool::shutdownNow);
        active.clear();
    }

    public boolean isIndexing() {
        return active.values().stream().anyMatch(p -> !p.isTerminated());
    }
}
