package com.epr.serviceimpl;

import com.epr.dto.enquiry.EnquiryRequestDto;
import com.epr.dto.enquiry.EnquiryResponseDto;
import com.epr.entity.Enquiry;
import com.epr.entity.User;
import com.epr.repository.EnquiryRepository;
import com.epr.repository.UserRepository;
import com.epr.service.EnquiryService;
import com.epr.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EnquiryServiceImpl implements EnquiryService {

    private static final Logger log = LoggerFactory.getLogger(EnquiryServiceImpl.class);

    private final EnquiryRepository enquiryRepository;
    private final UserRepository userRepository;
    private final DateTimeUtil dateTimeUtil;

    private User validateAndGetActiveUser(Long userId) {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("User ID is required");
        return userRepository.findActiveUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found or inactive"));
    }


    @Override
    public EnquiryResponseDto createEnquiry(EnquiryRequestDto dto, String ipAddress, String url) {

        // Trim & lowercase
        String email = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : null;
        String mobile = dto.getMobile() != null ? dto.getMobile().trim() : null;

        // Validation: at least email OR mobile required
        if ((email == null || email.isBlank()) && (mobile == null || mobile.isBlank())) {
            throw new IllegalArgumentException("Please provide either email or mobile number");
        }

        // Check if enquiry with same email exists (active only)
        Optional<Enquiry> existingByEmail = email != null && !email.isBlank()
                ? enquiryRepository.findByEmailAndDeleteStatus(email, 2)
                : Optional.empty();

        if (existingByEmail.isPresent()) {
            Enquiry existing = existingByEmail.get();
            existing.setCount(existing.getCount() + 1L);
            // Optional: update latest message, type, UTM etc.
            updateExistingEnquiry(existing, dto, ipAddress, url);
            Enquiry saved = enquiryRepository.save(existing);
            log.info("Enquiry count increased to {} for email {} (ID: {})", saved.getCount(), email, saved.getId());
            return toResponseDto(saved);
        }

        // Check if enquiry with same mobile exists
        Optional<Enquiry> existingByMobile = mobile != null && !mobile.isBlank()
                ? enquiryRepository.findByMobileAndDeleteStatus(mobile, 2)
                : Optional.empty();

        if (existingByMobile.isPresent()) {
            Enquiry existing = existingByMobile.get();
            existing.setCount(existing.getCount() + 1L);
            updateExistingEnquiry(existing, dto, ipAddress, url);
            Enquiry saved = enquiryRepository.save(existing);
            log.info("Enquiry count increased to {} for mobile {} (ID: {})", saved.getCount(), mobile, saved.getId());
            return toResponseDto(saved);
        }

        // No duplicate → create new
        Enquiry enquiry = new Enquiry();
        enquiry.setUuid(java.util.UUID.randomUUID().toString());
        enquiry.setType(dto.getType());
        enquiry.setMessage(dto.getMessage());
        enquiry.setName(dto.getName() != null ? dto.getName().trim() : null);
        enquiry.setEmail(email);
        enquiry.setMobile(mobile);
        enquiry.setCity(dto.getCity() != null ? dto.getCity().trim() : null);
        enquiry.setCategoryId(dto.getCategoryId());
        enquiry.setServiceId(dto.getServiceId());

        // UTM
        enquiry.setUtmSource(dto.getUtmSource());
        enquiry.setUtmMedium(dto.getUtmMedium());
        enquiry.setUtmCampaign(dto.getUtmCampaign());
        enquiry.setUtmTerm(dto.getUtmTerm());
        enquiry.setUtmContent(dto.getUtmContent());

        enquiry.setIpAddress(ipAddress);
        enquiry.setUrl(url);
        enquiry.setDisplayStatus(1);
        enquiry.setDeleteStatus(2);
        enquiry.setCount(1L);

        Enquiry savedEnquiry = enquiryRepository.save(enquiry);
        log.info("New enquiry created with ID: {} from IP: {}", savedEnquiry.getId(), ipAddress);

        return toResponseDto(savedEnquiry);
    }

    // Helper method to update latest data when count increases
    private void updateExistingEnquiry(Enquiry existing, EnquiryRequestDto dto, String ipAddress, String url) {
        existing.setType(dto.getType());
        existing.setMessage(dto.getMessage());
        if (dto.getName() != null) existing.setName(dto.getName().trim());
        if (dto.getCity() != null) existing.setCity(dto.getCity().trim());
        existing.setCategoryId(dto.getCategoryId());
        existing.setServiceId(dto.getServiceId());
        existing.setUtmSource(dto.getUtmSource());
        existing.setUtmMedium(dto.getUtmMedium());
        existing.setUtmCampaign(dto.getUtmCampaign());
        existing.setUtmTerm(dto.getUtmTerm());
        existing.setUtmContent(dto.getUtmContent());
        existing.setIpAddress(ipAddress);
        existing.setUrl(url);
        // createdAt remains the first submission time
    }
    @Override
    public EnquiryResponseDto findById(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid enquiry ID");
        Enquiry enquiry = enquiryRepository.findByIdAndDeleteStatus(id, 2)
                .orElseThrow(() -> new IllegalArgumentException("Enquiry not found"));
        return toResponseDto(enquiry);
    }

    @Override
    public List<EnquiryResponseDto> findAllActive() {
        return enquiryRepository.findByDeleteStatusOrderByIdDesc(2)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void softDeleteEnquiry(Long id, Long userId) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid enquiry ID");
        validateAndGetActiveUser(userId);

        Enquiry enquiry = enquiryRepository.findByIdAndDeleteStatus(id, 2)
                .orElseThrow(() -> new IllegalArgumentException("Enquiry not found"));

        enquiry.setDeleteStatus(1);
        enquiryRepository.save(enquiry);
        log.info("Enquiry soft deleted: {} by user {}", id, userId);
    }



    private EnquiryResponseDto toResponseDto(Enquiry e) {
        EnquiryResponseDto dto = new EnquiryResponseDto();
        dto.setId(e.getId());
        dto.setUuid(e.getUuid());
        dto.setType(e.getType());
        dto.setMessage(e.getMessage());
        dto.setName(e.getName());
        dto.setEmail(e.getEmail());
        dto.setMobile(e.getMobile());
        dto.setCity(e.getCity());
        dto.setCategoryId(e.getCategoryId());
        dto.setServiceId(e.getServiceId());
        dto.setUrl(e.getUrl());
        dto.setIpAddress(e.getIpAddress());
        dto.setUtmSource(e.getUtmSource());
        dto.setUtmMedium(e.getUtmMedium());
        dto.setUtmCampaign(e.getUtmCampaign());
        dto.setDisplayStatus(e.getDisplayStatus());
        dto.setDeleteStatus(e.getDeleteStatus());
        dto.setCount(e.getCount());

        // Fix: Convert LocalDateTime → formatted String
        if (e.getCreatedAt() != null) {
            dto.setCreatedAt(dateTimeUtil.formatDateTimeIst(e.getCreatedAt()));
        } else {
            dto.setCreatedAt("N/A");
        }

        return dto;
    }
}