package com.epr.dto.admin.client;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClientResponseDto {
    private Long id;
    private String uuid;
    private String name;
    private String logo;
    private String websiteUrl;
    private String description;
    private String slug;
    private String displayStatus;
    private LocalDateTime postDate;
    private LocalDateTime modifyDate;
    private int deleteStatus;
}