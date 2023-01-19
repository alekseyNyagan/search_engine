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
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SearchSystemServiceImpl implements SearchSystemService {

    private final static int RESULTS_ON_PAGE = 10;

    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;

    @Autowired
    public SearchSystemServiceImpl(LemmaRepository lemmaRepository, PageRepository pageRepository) {
        this.lemmaRepository = lemmaRepository;
        this.pageRepository = pageRepository;
    }

    @Override
    public AbstractResponse search(String query, String site, int offset, int limit) throws IOException {
        if (query.isEmpty()) {
            return new ErrorResponse(false, "Задан пустой поисковый запрос");
        } else {
            SearchResponse searchResponse = new SearchResponse();
            Lemmatizator lemmatizator = new Lemmatizator();
            StringBuilder snippetString = new StringBuilder();
            Set<String> lemmas = lemmatizator.getQueryLemmas(query);
            TreeSet<Lemma> lemmaSet = new TreeSet<>(Comparator.comparingInt(Lemma::getFrequency));
            lemmaSet.addAll(site == null ? lemmaRepository.findAllByLemmas(lemmas) : lemmaRepository.findAllByLemmasAndSite(lemmas, site));
            List<Page> pages = getPages(new TreeSet<>(lemmaSet), snippetString);
            Map<Page, Float> relativeRelevance = getRelevance(pages, lemmaSet);
            List<PageDTO> dtos = buildPageDTOList(relativeRelevance, pages, snippetString, offset, limit);

            searchResponse.setResult(true);
            searchResponse.setCount(pages.size());
            searchResponse.setData(dtos);
            return searchResponse;
        }
    }

    private List<PageDTO> buildPageDTOList(Map<Page, Float> relativeRelevance, List<Page> pages, StringBuilder snippetString, int offset, int limit) {
        List<PageDTO> pageDTOs = new ArrayList<>();
        int pageNumber = offset / RESULTS_ON_PAGE + 1;

        Pattern snippetRegex = Pattern.compile("(\\w+)\\W+" + snippetString + "\\W+(\\w+)");
        for (int i = offset; i < (Math.min(pageNumber * limit, pages.size())); i++) {
            Page page = pages.get(i);
            String title = Jsoup.parse(page.getContent()).title();
            Matcher snippetMatcher = snippetRegex.matcher(page.getContent().replaceAll("\\<.*?\\>", " "));
            StringBuilder builder = new StringBuilder();
            while (snippetMatcher.find()) {
                String snippet = snippetMatcher.group();
                builder.append(builder.length() == 0 ? "" : ", ")
                        .append("<b>")
                        .append(snippet)
                        .append("</b>");
            }
            pageDTOs.add(new PageDTO(page.getSite().getUrl(), page.getSite().getName(), page.getPath()
                    , title
                    , builder.toString()
                    , relativeRelevance.get(page)));
        }
        pageDTOs.sort(Comparator.comparingDouble(PageDTO::getRelevance).reversed());
        return pageDTOs;
    }

    private Map<Page, Float> getRelevance(List<Page> pages, Set<Lemma> lemmaList) {
        Map<Page, Float> absoluteRelevance = new HashMap<>();
        Map<Page, Float> relativeRelevance = new HashMap<>();
        float max = 0;

        for (Page page : pages) {
            List<Index> indexList = page.getIndex();
            for (Lemma lemma : lemmaList) {
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

    private List<Page> getPages(TreeSet<Lemma> lemmaSet, StringBuilder snippetString) {
        List<Page> pages;
        Lemma fitstLemma = lemmaSet.pollFirst();
        if (fitstLemma != null) {
            snippetString.append(snippetString.length() == 0 ? "" : "|").append(fitstLemma.getLemma());
            pages = pageRepository.getPagesByLemma(fitstLemma, fitstLemma.getSite());
            pages.retainAll(searchPagesByNextLemma(pages, lemmaSet, snippetString));
        } else {
            pages = Collections.emptyList();
        }
        return pages;
    }

    private List<Page> searchPagesByNextLemma(List<Page> pages, TreeSet<Lemma> lemmaSet, StringBuilder snippetString) {
        Lemma lemma = lemmaSet.pollFirst();
        if (lemma != null) {
            snippetString.append("|").append(lemma.getLemma());
            List<Page> tempList = new ArrayList<>();
            for (Page page : pages) {
                if (page.getLemmas().stream().map(Lemma::getLemma).anyMatch(s -> s.equals(lemma.getLemma()))) {
                    tempList.add(page);
                }
            }
            searchPagesByNextLemma(tempList, lemmaSet, snippetString);
        }
        return pages;
    }
}
