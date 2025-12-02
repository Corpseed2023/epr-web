package com.epr.service;


import com.epr.dto.blogfaq.BlogFaqRequestDto;
import com.epr.dto.blogfaq.BlogFaqResponseDto;

import java.util.List;

public interface BlogFaqService {


    BlogFaqResponseDto createFaq(Long blogId, BlogFaqRequestDto dto, Long userId);

    BlogFaqResponseDto updateFaq(Long blogId, Long faqId, BlogFaqRequestDto dto, Long userId);

    void softDeleteFaq(Long blogId, Long faqId, Long userId);
}