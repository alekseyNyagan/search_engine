package main.repository;

import main.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {
    Optional<Site> findSiteByName(String name);

    Optional<Site> findSiteByUrl(String url);

    Set<Site> findByNameIn(Collection<String> names);
}
