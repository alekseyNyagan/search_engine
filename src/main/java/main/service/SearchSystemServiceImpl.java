package main.service;

import lombok.RequiredArgsConstructor;
import main.api.response.AbstractResponse;
import main.api.response.AppSearchResponse;
import main.dto.PageDTO;
import main.model.HtmlPage;
import main.model.Site;
import main.repository.HtmlPageRepository;
import main.repository.SiteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchSystemServiceImpl implements SearchSystemService {

    private static final int POSTS_ON_PAGE = 10;
    private final HtmlPageRepository htmlPageRepository;
    private final SiteRepository siteRepository;

    @Override
    public AbstractResponse search(String query, String site, int offset, int limit) {
        int page = offset / POSTS_ON_PAGE;
        SearchPage<HtmlPage> htmlPagesByContent;
        if (site == null) {
            htmlPagesByContent = htmlPageRepository
                    .findHtmlPagesByContentOrTitleAndStatusCode(query, query, 200, PageRequest.of(page, limit));
        } else {
            htmlPagesByContent = htmlPageRepository
                    .findHtmlPagesByContentOrTitleAndStatusCodeAndSiteName(query, query, 200, site, PageRequest.of(page, limit));
        }

        Set<String> siteNames = htmlPagesByContent.getSearchHits().stream()
                .map(searchHit -> searchHit.getContent().getSiteName())
                .collect(Collectors.toSet());

        Map<String, Site> sitesByName = siteRepository.findByNameIn(siteNames).stream()
                .collect(Collectors.toMap(Site::getName, Function.identity()));

        List<PageDTO> pageDTOS = htmlPagesByContent.getSearchHits().stream()
                .map(searchHit -> mapToPageDTO(searchHit, sitesByName)).toList();
        AppSearchResponse appSearchResponse = new AppSearchResponse();
        appSearchResponse.setData(pageDTOS);
        appSearchResponse.setResult(true);
        appSearchResponse.setCount(htmlPagesByContent.getTotalElements());
        return appSearchResponse;
    }

    private PageDTO mapToPageDTO(SearchHit<HtmlPage> searchHit, Map<String, Site> sitesByName) {
        HtmlPage page = searchHit.getContent();

        String snippet = createSnippet(page.getContent(), searchHit.getHighlightFields());

        return new PageDTO(
                sitesByName.get(page.getSiteName()).getUrl(),
                page.getSiteName(),
                page.getPath(),
                page.getTitle(),
                snippet,
                searchHit.getScore()
        );
    }

    private String createSnippet(String content, Map<String, List<String>> highlightFields) {
        if (highlightFields != null && highlightFields.containsKey("content")) {
            return String.join(" ... ", highlightFields.get("content"));
        }

        return content.length() > 200 ? content.substring(0, 200) + "..." : content;
    }
}
