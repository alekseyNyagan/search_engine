package main.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.model.HtmlPage;
import main.model.Site;
import main.repository.HtmlPageRepository;
import main.repository.SiteRepository;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Slf4j
public class GrabberTask extends RecursiveAction {

    private final transient HTMLStorage htmlStorage;
    private final String siteName;
    private final String url;
    private final HashSet<String> links;
    private final Site site;
    private final transient HtmlPageRepository htmlPageRepository;
    private final transient SiteRepository siteRepository;

    @Override
    protected void compute() {
        try {
            Thread.sleep(150);
            Set<GrabberTask> taskList = new HashSet<>();
            parseDoc(url);
            Set<String> childLinks = getChildren(url);

            for (String child : childLinks) {
                GrabberTask task = new GrabberTask(htmlStorage, siteName, child, links, site, htmlPageRepository, siteRepository);
                taskList.add(task);
                task.fork();
            }

            for (GrabberTask task : taskList) {
                task.join();
            }
        } catch (IOException e) {
            log.error("Ошибка ввода-вывода: {}", e.getMessage());
        } catch (InterruptedException e) {
            log.error("Ошибка потока: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public HtmlPage parseDoc(String url) throws IOException {
        Response response = htmlStorage.getResponse(url);
        HtmlPage page = (response != null) ?
                HtmlPage.builder()
                        .path(response.url().getPath())
                        .statusCode(response.statusCode())
                        .content(Jsoup.clean(response.body(), Safelist.none()).replace("&nbsp", " "))
                        .siteName(siteName)
                        .title(response.parse().title())
                        .build() :
                HtmlPage.builder()
                        .path("/" + url.replaceFirst(siteName, ""))
                        .statusCode(404)
                        .content("")
                        .siteName(siteName)
                        .title("")
                        .build();
        htmlPageRepository.save(page);
        if (page.getStatusCode() == 200) {
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }
        return page;
    }

    private Set<String> getChildren(String mainUrl) throws IOException {
        Set<String> linksSet = new HashSet<>();
        Elements elements = htmlStorage.getDocument(mainUrl).select("a");
        for (Element element : elements) {
            String elementUrl = element.absUrl("href");
            if (checkLink(mainUrl, elementUrl) && !links.contains(elementUrl)) {
                links.add(elementUrl);
                linksSet.add(elementUrl);
            }

        }
        return linksSet;
    }


    private boolean checkLink(String parentUrl, String checkUrl) {
        Pattern urlRegex = Pattern.compile("(https?|ftp|file)://[-a-zA-Z\\d+&@#/%?=~_|!:,.;]*[-a-zA-Z\\d+&@#/%=~_|]");
        Matcher urlMatcher = urlRegex.matcher(checkUrl);
        return (checkUrl.startsWith(parentUrl)) &&
                (!checkUrl.contains("#")) &&
                (!checkUrl.contains("?")) &&
                (urlMatcher.find());
    }
}
