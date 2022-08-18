package main.repository;

import main.model.Lemma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(nativeQuery = true, value = "DELETE FROM lemma WHERE lemma.id IN (:ids)")
    public void deleteLemmasByIds(@Param("ids") List<Integer> ids);

    @Query(nativeQuery = true, value = "SELECT * FROM lemma WHERE lemma IN (:lemmas)")
    public Set<Lemma> findAllByLemmas(@Param("lemmas") Set<String> lemmas);

    @Query(nativeQuery = true, value = "SELECT * FROM lemma l JOIN site s on s.id = l.site_id WHERE lemma IN (:lemmas) AND s.url = :url")
    public Set<Lemma> findAllByLemmasAndSite(@Param("lemmas") Set<String> lemmas, @Param("url") String site);
}
