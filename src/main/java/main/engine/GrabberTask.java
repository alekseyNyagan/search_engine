package main.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.model.Site;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Slf4j
public class GrabberTask extends RecursiveAction {

    private static final Pattern URL_PATTERN = Pattern.compile("(https?|ftp|file)://[-a-zA-Z\\d+&@#/%?=~_|!:,.;]*[-a-zA-Z\\d+&@#/%=~_|]");

    private final String url;
    private final Set<String> visited;
    private final Site site;

    private final WebPageClient webPageClient;

    @Override
    protected void compute() {
        try {
            webPageClient.savePage(url, site);
            Set<String> children = extractChildren(url);

            invokeAll(children.stream()
                    .map(link -> new GrabberTask(link, visited, site, webPageClient))
                    .toList());

        } catch (IOException e) {
            log.error("Ошибка ввода-вывода: {}", e.getMessage());
        }
    }

    private Set<String> extractChildren(String parent) throws IOException {
        Document document = webPageClient.getDocument(parent);
        Set<String> kids = new HashSet<>();
        for (Element a : document.select("a[href]")) {
            String child = a.absUrl("href");
            if (isValid(parent, child) && visited.add(child)) {
                kids.add(child);
            }
        }
        return kids;
    }


    private boolean isValid(String parent, String candidate) {
        return candidate.startsWith(parent) &&
                !candidate.contains("#") &&
                !candidate.contains("?") &&
                URL_PATTERN.matcher(candidate).matches();
    }
}
