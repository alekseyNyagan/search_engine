package main.engine;

import lombok.extern.slf4j.Slf4j;
import main.configuration.ApplicationProperties;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

@Slf4j
public class HTMLStorage {
    private final ApplicationProperties applicationProperties;

    public HTMLStorage(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public Response getResponse(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(applicationProperties.getUserAgent())
                    .ignoreContentType(true)
                    .execute();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public Document getDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(applicationProperties.getUserAgent())
                .ignoreContentType(true)
                .get();
    }
}