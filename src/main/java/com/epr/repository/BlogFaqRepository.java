package com.epr.repository;


import com.epr.entity.BlogFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlogFaqRepository extends JpaRepository<BlogFaq, Long> {

    @Query("SELECT f FROM BlogFaq f WHERE f.blog.id = :blogId AND f.id = :faqId AND f.deleteStatus = 2")
    Optional<BlogFaq> findActiveByBlogIdAndId(@Param("blogId") Long blogId, @Param("faqId") Long faqId);
}