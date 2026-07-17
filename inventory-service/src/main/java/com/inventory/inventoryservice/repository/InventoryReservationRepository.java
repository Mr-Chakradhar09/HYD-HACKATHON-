package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.entity.InventoryReservation;
import com.inventory.inventoryservice.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    Optional<InventoryReservation> findByReferenceNumberAndReferenceTypeAndStatus(
            String referenceNumber, String referenceType, ReservationStatus status);

    List<InventoryReservation> findByInventoryIdAndStatus(Long inventoryId, ReservationStatus status);

    boolean existsByReferenceNumberAndReferenceTypeAndStatus(
            String referenceNumber, String referenceType, ReservationStatus status);
}
