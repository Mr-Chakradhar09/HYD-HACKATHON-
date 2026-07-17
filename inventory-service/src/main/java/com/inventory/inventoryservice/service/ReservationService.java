package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.dto.request.CreateReservationRequest;
import com.inventory.inventoryservice.dto.response.InventoryResponse;
import com.inventory.inventoryservice.entity.InventoryReservation;

public interface ReservationService {
    InventoryReservation createReservation(CreateReservationRequest request, String reservedBy);
    void releaseReservation(Long reservationId, String releasedBy);
    void cancelReservation(Long reservationId);
}
