package main.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.configuration.ApplicationProperties;
import main.model.HtmlPage;
import main.model.Site;
import main.repository.HtmlPageRepository;
import main.repository.SiteRepository;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebPageClient {

    private final ApplicationProperties applicationProperties;
    private final SiteRepository siteRepository;
    private final HtmlPageRepository htmlPageRepository;

    public Document getDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(applicationProperties.getUserAgent())
                .ignoreContentType(true)
                .get();
    }

    public HtmlPage savePage(String url, Site site) throws IOException {
        Response response = Jsoup.connect(url)
                .userAgent(applicationProperties.getUserAgent())
                .ignoreContentType(true)
                .execute();
        HtmlPage page = HtmlPage.builder()
                        .path(response.url().getPath())
                        .statusCode(response.statusCode())
                        .content(clean(response.body()))
                        .siteName(site.getName())
                        .title(response.parse().title())
                        .build();
        htmlPageRepository.save(page);
        if (page.getStatusCode() == 200) {
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }
        return page;
    }

    private String clean(String html) {
        return Jsoup.clean(html, Safelist.none()).replace("&nbsp", " ");
    }
}