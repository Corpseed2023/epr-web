// src/main/java/com/epr/dto/enquiry/EnquiryRequestDto.java
package com.epr.dto.enquiry;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EnquiryRequestDto {

    private String type;
    private String message;

     private String name;

     @Email private String email;

     @Size(min = 10, max = 15)
     private String mobile;

     private String city;

    private Long categoryId;
    private Long serviceId;

    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private String utmTerm;
    private String utmContent;
}