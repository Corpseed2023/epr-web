// src/main/java/com/epr/repository/BlogRepository.java
package com.epr.repository;

import com.epr.entity.Blogs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blogs, Long> {

    /* ==================== ADMIN QUERIES (already existed) ==================== */
    Optional<Blogs> findByIdAndDeleteStatus(Long id, int deleteStatus);

    List<Blogs> findAllByDeleteStatus(int deleteStatus);

    @Query("SELECT b FROM Blogs b WHERE b.deleteStatus = 2 " +
            "AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.searchKeyword) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Blogs> searchActiveBlogs(@Param("keyword") String keyword);

    boolean existsByTitleIgnoreCase(String title);
    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);
    boolean existsBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

    /* ==================== PUBLIC / CUSTOMER QUERIES ==================== */

    /**
     * Find blog by slug (case-insensitive) + must be active + visible
     */
    @Query("SELECT b FROM Blogs b WHERE LOWER(b.slug) = LOWER(:slug) " +
            "AND b.deleteStatus = 2 AND b.displayStatus = 1")
    Optional<Blogs> findBySlugIgnoreCaseAndDeleteStatusAndDisplayStatus(
            @Param("slug") String slug, int deleteStatus, int displayStatus);

    /**
     * All public visible blogs (deleteStatus=2, displayStatus=1)
     */
    @Query("SELECT b FROM Blogs b WHERE b.deleteStatus = 2 AND b.displayStatus = 1 " +
            "ORDER BY b.postDate DESC")
    List<Blogs> findAllByDeleteStatusAndDisplayStatus(int deleteStatus, int displayStatus);

    /**
     * Latest N public blogs (for /latest endpoint)
     */
    @Query("SELECT b FROM Blogs b WHERE b.deleteStatus = 2 AND b.displayStatus = 1 " +
            "ORDER BY b.postDate DESC")
    Page<Blogs> findLatestPublicBlogs(Pageable pageable);

    /**
     * Blogs by Category ID
     */
    @Query("SELECT b FROM Blogs b WHERE b.category.id = :categoryId " +
            "AND b.deleteStatus = 2 AND b.displayStatus = 1 " +
            "ORDER BY b.postDate DESC")
    List<Blogs> findByCategoryIdAndDeleteStatusAndDisplayStatus(
            @Param("categoryId") Long categoryId, int deleteStatus, int displayStatus);

    /**
     * Blogs by Subcategory ID
     */
    @Query("SELECT b FROM Blogs b WHERE b.subcategory.id = :subcategoryId " +
            "AND b.deleteStatus = 2 AND b.displayStatus = 1 " +
            "ORDER BY b.postDate DESC")
    List<Blogs> findBySubcategoryIdAndDeleteStatusAndDisplayStatus(
            @Param("subcategoryId") Long subcategoryId, int deleteStatus, int displayStatus);

    /**
     * Featured blogs (showHomeStatus = 1)
     */
    @Query("SELECT b FROM Blogs b WHERE b.showHomeStatus = :showHome " +
            "AND b.deleteStatus = 2 AND b.displayStatus = 1 " +
            "ORDER BY b.postDate DESC")
    List<Blogs> findByShowHomeStatusAndDeleteStatusAndDisplayStatus(
            @Param("showHome") int showHomeStatus, int deleteStatus, int displayStatus);


    @Query("SELECT b FROM Blogs b JOIN b.services s " +
            "WHERE s.id = :serviceId " +
            "AND b.deleteStatus = 2 AND b.displayStatus = 1 " +
            "ORDER BY b.postDate DESC")
    List<Blogs> findPublicByServiceId(@Param("serviceId") Long serviceId);



}