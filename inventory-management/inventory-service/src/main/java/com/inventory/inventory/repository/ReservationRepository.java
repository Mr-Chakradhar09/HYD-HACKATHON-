package com.inventory.inventory.repository;

import com.inventory.inventory.entity.Reservation;
import com.inventory.inventory.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByOrderId(String orderId);
    List<Reservation> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
    List<Reservation> findByStatus(ReservationStatus status);
}
