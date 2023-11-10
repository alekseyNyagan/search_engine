package main.service;

import main.api.response.AbstractResponse;
import main.api.response.ErrorResponse;
import main.api.response.SearchResponse;
import main.dto.PageDTO;
import main.lemmatizator.Lemmatizator;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.repository.LemmaRepository;
import main.repository.PageRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.morphology.WrongCharaterException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class SearchSystemServiceImpl implements SearchSystemService {

    private final static int RESULTS_ON_PAGE = 10;
    private final static int SNIPPET_LENGTH = 50;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private List<Page> pages;
    private Map<Page, Float> relativeRelevance;
    private final Lemmatizator lemmatizator;
    private final static Logger LOGGER = LogManager.getLogger();

    @Autowired
    public SearchSystemServiceImpl(LemmaRepository lemmaRepository, PageRepository pageRepository) throws IOException {
        this.lemmaRepository = lemmaRepository;
        this.pageRepository = pageRepository;
        pages = new ArrayList<>();
        relativeRelevance = new HashMap<>();
        lemmatizator = new Lemmatizator();
    }

    @Override
    public AbstractResponse search(String query, String site, int offset, int limit) {
        Set<String> lemmas;
        if (query.isEmpty()) {
            return new ErrorResponse(false, "Задан пустой поисковый запрос");
        } else {
            SearchResponse searchResponse = new SearchResponse();
            lemmas = lemmatizator.getQueryLemmas(query);
            TreeSet<Lemma> lemmaSet = new TreeSet<>(Comparator.comparingInt(Lemma::getFrequency));
            lemmaSet.addAll(site == null ? lemmaRepository.findAllByLemmas(lemmas) : lemmaRepository.findAllByLemmasAndSite(lemmas, site));
            pages = getPages(new TreeSet<>(lemmaSet));
            relativeRelevance = getRelevance(pages, new TreeSet<>(lemmaSet));
            List<PageDTO> dtos = buildPageDTOList(relativeRelevance, pages, new ArrayList<>(lemmas), offset, limit);

            searchResponse.setResult(true);
            searchResponse.setCount(pages.size());
            searchResponse.setData(dtos);
            return searchResponse;
        }
    }

    private List<PageDTO> buildPageDTOList(Map<Page, Float> relativeRelevance, List<Page> pages, List<String> lemmas, int offset, int limit) {
        List<PageDTO> pageDTOs = new ArrayList<>();
        int pageNumber = offset / RESULTS_ON_PAGE + 1;
        for (int i = offset; i < (Math.min(pageNumber * limit, pages.size())); i++) {
            Page page = pages.get(i);
            String title = Jsoup.parse(page.getContent()).title();
            String content = Jsoup.clean(page.getContent(), Safelist.basic())
                    .replaceAll("(<.*?>)|(&nbsp)", " ")
                    .toLowerCase()
                    .replaceAll("\\s+", " ")
                    .trim();
            String snippet = getSnippet(lemmas, content);
            pageDTOs.add(new PageDTO(page.getSite().getUrl(), page.getSite().getName(), page.getPath()
                    , title
                    , snippet
                    , relativeRelevance.get(page)));
        }
        pageDTOs.sort(Comparator.comparingDouble(PageDTO::getRelevance).reversed());
        return pageDTOs;
    }

    private String getSnippet(List<String> lemmas, String content) {
        StringBuilder wordsToBold = new StringBuilder();
        StringBuilder snippet = new StringBuilder();

        for (String lemma : lemmas) {
            wordsToBold
                    .append(wordsToBold.length() == 0 ? "" : "|")
                    .append(lemma);
        }
        String lemma = lemmas.get(0);
        int wordIndex = 0;
        String[] contentArray = content.split(" ");

        for (String word : contentArray) {
            try {
                if (lemmatizator.getWordNormalForm(word).equals(lemma)) {
                    wordIndex = content.indexOf(word);
                    break;
                }
            } catch (WrongCharaterException exception) {
                LOGGER.warn(exception.getMessage());
            }
        }
        String snippetSubstring = getSnippetSubstring(content, wordIndex);

        for (String word : snippetSubstring.split(" ")) {
            try {
                snippet
                        .append(snippet.length() == 0 ? "" : " ")
                        .append(lemmas.contains(lemmatizator.getWordNormalForm(word)) ? "<b>" + word + "</b>" : word);
            } catch (WrongCharaterException exception) {
                LOGGER.warn(exception.getMessage());
            }
        }

        return snippet.toString();
    }

    private String getSnippetSubstring(String content, int wordIndex) {
        String snippetSubstring;
        int middleOfSnippet = SNIPPET_LENGTH / 2;
        if (wordIndex - middleOfSnippet <= 0) {
            int spaceIndex = content.substring(wordIndex, wordIndex + SNIPPET_LENGTH).lastIndexOf(" ");
            snippetSubstring = content.substring(wordIndex, spaceIndex);
        } else if (wordIndex + middleOfSnippet >= content.length() - 1) {
            int spaceIndex = content.substring(wordIndex - SNIPPET_LENGTH).indexOf(" ");
            snippetSubstring = content.substring(spaceIndex);
        } else {
            snippetSubstring = content.substring(wordIndex - middleOfSnippet, wordIndex + middleOfSnippet);
            int firstSpaceIndex = snippetSubstring.indexOf(" ") + 1;
            int lastSpaceIndex = snippetSubstring.lastIndexOf(" ");
            snippetSubstring = snippetSubstring.substring(firstSpaceIndex, lastSpaceIndex);
        }
        return snippetSubstring;
    }

    private Map<Page, Float> getRelevance(List<Page> pages, Set<Lemma> lemmasSet) {
        Map<Page, Float> absoluteRelevance = new HashMap<>();
        Map<Page, Float> relativeRelevance = new HashMap<>();
        float max = 0;

        for (Page page : pages) {
            List<Index> indexList = page.getIndex();
            for (Lemma lemma : lemmasSet) {
                Optional<Index> indexOptional = indexList.stream().filter(index -> index.getLemma() == lemma).findAny();
                if (indexOptional.isPresent()) {
                    float rank = indexOptional.get().getRank();
                    float relevance = 0;
                    relevance = relevance + rank;
                    absoluteRelevance.put(page, relevance);
                    if (relevance >= absoluteRelevance.values().stream().max(Comparator.comparing(Float::floatValue)).get()) {
                        max = relevance;
                    }
                }
            }
        }

        for (Page page : absoluteRelevance.keySet()) {
            relativeRelevance.put(page, absoluteRelevance.get(page) / max);
        }
        return relativeRelevance;
    }

    private List<Page> getPages(TreeSet<Lemma> lemmaSet) {
        List<Page> pages;
        Lemma firstLemma = lemmaSet.pollFirst();
        if (firstLemma != null) {
            pages = pageRepository.getPagesByLemma(firstLemma, firstLemma.getSite());
            pages.retainAll(searchPagesByNextLemma(pages, lemmaSet));
        } else {
            pages = Collections.emptyList();
        }
        return pages;
    }

    private List<Page> searchPagesByNextLemma(List<Page> pages, TreeSet<Lemma> lemmaSet) {
        Lemma lemma = lemmaSet.pollFirst();
        if (lemma != null) {
            List<Page> tempList = new ArrayList<>();
            for (Page page : pages) {
                if (page.getLemmas().stream().map(Lemma::getLemma).anyMatch(s -> s.equals(lemma.getLemma()))) {
                    tempList.add(page);
                }
            }
            searchPagesByNextLemma(tempList, lemmaSet);
        }
        return pages;
    }
}
