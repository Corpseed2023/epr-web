package com.epr.dto.blogfaq;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlogFaqResponseDto {
    private Long id;
    private String uuid;
    private String title;
    private String description;
    private int displayStatus;
    private LocalDateTime postDate;
    private LocalDateTime modifyDate;
    private String addedByName;
    private int deleteStatus;
}
