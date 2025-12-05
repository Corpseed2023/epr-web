// src/main/java/com/epr/serviceimpl/ClientServiceImpl.java
package com.epr.serviceimpl;

import com.epr.dto.admin.client.ClientRequestDto;
import com.epr.dto.admin.client.ClientResponseDto;
import com.epr.entity.Client;
import com.epr.entity.User;
import com.epr.repository.ClientRepository;
import com.epr.repository.UserRepository;
import com.epr.service.ClientService;
import com.epr.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final DateTimeUtil dateTimeUtil;

    // Helper: Validate and get active user
    private User validateAndGetActiveUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID is required");
        }
        return userRepository.findActiveUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found or inactive"));
    }

    @Override
    public List<ClientResponseDto> findAllActiveAndVisible() {
        return clientRepository.findAllActiveAndVisible()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClientResponseDto> findAllActive() {
        return clientRepository.findAllActive()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClientResponseDto> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllActive();
        }
        return clientRepository.searchActive(keyword.trim())
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ClientResponseDto findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid client ID");
        }
        Client client = clientRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found with id: " + id));
        return toResponseDto(client);
    }

    @Override
    public ClientResponseDto create(ClientRequestDto dto, Long userId) {
        validateDto(dto, null);

        User currentUser = validateAndGetActiveUser(userId);

        String name = dto.getName().trim();
        String slug = dto.getSlug();

        // Check uniqueness
        if (clientRepository.existsByNameIgnoreCaseAndIdNot(name, null)) {
            throw new IllegalArgumentException("Client with this name already exists");
        }
        if (clientRepository.existsBySlugIgnoreCaseAndIdNot(slug, null)) {
            throw new IllegalArgumentException("Client slug already exists");
        }

        Client client = new Client();
        mapRequestToEntity(dto, client);

        client.setUuid(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        LocalDateTime now = dateTimeUtil.getCurrentUtcTime();
        client.setPostDate(now);
        client.setModifyDate(now);
        client.setAddedByUUID(currentUser.getUuid());
        client.setDeleteStatus(2);

        Client saved = clientRepository.save(client);
        log.info("Client created: {} (ID: {}) by user {}", saved.getName(), saved.getId(), userId);
        return toResponseDto(saved);
    }

    @Override
    public ClientResponseDto update(Long id, ClientRequestDto dto, Long userId) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid client ID");

        validateDto(dto, id);
        User currentUser = validateAndGetActiveUser(userId);

        Client existing = clientRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        String newName = dto.getName().trim();


        // Check uniqueness (excluding current record)
        if (!existing.getName().equalsIgnoreCase(newName) &&
                clientRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {
            throw new IllegalArgumentException("Client with this name already exists");
        }
        if (!existing.getSlug().equalsIgnoreCase(dto.getSlug()) &&
                clientRepository.existsBySlugIgnoreCaseAndIdNot(dto.getSlug(), id)) {
            throw new IllegalArgumentException("Client slug already exists");
        }

        mapRequestToEntity(dto, existing);
        existing.setSlug(dto.getSlug());
        existing.setModifyDate(dateTimeUtil.getCurrentUtcTime());
        existing.setModifyByUUID(currentUser.getUuid()); // optional: add this field to entity if needed

        Client updated = clientRepository.save(existing);
        log.info("Client updated: {} (ID: {}) by user {}", updated.getName(), updated.getId(), userId);
        return toResponseDto(updated);
    }

    @Override
    public void softDelete(Long id, Long userId) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid client ID");

        validateAndGetActiveUser(userId);

        Client client = clientRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        client.setDeleteStatus(1);
        client.setModifyDate(dateTimeUtil.getCurrentUtcTime());
        clientRepository.save(client);

        log.info("Client soft deleted: ID={} by user={}", id, userId);
    }

    // ==================== Helper Methods ====================

    private void validateDto(ClientRequestDto dto, Long existingId) {
        if (dto == null) throw new IllegalArgumentException("Client data is required");
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Client name is required");
        }
    }

    private void mapRequestToEntity(ClientRequestDto dto, Client entity) {
        entity.setName(dto.getName().trim());
        entity.setLogo(dto.getLogo());
        entity.setWebsiteUrl(dto.getWebsiteUrl());
        entity.setDescription(dto.getDescription());

        String displayStatus = dto.getDisplayStatus();
        if (displayStatus == null || (!displayStatus.equals("1") && !displayStatus.equals("2"))) {
            entity.setDisplayStatus("1"); // default visible
        } else {
            entity.setDisplayStatus(displayStatus);
        }
    }

    private ClientResponseDto toResponseDto(Client c) {
        ClientResponseDto dto = new ClientResponseDto();
        dto.setId(c.getId());
        dto.setUuid(c.getUuid());
        dto.setName(c.getName());
        dto.setLogo(c.getLogo());
        dto.setWebsiteUrl(c.getWebsiteUrl());
        dto.setDescription(c.getDescription());
        dto.setSlug(c.getSlug());
        dto.setDisplayStatus(c.getDisplayStatus());
        dto.setPostDate(c.getPostDate());
        dto.setModifyDate(c.getModifyDate());
        dto.setDeleteStatus(c.getDeleteStatus());
        return dto;
    }
}