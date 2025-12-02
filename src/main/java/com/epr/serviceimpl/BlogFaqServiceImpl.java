package com.epr.serviceimpl;

import com.epr.dto.blogfaq.BlogFaqRequestDto;
import com.epr.dto.blogfaq.BlogFaqResponseDto;
import com.epr.entity.BlogFaq;
import com.epr.entity.Blogs;
import com.epr.entity.User;
import com.epr.repository.BlogFaqRepository;
import com.epr.repository.BlogRepository;
import com.epr.repository.UserRepository;
import com.epr.service.BlogFaqService;
import com.epr.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogFaqServiceImpl implements BlogFaqService {

    private static final Logger log = LoggerFactory.getLogger(BlogFaqServiceImpl.class);

    private final BlogFaqRepository faqRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final DateTimeUtil dateTimeUtil;

    private User validateUser(Long userId) {
        return userRepository.findActiveUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found or inactive"));
    }

    private Blogs validateBlog(Long blogId) {
        return blogRepository.findByIdAndDeleteStatus(blogId, 2)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found or deleted"));
    }



    @Override
    public BlogFaqResponseDto createFaq(Long blogId, BlogFaqRequestDto dto, Long userId) {
        validateUser(userId);
        Blogs blog = validateBlog(blogId);

        BlogFaq faq = new BlogFaq();
        faq.setUuid(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        faq.setTitle(dto.getTitle().trim());
        faq.setDescription(dto.getDescription());
        faq.setDisplayStatus(dto.getDisplayStatus() != null ? dto.getDisplayStatus() : 1);
        faq.setBlog(blog);
        faq.setPostDate(dateTimeUtil.getCurrentUtcTime());
        faq.setAddedByUUID(userRepository.findById(userId).get().getUuid());

        BlogFaq saved = faqRepository.save(faq);
        log.info("Blog FAQ created: {} for blog ID: {}", saved.getId(), blogId);
        return toResponseDto(saved);
    }

    @Override
    public BlogFaqResponseDto updateFaq(Long blogId, Long faqId, BlogFaqRequestDto dto, Long userId) {
        validateUser(userId);
        validateBlog(blogId);

        BlogFaq faq = faqRepository.findActiveByBlogIdAndId(blogId, faqId)
                .orElseThrow(() -> new IllegalArgumentException("FAQ not found"));

        faq.setTitle(dto.getTitle().trim());
        faq.setDescription(dto.getDescription());
        faq.setDisplayStatus(dto.getDisplayStatus() != null ? dto.getDisplayStatus() : 1);
        faq.setModifyDate(dateTimeUtil.getCurrentUtcTime());
        faq.setModifyByUUID(userRepository.findById(userId).get().getUuid());

        BlogFaq updated = faqRepository.save(faq);
        log.info("Blog FAQ updated: {} (Blog: {})", faqId, blogId);
        return toResponseDto(updated);
    }

    @Override
    public void softDeleteFaq(Long blogId, Long faqId, Long userId) {
        validateUser(userId);
        validateBlog(blogId);

        BlogFaq faq = faqRepository.findActiveByBlogIdAndId(blogId, faqId)
                .orElseThrow(() -> new IllegalArgumentException("FAQ not found"));

        faq.setDeleteStatus(1);
        faq.setModifyDate(dateTimeUtil.getCurrentUtcTime());
        faqRepository.save(faq);
        log.info("Blog FAQ soft deleted: {} (Blog: {}) by user: {}", faqId, blogId, userId);
    }

    private BlogFaqResponseDto toResponseDto(BlogFaq f) {
        BlogFaqResponseDto dto = new BlogFaqResponseDto();
        dto.setId(f.getId());
        dto.setUuid(f.getUuid());
        dto.setTitle(f.getTitle());
        dto.setDescription(f.getDescription());
        dto.setDisplayStatus(f.getDisplayStatus());
        dto.setPostDate(f.getPostDate());
        dto.setModifyDate(f.getModifyDate());
        dto.setDeleteStatus(f.getDeleteStatus());

        if (f.getAddedByUUID() != null) {
            userRepository.findByUuid(f.getAddedByUUID())
                    .ifPresent(u -> dto.setAddedByName(u.getFullName() != null ? u.getFullName() : u.getFullName()));
        }
        return dto;
    }
}