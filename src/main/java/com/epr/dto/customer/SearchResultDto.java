package com.epr.dto.customer;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchResultDto {
    private List<BlogCustomerDto> blogs;
    private List<ServiceCustomerDto> services;

    public SearchResultDto(List<BlogCustomerDto> blogs, List<ServiceCustomerDto> services) {
        this.blogs = blogs;
        this.services = services;
    }
}