package com.epr.serviceimpl;


import com.epr.dto.admin.blog.BlogRequestDto;
import com.epr.dto.admin.blog.BlogResponseDto;
import com.epr.dto.customer.BlogCustomerDto;
import com.epr.entity.Blogs;
import com.epr.entity.Category;
import com.epr.entity.Services;
import com.epr.entity.Subcategory;
import com.epr.entity.User;
import com.epr.repository.*;
import com.epr.service.BlogService;
import com.epr.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogServiceImpl implements BlogService {

    private static final Logger log = LoggerFactory.getLogger(BlogServiceImpl.class);

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final ServiceRepository serviceRepository;
    private final DateTimeUtil dateTimeUtil;

    private User validateAndGetActiveUser(Long userId) {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("User ID is required");
        return userRepository.findActiveUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found or inactive"));
    }

    @Override
    public List<BlogResponseDto> findAllActiveBlogs() {
        return blogRepository.findAllByDeleteStatus(2)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BlogResponseDto> searchBlogs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return findAllActiveBlogs();
        return blogRepository.searchActiveBlogs(keyword.trim())
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BlogResponseDto findById(Long id) {
        Blogs blog = blogRepository.findByIdAndDeleteStatus(id, 2)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found with id: " + id));
        return toResponseDto(blog);
    }

    @Override
    public BlogResponseDto createBlog(BlogRequestDto dto, Long userId) {
        validateDto(dto);
        User user = validateAndGetActiveUser(userId);

        String title = dto.getTitle().trim();
        String slug = dto.getSlug().trim().toLowerCase();

        if (blogRepository.existsByTitleIgnoreCase(title))
            throw new IllegalArgumentException("Blog with this title already exists");
        if (blogRepository.existsBySlugIgnoreCase(slug))
            throw new IllegalArgumentException("Blog slug already exists");

        Blogs blog = new Blogs();
        mapRequestToEntity(dto, blog);

        blog.setUuid(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        LocalDateTime now = dateTimeUtil.getCurrentUtcTime();
        blog.setPostDate(now);
        blog.setModifyDate(now);
        blog.setAddedByUUID(user.getUuid());
        blog.setPostedByUuid(user.getUuid());
        blog.setPostedByName(user.getFullName() != null ? user.getFullName() : user.getFullName());
        blog.setVisited(0L);
        blog.setDeleteStatus(2);

        setCategoryAndSubcategory(blog, dto.getCategoryId(), dto.getSubcategoryId());
        associateServices(blog, dto.getServiceIds());

        Blogs saved = blogRepository.save(blog);
        log.info("Blog created: {} by user {}", saved.getTitle(), userId);
        return toResponseDto(saved);
    }

    @Override
    public BlogResponseDto updateBlog(Long id, BlogRequestDto dto, Long userId) {
        validateDto(dto);
        User user = validateAndGetActiveUser(userId);

        Blogs existing = blogRepository.findByIdAndDeleteStatus(id, 2)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found"));

        String newTitle = dto.getTitle().trim();
        String newSlug = dto.getSlug().trim().toLowerCase();

        if (!existing.getTitle().equalsIgnoreCase(newTitle) &&
                blogRepository.existsByTitleIgnoreCaseAndIdNot(newTitle, id))
            throw new IllegalArgumentException("Blog with this title already exists");

        if (!existing.getSlug().equalsIgnoreCase(newSlug) &&
                blogRepository.existsBySlugIgnoreCaseAndIdNot(newSlug, id))
            throw new IllegalArgumentException("Blog slug already exists");

        mapRequestToEntity(dto, existing);
        existing.setModifyDate(dateTimeUtil.getCurrentUtcTime());
        existing.setModifyByUUID(user.getUuid());

        setCategoryAndSubcategory(existing, dto.getCategoryId(), dto.getSubcategoryId());
        associateServices(existing, dto.getServiceIds());

        Blogs updated = blogRepository.save(existing);
        log.info("Blog updated: {} (ID: {})", updated.getTitle(), updated.getId());
        return toResponseDto(updated);
    }

    @Override
    public void softDeleteBlog(Long id, Long userId) {
        validateAndGetActiveUser(userId);
        Blogs blog = blogRepository.findByIdAndDeleteStatus(id, 2)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found"));

        blog.setDeleteStatus(1);
        blog.setModifyDate(dateTimeUtil.getCurrentUtcTime());
        blogRepository.save(blog);
        log.info("Blog soft deleted: {} by user {}", id, userId);
    }

    // ------------------- Helpers -------------------

    private void validateDto(BlogRequestDto dto) {
        if (dto == null) throw new IllegalArgumentException("Blog data is required");
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Blog title is required");
        if (dto.getSlug() == null || dto.getSlug().trim().isEmpty())
            throw new IllegalArgumentException("Blog slug is required");
        if (dto.getCategoryId() == null || dto.getCategoryId() <= 0)
            throw new IllegalArgumentException("Valid category is required");
    }

    private void setCategoryAndSubcategory(Blogs blog, Long categoryId, Long subcategoryId) {
        Category category = categoryRepository.findByIdAndDeleteStatus(categoryId, 2)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or deleted"));
        blog.setCategory(category);

        if (subcategoryId != null && subcategoryId > 0) {
            Subcategory sub = subcategoryRepository.findByIdAndDeleteStatus(subcategoryId, 2)
                    .orElseThrow(() -> new IllegalArgumentException("Subcategory not found or deleted"));
            if (!sub.getCategory().getId().equals(categoryId))
                throw new IllegalArgumentException("Subcategory does not belong to selected category");
            blog.setSubcategory(sub);
        } else {
            blog.setSubcategory(null);
        }
    }

    private void associateServices(Blogs blog, List<Long> serviceIds) {
        blog.getServices().clear();
        if (serviceIds != null && !serviceIds.isEmpty()) {
            List<Services> services = serviceRepository.findAllByIdInAndDeleteStatus(serviceIds, 2);
            if (services.size() != serviceIds.size())
                throw new IllegalArgumentException("One or more services not found or deleted");
            blog.getServices().addAll(services);
        }
    }


    @Override
    public List<BlogCustomerDto> findAllPublicBlogs() {
        return blogRepository.findAllByDeleteStatusAndDisplayStatus(2, 1)
                .stream()
                .map(this::toCustomerDto)
                .collect(Collectors.toList());
    }

    @Override
    public BlogCustomerDto findPublicBySlugAndIncrementVisit(String slug) {
        if (slug == null || slug.trim().isEmpty()) return null;

        Blogs blog = blogRepository.findBySlugIgnoreCaseAndDeleteStatusAndDisplayStatus(slug.trim().toLowerCase(), 2, 1)
                .orElse(null);

        if (blog != null) {
            blog.setVisited(blog.getVisited() + 1);
            blogRepository.save(blog); // async in production
        }
        return blog != null ? toCustomerDto(blog) : null;
    }

    @Override
    public List<BlogCustomerDto> findLatestPublicBlogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return blogRepository.findLatestPublicBlogs(pageable)
                .stream()
                .map(this::toCustomerDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BlogCustomerDto> findPublicByCategoryId(Long categoryId) {
        if (categoryId == null || categoryId <= 0) return List.of();
        return blogRepository.findByCategoryIdAndDeleteStatusAndDisplayStatus(categoryId, 2, 1)
                .stream()
                .map(this::toCustomerDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BlogCustomerDto> findPublicBySubcategoryId(Long subcategoryId) {
        if (subcategoryId == null || subcategoryId <= 0) return List.of();
        return blogRepository.findBySubcategoryIdAndDeleteStatusAndDisplayStatus(subcategoryId, 2, 1)
                .stream()
                .map(this::toCustomerDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BlogCustomerDto> searchPublicBlogs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return findAllPublicBlogs();
        return blogRepository.searchActiveBlogs(keyword.trim())
                .stream()
                .filter(b -> b.getDisplayStatus() == 1)
                .map(this::toCustomerDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BlogCustomerDto> findFeaturedPublicBlogs() {
        return blogRepository.findByShowHomeStatusAndDeleteStatusAndDisplayStatus(1, 2, 1)
                .stream()
                .map(this::toCustomerDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<BlogCustomerDto> findPublicBlogsByServiceId(Long serviceId) {
        if (serviceId == null || serviceId <= 0) {
            return List.of();
        }
        return blogRepository.findPublicByServiceId(serviceId)
                .stream()
                .map(this::toCustomerDto)
                .collect(Collectors.toList());
    }

    private void mapRequestToEntity(BlogRequestDto dto, Blogs entity) {
        entity.setTitle(dto.getTitle().trim());
        entity.setSlug(dto.getSlug().trim().toLowerCase());
        entity.setImage(dto.getImage());
        entity.setSummary(dto.getSummary());
        entity.setDescription(dto.getDescription());
        entity.setMetaTitle(dto.getMetaTitle());
        entity.setMetaKeyword(dto.getMetaKeyword());
        entity.setMetaDescription(dto.getMetaDescription());
        entity.setSearchKeyword(dto.getSearchKeyword());
        entity.setDisplayStatus(dto.getDisplayStatus() != null ? dto.getDisplayStatus() : 1);
    }

    private BlogResponseDto toResponseDto(Blogs b) {
        BlogResponseDto dto = new BlogResponseDto();
        dto.setId(b.getId());
        dto.setUuid(b.getUuid());
        dto.setTitle(b.getTitle());
        dto.setSlug(b.getSlug());
        dto.setImage(b.getImage());
        dto.setSummary(b.getSummary());
        dto.setDescription(b.getDescription());
        dto.setMetaTitle(b.getMetaTitle());
        dto.setMetaKeyword(b.getMetaKeyword());
        dto.setMetaDescription(b.getMetaDescription());
        dto.setDisplayStatus(b.getDisplayStatus());
        dto.setSearchKeyword(b.getSearchKeyword());
        dto.setPostDate(dateTimeUtil.formatDateTimeIst(b.getPostDate()));
        dto.setModifyDate(b.getModifyDate() != null ? dateTimeUtil.formatDateTimeIst(b.getModifyDate()) : null);
        dto.setPostedByName(b.getPostedByName());
        dto.setVisited(b.getVisited());
        dto.setDeleteStatus(b.getDeleteStatus());

        if (b.getCategory() != null) {
            dto.setCategoryId(b.getCategory().getId());
            dto.setCategoryName(b.getCategory().getName());
        }
        if (b.getSubcategory() != null) {
            dto.setSubcategoryId(b.getSubcategory().getId());
            dto.setSubcategoryName(b.getSubcategory().getName());
        }

        dto.setServiceIds(b.getServices().stream().map(Services::getId).collect(Collectors.toList()));
        dto.setServiceTitles(b.getServices().stream().map(Services::getTitle).collect(Collectors.toList()));

        return dto;
    }

    private BlogCustomerDto toCustomerDto(Blogs b) {
        BlogCustomerDto dto = new BlogCustomerDto();
        dto.setId(b.getId());
        dto.setUuid(b.getUuid());
        dto.setTitle(b.getTitle());
        dto.setSlug(b.getSlug());
        dto.setImage(b.getImage());
        dto.setSummary(b.getSummary());
        dto.setDescription(b.getDescription());
        dto.setMetaTitle(b.getMetaTitle());
        dto.setMetaKeyword(b.getMetaKeyword());
        dto.setMetaDescription(b.getMetaDescription());
        dto.setPostDate(dateTimeUtil.formatDateTimeIst(b.getPostDate()));
        dto.setPostedByName(b.getPostedByName());
        dto.setVisited(b.getVisited());

        if (b.getCategory() != null) {
            dto.setCategoryId(b.getCategory().getId());
            dto.setCategoryName(b.getCategory().getName());
            dto.setCategorySlug(b.getCategory().getSlug());
        }
        if (b.getSubcategory() != null) {
            dto.setSubcategoryId(b.getSubcategory().getId());
            dto.setSubcategoryName(b.getSubcategory().getName());
            dto.setSubcategorySlug(b.getSubcategory().getSlug());
        }
        dto.setRelatedServiceTitles(b.getServices().stream()
                .map(Services::getTitle)
                .collect(Collectors.toList()));

        return dto;
    }
}
