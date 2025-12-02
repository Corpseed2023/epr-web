// src/main/java/com/epr/controller/BlogController.java
package com.epr.controller;

import com.epr.dto.blog.BlogRequestDto;
import com.epr.dto.blog.BlogResponseDto;
import com.epr.error.ApiResponse;
import com.epr.service.BlogService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private static final Logger log = LoggerFactory.getLogger(BlogController.class);

    @Autowired
    private BlogService blogService;

    @GetMapping
    public ResponseEntity<List<BlogResponseDto>> getAllActiveBlogs() {
        List<BlogResponseDto> blogs = blogService.findAllActiveBlogs();
        return blogs.isEmpty()
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : ResponseEntity.ok(blogs);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BlogResponseDto>> searchBlogs(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(blogService.searchBlogs(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponseDto> getBlogById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(blogService.findById(id));
        } catch (IllegalArgumentException e) {
            log.warn("Blog not found: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<?> createBlog(
            @Valid @RequestBody BlogRequestDto dto,
            @RequestParam Long userId) {

        log.info("Creating blog by userId={}", userId);
        try {
            BlogResponseDto saved = blogService.createBlog(dto, userId);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating blog", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create blog", 500));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBlog(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @RequestBody BlogRequestDto dto) {

        log.info("Updating blog ID={} by userId={}", id, userId);
        try {
            BlogResponseDto updated = blogService.updateBlog(id, dto, userId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            boolean notFound = e.getMessage().toLowerCase().contains("not found");
            HttpStatus status = notFound ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(ApiResponse.error(e.getMessage(), status.value()));
        } catch (Exception e) {
            log.error("Unexpected error updating blog", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to update blog", 500));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBlog(
            @PathVariable Long id,
            @RequestParam Long userId) {

        log.info("Soft deleting blog ID={} by userId={}", id, userId);
        try {
            blogService.softDeleteBlog(id, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            boolean notFound = e.getMessage().toLowerCase().contains("not found");
            HttpStatus status = notFound ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(ApiResponse.error(e.getMessage(), status.value()));
        } catch (Exception e) {
            log.error("Unexpected error deleting blog", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to delete blog", 500));
        }
    }
}