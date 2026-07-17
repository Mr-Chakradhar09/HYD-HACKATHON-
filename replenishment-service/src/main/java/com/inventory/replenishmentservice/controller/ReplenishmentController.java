package com.inventory.replenishmentservice.controller;

import com.inventory.replenishmentservice.dto.request.CreateReplenishmentRequest;
import com.inventory.replenishmentservice.dto.response.ReplenishmentResponse;
import com.inventory.replenishmentservice.enums.ReplenishmentStatus;
import com.inventory.replenishmentservice.service.interfaces.ReplenishmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/replenishment")
public class ReplenishmentController {

    private final ReplenishmentService service;

    public ReplenishmentController(ReplenishmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReplenishmentResponse> createRequest(@Valid @RequestBody CreateReplenishmentRequest request,
                                                               Authentication authentication) {
        return ResponseEntity.ok(service.createRequest(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<Page<ReplenishmentResponse>> getAllRequests(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.getAllRequests(pageable));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReplenishmentResponse> updateStatus(@PathVariable Long id,
                                                              @RequestParam ReplenishmentStatus status,
                                                              Authentication authentication) {
        return ResponseEntity.ok(service.updateStatus(id, status, authentication.getName()));
    }
}
