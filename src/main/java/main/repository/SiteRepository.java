package main.repository;

import main.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {
    public Optional<Site> findSiteByName(String name);

    public Optional<Site> findSiteByUrl(String url);
}
