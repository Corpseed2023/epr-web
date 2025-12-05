package com.epr.controller.admin;

import com.epr.dto.admin.client.ClientRequestDto;
import com.epr.dto.admin.client.ClientResponseDto;
import com.epr.error.ApiResponse;
import com.epr.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    // Public + Admin visible (only active + displayStatus=1)
    @GetMapping("/public")
    public ResponseEntity<List<ClientResponseDto>> getPublicClients() {
        List<ClientResponseDto> clients = clientService.findAllActiveAndVisible();
        return clients.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(clients);
    }

    // Admin only - all active (even hidden ones)
    @GetMapping
    public ResponseEntity<List<ClientResponseDto>> getAllActive() {
        List<ClientResponseDto> clients = clientService.findAllActive();
        return clients.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(clients);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClientResponseDto>> search(@RequestParam String keyword) {
        List<ClientResponseDto> clients = clientService.search(keyword);
        return clients.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDto> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(clientService.findById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ClientRequestDto dto,
                                    @RequestParam Long userId) {
        try {
            ClientResponseDto saved = clientService.create(dto, userId);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody ClientRequestDto dto,
                                    @RequestParam Long userId) {
        try {
            ClientResponseDto updated = clientService.update(id, dto, userId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            boolean notFound = e.getMessage().toLowerCase().contains("not found");
            return ResponseEntity.status(notFound ? 404 : 400)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @RequestParam Long userId) {
        try {
            clientService.softDelete(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Client not found"));
        }
    }
}