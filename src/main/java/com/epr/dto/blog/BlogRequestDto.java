package com.epr.dto.blog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BlogRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Slug is required")
    private String slug;

    private String image;

    @NotBlank(message = "Summary is required")
    private String summary;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Meta title is required")
    private String metaTitle;

    @NotBlank(message = "Meta keyword is required")
    private String metaKeyword;

    @NotBlank(message = "Meta description is required")
    private String metaDescription;

    private Integer displayStatus;      // 1 = visible, 2 = hidden etc.
    private String searchKeyword;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Long subcategoryId;

    private List<Long> serviceIds;


}