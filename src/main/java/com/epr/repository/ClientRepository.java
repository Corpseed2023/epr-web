package com.epr.repository;

import com.epr.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("SELECT c FROM Client c WHERE c.deleteStatus = 2 ORDER BY c.name ASC")
    List<Client> findAllActive();

    @Query("SELECT c FROM Client c WHERE c.deleteStatus = 2 AND c.displayStatus = '1' ORDER BY c.name ASC")
    List<Client> findAllActiveAndVisible();

    @Query("SELECT c FROM Client c WHERE c.id = :id AND c.deleteStatus = 2")
    Optional<Client> findActiveById(@Param("id") Long id);

    @Query("SELECT c FROM Client c WHERE c.slug = :slug AND c.deleteStatus = 2 AND c.displayStatus = '1'")
    Optional<Client> findBySlugAndActiveAndVisible(@Param("slug") String slug);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

    @Query("SELECT c FROM Client c WHERE (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.websiteUrl) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND c.deleteStatus = 2")
    List<Client> searchActive(@Param("keyword") String keyword);
}