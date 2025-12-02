package com.epr.serviceimpl;

import com.epr.dto.category.CategoryRequestDto;
import com.epr.dto.category.CategoryResponseDto;
import com.epr.entity.Category;
import com.epr.entity.User;
import com.epr.repository.CategoryRepository;
import com.epr.repository.UserRepository;
import com.epr.service.CategoryService;
import com.epr.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    // Reusable method to validate active user
    private User validateAndGetActiveUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID is required");
        }
        return userRepository.findActiveUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found or account is inactive/deleted"));
    }

    @Override
    public List<CategoryResponseDto> findAllActiveCategories() {
        return categoryRepository.findByDeleteStatus(2).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDto findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid category ID");
        }
        Category category = categoryRepository.findByIdAndDeleteStatus(id, 2)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        return toResponseDto(category);
    }

    @Override
    public CategoryResponseDto createCategory(CategoryRequestDto dto, Long userId) {
        validateDto(dto);

        User currentUser = validateAndGetActiveUser(userId);


        String name = dto.getName().trim();
        String slug = dto.getSlug().trim().toLowerCase();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category name already exists");
        }
        if (categoryRepository.existsBySlugIgnoreCase(slug)) {
            throw new IllegalArgumentException("Category slug already exists");
        }

        Category category = new Category();
        mapRequestToEntity(dto, category);

        category.setUuid(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        category.setPostDate(dateTimeUtil.getCurrentUtcTime());
        category.setAddedByUUID(currentUser.getUuid());

        Category saved = categoryRepository.save(category);
        return toResponseDto(saved);
    }

    @Override
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto, Long userId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid category ID");
        }

        validateDto(dto);
        User currentUser = validateAndGetActiveUser(userId);

        Category existing = categoryRepository.findByIdAndDeleteStatus(id, 2)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        String newName = dto.getName().trim();
        String newSlug = dto.getSlug().trim().toLowerCase();

        if (!existing.getName().equalsIgnoreCase(newName) &&
                categoryRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {
            throw new IllegalArgumentException("Category name already exists");
        }
        if (!existing.getSlug().equalsIgnoreCase(newSlug) &&
                categoryRepository.existsBySlugIgnoreCaseAndIdNot(newSlug, id)) {
            throw new IllegalArgumentException("Category slug already exists");
        }

        mapRequestToEntity(dto, existing);
        existing.setModifyDate(dateTimeUtil.getCurrentUtcTime());
        existing.setModifyByUUID(currentUser.getUuid());

        Category updated = categoryRepository.save(existing);
        return toResponseDto(updated);
    }

    @Override
    public void softDeleteCategory(Long id, Long userId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid category ID");
        }

        User currentUser = validateAndGetActiveUser(userId);

        Category category = categoryRepository.findByIdAndDeleteStatus(id, 2)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (!category.getSubcategories().isEmpty() || !category.getServices().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category that has subcategories or services");
        }

        category.setDeleteStatus(1);
        category.setModifyDate(dateTimeUtil.getCurrentUtcTime());
        category.setModifyByUUID(currentUser.getUuid());

        categoryRepository.save(category);
    }

    // Helper Methods
    private void validateDto(CategoryRequestDto dto) {
        if (dto == null) throw new IllegalArgumentException("Category data is required");
        if (dto.getName() == null || dto.getName().trim().isEmpty())
            throw new IllegalArgumentException("Category name is required");
        if (dto.getSlug() == null || dto.getSlug().trim().isEmpty())
            throw new IllegalArgumentException("Category slug is required");
    }

    private void mapRequestToEntity(CategoryRequestDto dto, Category entity) {
        entity.setName(dto.getName().trim());
        entity.setSlug(dto.getSlug().trim().toLowerCase());
        entity.setIcon(dto.getIcon());
        entity.setMetaTitle(dto.getMetaTitle());
        entity.setMetaKeyword(dto.getMetaKeyword());
        entity.setMetaDescription(dto.getMetaDescription());
        entity.setSearchKeywords(dto.getSearchKeywords());
    }

    private CategoryResponseDto toResponseDto(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setUuid(category.getUuid());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setIcon(category.getIcon());
        dto.setMetaTitle(category.getMetaTitle());
        dto.setMetaKeyword(category.getMetaKeyword());
        dto.setMetaDescription(category.getMetaDescription());
        dto.setSearchKeywords(category.getSearchKeywords());
        dto.setPostDate(category.getPostDate());
        dto.setModifyDate(category.getModifyDate());
        dto.setDeleteStatus(category.getDeleteStatus());
        dto.setAddedByUUID(category.getAddedByUUID());
        dto.setModifyByUUID(category.getModifyByUUID());
        return dto;
    }
}