package com.epr.dto.blogfaq;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlogFaqRequestDto {

    @NotBlank(message = "FAQ title is required")
    private String title;

    @NotBlank(message = "FAQ description is required")
    private String description;

    private Integer displayStatus; // 1 = show, 2 = hide (default 1)
}