// src/main/java/com/epr/repository/BlogRepository.java
package com.epr.repository;

import com.epr.entity.Blogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blogs, Long> {


    /** Find blog by ID and only if it's active (not soft-deleted) */
    Optional<Blogs> findByIdAndDeleteStatus(Long id, int deleteStatus);

    /** All active blogs */
    List<Blogs> findAllByDeleteStatus(int deleteStatus);

    @Query("SELECT b FROM Blogs b WHERE b.deleteStatus = 2 " +
            "AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.searchKeyword) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Blogs> searchActiveBlogs(@Param("keyword") String keyword);


    /** Check if title already exists (case-insensitive) */
    boolean existsByTitleIgnoreCase(String title);

    /** Check if title exists excluding current blog (for update) */
    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);

    /** Check if slug already exists (case-insensitive) */
    boolean existsBySlugIgnoreCase(String slug);

    /** Check if slug exists excluding current blog */
    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);


}