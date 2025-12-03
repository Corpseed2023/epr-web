// src/main/java/com/epr/controller/ServiceController.java
package com.epr.controller.admin;

import com.epr.dto.admin.service.ServiceRequestDto;
import com.epr.dto.admin.service.ServiceResponseDto;
import com.epr.error.ApiResponse;
import com.epr.service.ServiceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private static final Logger log = LoggerFactory.getLogger(ServiceController.class);

    @Autowired private ServiceService serviceService;

    @GetMapping
    public ResponseEntity<List<ServiceResponseDto>> getAllActiveServices() {
        List<ServiceResponseDto> services = serviceService.findAllActiveServices();
        return services.isEmpty()
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDto> getServiceById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(serviceService.findById(id));
        } catch (IllegalArgumentException e) {
            log.warn("Service not found: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }



    @PostMapping
    public ResponseEntity<?> createService(
            @Valid @RequestBody ServiceRequestDto dto,
            @RequestParam Long userId) {

        log.info("Creating service by userId={}", userId);
        try {
            ServiceResponseDto saved = serviceService.createService(dto, userId);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create service", 500));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateService(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @RequestBody ServiceRequestDto dto) {

        log.info("Updating service ID={} by userId={}", id, userId);
        try {
            ServiceResponseDto updated = serviceService.updateService(id, dto, userId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            boolean notFound = e.getMessage().toLowerCase().contains("not found");
            return ResponseEntity.status(notFound ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), notFound ? 404 : 400));
        } catch (Exception e) {
            log.error("Error updating service", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to update service", 500));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Long id, @RequestParam Long userId) {
        log.info("Soft deleting service ID={} by userId={}", id, userId);
        try {
            serviceService.softDeleteService(id, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            boolean notFound = e.getMessage().toLowerCase().contains("not found");
            return ResponseEntity.status(notFound ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), notFound ? 404 : 400));
        } catch (Exception e) {
            log.error("Error deleting service", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to delete service", 500));
        }
    }



    // Add to your existing ServiceController or create a new PublicServiceController

    @GetMapping("/subcategory/{subcategoryId}/services")
    public ResponseEntity<List<ServiceResponseDto>> getActiveServicesBySubcategory(
            @PathVariable Long subcategoryId) {

        if (subcategoryId == null || subcategoryId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        List<ServiceResponseDto> services = serviceService.findActivePublicServicesBySubcategoryId(subcategoryId);

        return services.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(services);
    }

}