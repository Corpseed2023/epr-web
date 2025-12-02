package com.epr.service;



import com.epr.dto.enquiry.EnquiryRequestDto;
import com.epr.dto.enquiry.EnquiryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EnquiryService {

    EnquiryResponseDto findById(Long id);

    List<EnquiryResponseDto> findAllActive();

    void softDeleteEnquiry(Long id, Long userId);

    EnquiryResponseDto createEnquiry(EnquiryRequestDto enquiryRequestDto, String clientIp, String url);
}