package com.inventory.inventoryservice.service.impl;

import com.inventory.inventoryservice.dto.request.CreateReservationRequest;
import com.inventory.inventoryservice.entity.Inventory;
import com.inventory.inventoryservice.entity.InventoryReservation;
import com.inventory.inventoryservice.enums.ReservationStatus;
import com.inventory.inventoryservice.event.InventoryEvent;
import com.inventory.inventoryservice.event.KafkaEventPublisher;
import com.inventory.inventoryservice.exception.DuplicateResourceException;
import com.inventory.inventoryservice.exception.InsufficientStockException;
import com.inventory.inventoryservice.exception.InvalidOperationException;
import com.inventory.inventoryservice.exception.ResourceNotFoundException;
import com.inventory.inventoryservice.repository.InventoryRepository;
import com.inventory.inventoryservice.repository.InventoryReservationRepository;
import com.inventory.inventoryservice.service.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final KafkaEventPublisher eventPublisher;

    public ReservationServiceImpl(InventoryRepository inventoryRepository, InventoryReservationRepository reservationRepository, KafkaEventPublisher eventPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public InventoryReservation createReservation(CreateReservationRequest request, String reservedBy) {
        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        if (reservationRepository.existsByReferenceNumberAndReferenceTypeAndStatus(
                request.getReferenceNumber(), request.getReferenceType(), ReservationStatus.ACTIVE)) {
            throw new DuplicateResourceException("Active reservation already exists for this reference");
        }
        if (inventory.getAvailableQuantity() < request.getReservedQuantity()) {
            throw new InsufficientStockException("Insufficient available stock");
        }
        int prevAvailable = inventory.getAvailableQuantity();
        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.getReservedQuantity());
        inventory.recalculateAvailable();
        inventoryRepository.save(inventory);

        InventoryReservation reservation = new InventoryReservation();
        reservation.setInventory(inventory);
        reservation.setReferenceNumber(request.getReferenceNumber());
        reservation.setReferenceType(request.getReferenceType());
        reservation.setReservedQuantity(request.getReservedQuantity());
        reservation.setReservedBy(reservedBy);
        reservation = reservationRepository.save(reservation);

        InventoryEvent event = new InventoryEvent("StockReserved", inventory.getId(), inventory.getProductId(), inventory.getWarehouseId());
        event.setPreviousQuantity(prevAvailable);
        event.setNewQuantity(inventory.getAvailableQuantity());
        event.setChangeQuantity(-request.getReservedQuantity());
        event.setPerformedBy(reservedBy);
        event.setMovementNumber("RSV-" + reservation.getId());
        eventPublisher.publishInventoryEvent(event);

        return reservation;
    }

    @Override
    @Transactional
    public void releaseReservation(Long reservationId, String releasedBy) {
        InventoryReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new InvalidOperationException("Reservation is not active");
        }
        Inventory inventory = reservation.getInventory();
        int prevAvailable = inventory.getAvailableQuantity();
        inventory.setReservedQuantity(inventory.getReservedQuantity() - reservation.getReservedQuantity());
        inventory.recalculateAvailable();
        inventoryRepository.save(inventory);
        reservation.setStatus(ReservationStatus.RELEASED);
        reservation.setReleasedBy(releasedBy);
        reservation.setReleasedAt(java.time.LocalDateTime.now());
        reservationRepository.save(reservation);

        InventoryEvent event = new InventoryEvent("StockReleased", inventory.getId(), inventory.getProductId(), inventory.getWarehouseId());
        event.setPreviousQuantity(prevAvailable);
        event.setNewQuantity(inventory.getAvailableQuantity());
        event.setChangeQuantity(reservation.getReservedQuantity());
        event.setPerformedBy(releasedBy);
        event.setMovementNumber("RSV-" + reservation.getId());
        eventPublisher.publishInventoryEvent(event);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        InventoryReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new InvalidOperationException("Reservation is not active");
        }
        Inventory inventory = reservation.getInventory();
        int prevAvailable = inventory.getAvailableQuantity();
        inventory.setReservedQuantity(inventory.getReservedQuantity() - reservation.getReservedQuantity());
        inventory.recalculateAvailable();
        inventoryRepository.save(inventory);
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        InventoryEvent event = new InventoryEvent("StockReservationCancelled", inventory.getId(), inventory.getProductId(), inventory.getWarehouseId());
        event.setPreviousQuantity(prevAvailable);
        event.setNewQuantity(inventory.getAvailableQuantity());
        event.setChangeQuantity(reservation.getReservedQuantity());
        event.setPerformedBy("SYSTEM");
        event.setMovementNumber("RSV-" + reservation.getId());
        eventPublisher.publishInventoryEvent(event);
    }
}
