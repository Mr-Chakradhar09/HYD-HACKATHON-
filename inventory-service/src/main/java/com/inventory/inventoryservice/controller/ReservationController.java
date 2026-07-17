package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.dto.request.CreateReservationRequest;
import com.inventory.inventoryservice.dto.response.ApiResponse;
import com.inventory.inventoryservice.entity.InventoryReservation;
import com.inventory.inventoryservice.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) { this.reservationService = reservationService; }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_OPERATOR')")
    public ResponseEntity<ApiResponse<InventoryReservation>> createReservation(
            @Valid @RequestBody CreateReservationRequest request, Authentication authentication) {
        InventoryReservation reservation = reservationService.createReservation(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Reservation created", reservation, HttpStatus.CREATED.value()));
    }

    @PatchMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_OPERATOR')")
    public ResponseEntity<ApiResponse<Void>> releaseReservation(@PathVariable Long id, Authentication authentication) {
        reservationService.releaseReservation(id, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Reservation released", null, HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_OPERATOR')")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reservation cancelled", null, HttpStatus.OK.value()));
    }
}
