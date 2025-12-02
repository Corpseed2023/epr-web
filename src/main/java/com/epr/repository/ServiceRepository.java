// src/main/java/com/epr/repository/ServiceRepository.java
package com.epr.repository;

import com.epr.entity.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ServiceRepository extends JpaRepository<Services, Long> {

    @Query("SELECT s FROM Services s WHERE s.id = :id AND s.deleteStatus = 2")
    Optional<Services> findActiveById(@Param("id") Long id);

    @Query("SELECT s FROM Services s WHERE s.deleteStatus = 2 ORDER BY s.postDate DESC")
    List<Services> findAllActiveServices();

    @Query("SELECT s FROM Services s WHERE s.deleteStatus = 2 " +
            "AND (LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            "ORDER BY s.postDate DESC")
    List<Services> searchActiveServices(@Param("keyword") String keyword);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

    boolean existsByTitleIgnoreCase(String title);
    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);

    // ADD THIS MISSING METHOD
//    List<Services> findAllByIdInAndDeleteStatus(List<Long> ids, int deleteStatus);

    // Alternative (cleaner & more efficient) using @Query - RECOMMENDED
    @Query("SELECT s FROM Services s WHERE s.id IN :ids AND s.deleteStatus = :deleteStatus")
    List<Services> findAllByIdInAndDeleteStatus(@Param("ids") List<Long> ids, @Param("deleteStatus") int deleteStatus);
}