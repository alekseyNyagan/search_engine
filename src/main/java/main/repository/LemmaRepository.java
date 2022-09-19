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
    @Query("DELETE FROM Lemma l WHERE l.id IN (:ids)")
    public void deleteLemmasByIds(@Param("ids") List<Integer> ids);

    @Query("FROM Lemma l WHERE l.lemma IN (:lemmas)")
    public Set<Lemma> findAllByLemmas(@Param("lemmas") Set<String> lemmas);

    @Query("FROM Lemma l JOIN Site s on s = l.site WHERE l.lemma IN (:lemmas) AND s.url = :url")
    public Set<Lemma> findAllByLemmasAndSite(@Param("lemmas") Set<String> lemmas, @Param("url") String site);
}
