package com.inventory.movementservice.repository;

import com.inventory.movementservice.entity.TransferRequest;
import com.inventory.movementservice.enums.TransferRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long> {

    Optional<TransferRequest> findByRequestNumber(String requestNumber);

    Page<TransferRequest> findByStatus(TransferRequestStatus status, Pageable pageable);

    Page<TransferRequest> findByRequestedBy(String requestedBy, Pageable pageable);

    Page<TransferRequest> findBySourceWarehouseIdOrDestinationWarehouseId(Long sourceWarehouseId, Long destinationWarehouseId, Pageable pageable);

    Page<TransferRequest> findByStatusAndSourceWarehouseIdOrStatusAndDestinationWarehouseId(
            TransferRequestStatus status1, Long sourceId,
            TransferRequestStatus status2, Long destId, Pageable pageable);

    long countByStatus(TransferRequestStatus status);

    Page<TransferRequest> findBySourceWarehouseIdAndStatus(Long warehouseId, TransferRequestStatus status, Pageable pageable);

    Page<TransferRequest> findByDestinationWarehouseIdAndStatus(Long warehouseId, TransferRequestStatus status, Pageable pageable);
}
