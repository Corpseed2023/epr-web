// src/main/java/com/epr/serviceimpl/SearchServiceImpl.java

package com.epr.serviceimpl;

import com.epr.dto.customer.BlogCustomerDto;
import com.epr.dto.customer.SearchResultDto;
import com.epr.dto.customer.ServiceCustomerDto;
import com.epr.service.BlogService;
import com.epr.service.SearchService;
import com.epr.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final BlogService blogService;
    private final ServiceService serviceService;

    @Override
    public SearchResultDto searchPublic(String keyword) {
        List<BlogCustomerDto> blogs = blogService.searchPublicBlogs(keyword);
        List<ServiceCustomerDto> services = serviceService.searchPublicServices(keyword);
        return new SearchResultDto(blogs, services);
    }
}