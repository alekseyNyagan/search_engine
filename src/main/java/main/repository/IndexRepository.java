package main.repository;

import main.model.Index;
import main.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    public void deleteAllByPage(Page page);
}
