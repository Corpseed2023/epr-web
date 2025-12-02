// src/main/java/com/epr/controller/EnquiryController.java
package com.epr.controller;

import com.epr.dto.category.CategoryRequestDto;
import com.epr.dto.category.CategoryResponseDto;
import com.epr.dto.enquiry.EnquiryRequestDto;
import com.epr.dto.enquiry.EnquiryResponseDto;
import com.epr.error.ApiResponse;
import com.epr.service.EnquiryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enquiries")
public class EnquiryController {

    private static final Logger log = LoggerFactory.getLogger(EnquiryController.class);

    @Autowired
    private EnquiryService enquiryService;


    @GetMapping
    public ResponseEntity<List<EnquiryResponseDto>> getAll() {
        return ResponseEntity.ok(enquiryService.findAllActive());
    }

    @PostMapping
    public ResponseEntity<?> submitEnquiry(
            @Valid @RequestBody EnquiryRequestDto enquiryRequestDto,
            HttpServletRequest request) {

        try {
            String clientIp = getClientIp(request);
            String url = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

            EnquiryResponseDto saved = enquiryService.createEnquiry(enquiryRequestDto, clientIp, url);
            log.info("Enquiry created successfully: {}", saved.getName());
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.error("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while creating enquiry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create enquiry. Please try again later.",
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<EnquiryResponseDto> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(enquiryService.findById(id));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam Long userId) {
        try {
            enquiryService.softDeleteEnquiry(id, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(e.getMessage().contains("not found") ? 404 : 400)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}