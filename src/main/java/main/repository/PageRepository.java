package main.repository;

import main.model.Lemma;
import main.model.Page;
import main.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    @Query(nativeQuery = true, value = "SELECT `index`.lemma_id FROM `index` JOIN lemma l on `index`.lemma_id = l.id JOIN `index` i on l.id = i.lemma_id " +
            "WHERE i.page_id = :pageId GROUP BY `index`.lemma_id HAVING COUNT(`index`.page_id) = 1")
    public List<Integer> lemmasIds(@Param("pageId") int pageId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    public void delete(Page page);

    public Optional<Page> findPageByPath(String url);

    @Query(value = "SELECT p FROM Index i JOIN i.page p JOIN i.lemma l WHERE l = ?1 AND l.site = ?2")
    public List<Page> getPagesByLemma(Lemma lemma, Site site);
}
