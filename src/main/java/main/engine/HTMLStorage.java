package main.engine;

import main.configuration.ApplicationProperties;
import main.lemmatizator.Lemmatizator;
import main.model.*;
import main.repository.FieldRepository;
import main.repository.IndexRepository;
import main.repository.LemmaRepository;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;

public class HTMLStorage {
    private final Lemmatizator lemmatizator;
    private final FieldRepository fieldRepository;
    private final LemmaRepository lemmaRepository;
    private final Site site;
    private final IndexRepository indexRepository;
    private final ApplicationProperties applicationProperties;
    public HTMLStorage(FieldRepository fieldRepository, Site site, LemmaRepository lemmaRepository, IndexRepository indexRepository,
                       ApplicationProperties applicationProperties) throws IOException {
        lemmatizator = new Lemmatizator();
        this.fieldRepository = fieldRepository;
        this.site = site;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.applicationProperties = applicationProperties;
    }

    public Response getResponse(String url) throws IOException {
        return Jsoup.connect(url).userAgent(applicationProperties.getUserAgent()).ignoreContentType(true).execute();
    }

    public Document getDocument(String url) throws IOException {
        return Jsoup.connect(url).userAgent(applicationProperties.getUserAgent()).ignoreContentType(true).get();
    }

    public void parseHTMLDocument(Page page) {
        String htmlDoc = page.getContent();
        List<Field> fields = fieldRepository.findAll();
        Map<String, Float> rankMap = new HashMap<>();
        Set<Index> indexSet = new HashSet<>();
        for (Field field : fields) {
            String text = Jsoup.parse(htmlDoc).selectFirst(field.getSelector()).text();
            Map<String, Integer> lemmasMap = lemmatizator.getLemmas(text);

            for (String lemma : lemmasMap.keySet()) {
                float rank = rankMap.containsKey(lemma) ?
                        rankMap.get(lemma) + (lemmasMap.get(lemma) * field.getWeight()) :
                        lemmasMap.get(lemma) * field.getWeight();
                rankMap.put(lemma, rank);
            }

        }
        rankMap.forEach((lemma, rank) -> {
            Lemma lemmaToInsert = new Lemma(lemma, 1, site);
            lemmaRepository.save(lemmaToInsert);
            indexSet.add(new Index(page, lemmaToInsert, rank));
        });
        indexRepository.saveAll(indexSet);
    }
}

