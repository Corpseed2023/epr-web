// src/main/java/com/epr/service/SearchService.java

package com.epr.service;

import com.epr.dto.customer.SearchResultDto;

public interface SearchService {

    /**
     * Central search for both blogs and services (public/visible only)
     * @param keyword Search term
     * @return Combined results
     */
    SearchResultDto searchPublic(String keyword);
}