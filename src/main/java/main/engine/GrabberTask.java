package main.engine;

import lombok.RequiredArgsConstructor;
import main.model.Page;
import main.model.Site;
import main.repository.PageRepository;
import main.repository.SiteRepository;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class GrabberTask extends RecursiveAction {

    private final HTMLStorage htmlStorage;
    private final String siteName;
    private final String url;
    private final HashSet<String> links;
    private final Site site;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    @Override
    protected void compute() {
        try {
            Thread.sleep(150);
            Set<GrabberTask> taskList = new HashSet<>();
            parseDoc(url);
            Set<String> childLinks = getChildren(url);

            for (String child : childLinks) {
                GrabberTask task = new GrabberTask(htmlStorage, siteName, child, links, site, pageRepository, siteRepository);
                taskList.add(task);
                task.fork();
            }

            for (GrabberTask task : taskList) {
                task.join();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Set<String> getChildren(String mainUrl) throws IOException {
        Set<String> linksSet = new HashSet<>();
        Elements elements = htmlStorage.getDocument(mainUrl).select("a");
        for (Element element : elements) {
            String url = element.absUrl("href");
            if (checkLink(mainUrl, url)) {
                if (!links.contains(url)) {
                    links.add(url);
                    linksSet.add(url);
                }
            }
        }
        return linksSet;
    }

    private void parseDoc(String url) {
        try {
            Response response = htmlStorage.getResponse(url);
            Page page = (response != null) ?
                    new Page(response.url().getPath(), response.statusCode(), response.body(), site) :
                    new Page("/" + url.replaceFirst(siteName, ""), 404, "", site);
            pageRepository.save(page);
            if (page.getCode() == 200) {
                site.setStatusTime(LocalDateTime.now());
                siteRepository.save(site);
                htmlStorage.parseHTMLDocument(page);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
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
