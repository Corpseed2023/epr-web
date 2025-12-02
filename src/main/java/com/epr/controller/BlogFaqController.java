package com.epr.controller;

import com.epr.dto.blogfaq.BlogFaqRequestDto;
import com.epr.dto.blogfaq.BlogFaqResponseDto;
import com.epr.error.ApiResponse;
import com.epr.service.BlogFaqService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs/{blogId}/faqs")
public class BlogFaqController {

    private static final Logger log = LoggerFactory.getLogger(BlogFaqController.class);

    @Autowired
    private BlogFaqService blogFaqService;

    @PostMapping
    public ResponseEntity<?> createFaq(
            @PathVariable Long blogId,
            @Valid @RequestBody BlogFaqRequestDto dto,
            @RequestParam Long userId) {

        log.info("Creating FAQ for blogId={} by userId={}", blogId, userId);
        try {
            return new ResponseEntity<>(blogFaqService.createFaq(blogId, dto, userId), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating FAQ", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to create FAQ", 500));
        }
    }

    @PutMapping("/{faqId}")
    public ResponseEntity<?> updateFaq(
            @PathVariable Long blogId,
            @PathVariable Long faqId,
            @Valid @RequestBody BlogFaqRequestDto dto,
            @RequestParam Long userId) {

        log.info("Updating FAQ {} for blogId={} by userId={}", faqId, blogId, userId);
        try {
            return ResponseEntity.ok(blogFaqService.updateFaq(blogId, faqId, dto, userId));
        } catch (IllegalArgumentException e) {
            boolean notFound = e.getMessage().toLowerCase().contains("not found");
            return ResponseEntity.status(notFound ? 404 : 400)
                    .body(ApiResponse.error(e.getMessage(), notFound ? 404 : 400));
        } catch (Exception e) {
            log.error("Error updating FAQ", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to update FAQ", 500));
        }
    }

    @DeleteMapping("/{faqId}")
    public ResponseEntity<?> deleteFaq(
            @PathVariable Long blogId,
            @PathVariable Long faqId,
            @RequestParam Long userId) {

        log.info("Soft deleting FAQ {} for blogId={} by userId={}", faqId, blogId, userId);
        try {
            blogFaqService.softDeleteFaq(blogId, faqId, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            boolean notFound = e.getMessage().toLowerCase().contains("not found");
            return ResponseEntity.status(notFound ? 404 : 400)
                    .body(ApiResponse.error(e.getMessage(), notFound ? 404 : 400));
        } catch (Exception e) {
            log.error("Error deleting FAQ", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to delete FAQ", 500));
        }
    }
}