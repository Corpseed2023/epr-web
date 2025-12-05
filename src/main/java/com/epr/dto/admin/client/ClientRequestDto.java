package com.epr.dto.admin.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRequestDto {

    @NotBlank(message = "Client name is required")
    private String name;

    private String logo;
    private String websiteUrl;
    private String description;
    private String slug;
    private String displayStatus;  // "1" or "2"
}