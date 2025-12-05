package com.epr.service;

import com.epr.dto.admin.client.ClientRequestDto;
import com.epr.dto.admin.client.ClientResponseDto;

import java.util.List;

// Service Interface (optional but recommended)
public interface ClientService {
    List<ClientResponseDto> findAllActiveAndVisible();
    List<ClientResponseDto> findAllActive();           // for admin
    ClientResponseDto findById(Long id);
    ClientResponseDto create(ClientRequestDto dto, Long userId);
    ClientResponseDto update(Long id, ClientRequestDto dto, Long userId);
    void softDelete(Long id, Long userId);
    List<ClientResponseDto> search(String keyword);
}