package com.epr.controller.customer;

import com.epr.dto.customer.SearchResultDto;
import com.epr.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
@CrossOrigin(origins = "*") // Adjust in production
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResultDto> search(@RequestParam String q) {
        SearchResultDto results = searchService.searchPublic(q.trim());
        if (results.getBlogs().isEmpty() && results.getServices().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(results);
    }
}