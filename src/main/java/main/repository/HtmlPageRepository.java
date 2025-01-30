package main.repository;

import main.model.HtmlPage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HtmlPageRepository extends ElasticsearchRepository<HtmlPage, String> {
    void deleteByPath(String path);

    Optional<HtmlPage> findPageByPath(String path);

    void deleteAllBySiteName(String siteName);

    long countBySiteName(String siteName);

    SearchHits<HtmlPage> findBySiteName(String siteName);

    @Highlight(fields = {
            @HighlightField(name = "content"),
            @HighlightField(name = "title")
    })
    SearchPage<HtmlPage> findHtmlPagesByContentOrTitleAndStatusCode(String content, String title, Integer statusCode, Pageable pageable);

    @Highlight(fields = {
            @HighlightField(name = "content"),
            @HighlightField(name = "title")
    })
    SearchPage<HtmlPage> findHtmlPagesByContentOrTitleAndStatusCodeAndSiteName(String content, String title, Integer statusCode, String siteName, Pageable pageable);
}
