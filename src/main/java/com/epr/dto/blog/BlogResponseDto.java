package com.epr.dto.blog;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BlogResponseDto {
    private Long id;
    private String uuid;
    private String title;
    private String slug;
    private String image;
    private String summary;
    private String description;
    private String metaTitle;
    private String metaKeyword;
    private String metaDescription;
    private Integer displayStatus;
    private String searchKeyword;

    private Long categoryId;
    private String categoryName;
    private Long subcategoryId;
    private String subcategoryName;

    private List<Long> serviceIds;
    private List<String> serviceTitles;

    private String postDate;      // formatted IST string
    private String modifyDate;    // formatted IST string
    private String postedByName;
    private Long visited;
    private Integer deleteStatus;
}