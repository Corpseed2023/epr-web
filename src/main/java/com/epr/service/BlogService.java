// src/main/java/com/epr/service/BlogService.java
package com.epr.service;

import com.epr.dto.admin.blog.BlogRequestDto;
import com.epr.dto.admin.blog.BlogResponseDto;
import com.epr.dto.customer.BlogCustomerDto;

import java.util.List;

public interface BlogService {

    /* ======================= ADMIN APIs ======================= */
    List<BlogResponseDto> findAllActiveBlogs();

    List<BlogResponseDto> searchBlogs(String keyword);

    BlogResponseDto findById(Long id);

    BlogResponseDto createBlog(BlogRequestDto dto, Long userId);

    BlogResponseDto updateBlog(Long id, BlogRequestDto dto, Long userId);

    void softDeleteBlog(Long id, Long userId);

    /* ======================= PUBLIC / CUSTOMER APIs ======================= */

    /**
     * Get all visible blogs for frontend (displayStatus = 1, deleteStatus = 2)
     */
    List<BlogCustomerDto> findAllPublicBlogs();

    /**
     * Get blog detail by slug (SEO friendly URL)
     * Also increments visit count
     */
    BlogCustomerDto findPublicBySlugAndIncrementVisit(String slug);

    /**
     * Latest N blogs (e.g., latest 10)
     */
    List<BlogCustomerDto> findLatestPublicBlogs(int limit);

    /**
     * Blogs by Category ID
     */
    List<BlogCustomerDto> findPublicByCategoryId(Long categoryId);

    /**
     * Blogs by Subcategory ID
     */
    List<BlogCustomerDto> findPublicBySubcategoryId(Long subcategoryId);

    /**
     * Search blogs (public version - only visible blogs)
     */
    List<BlogCustomerDto> searchPublicBlogs(String keyword);

    /**
     * Featured blogs (showHomeStatus = 1)
     */
    List<BlogCustomerDto> findFeaturedPublicBlogs();

    List<BlogCustomerDto> findPublicBlogsByServiceId(Long serviceId);
}