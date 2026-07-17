package com.inventory.inventory.service;

import com.inventory.inventory.dto.InventoryRequest;
import com.inventory.inventory.dto.InventoryResponse;
import com.inventory.inventory.dto.ReservationRequest;
import com.inventory.inventory.dto.ReservationResponse;
import com.inventory.inventory.dto.TransferRequest;
import com.inventory.inventory.entity.Inventory;
import com.inventory.inventory.entity.InventoryTransaction;
import com.inventory.inventory.entity.Reservation;
import com.inventory.inventory.enums.ReservationStatus;
import com.inventory.inventory.enums.TransactionType;
import com.inventory.inventory.kafka.InventoryEventPublisher;
import com.inventory.inventory.repository.InventoryRepository;
import com.inventory.inventory.repository.InventoryTransactionRepository;
import com.inventory.inventory.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final ReservationRepository reservationRepository;
    private final InventoryEventPublisher eventPublisher;

    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryTransactionRepository transactionRepository,
                            ReservationRepository reservationRepository,
                            InventoryEventPublisher eventPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.reservationRepository = reservationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public InventoryResponse inbound(InventoryRequest request) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(request.getProductId(), request.getWarehouseId())
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setProductId(request.getProductId());
                    inv.setWarehouseId(request.getWarehouseId());
                    inv.setQuantity(0);
                    return inv;
                });

        inventory.setQuantity(inventory.getQuantity() + request.getQuantity());
        inventory = inventoryRepository.save(inventory);

        InventoryTransaction txn = new InventoryTransaction();
        txn.setProductId(request.getProductId());
        txn.setWarehouseId(request.getWarehouseId());
        txn.setType(TransactionType.INBOUND);
        txn.setQuantity(request.getQuantity());
        txn.setReference(request.getReference());
        transactionRepository.save(txn);

        eventPublisher.publishInventoryUpdated(txn);

        return InventoryResponse.fromEntity(inventory);
    }

    @Transactional
    public InventoryResponse outbound(InventoryRequest request) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(request.getProductId(), request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        if (inventory.getAvailable() < request.getQuantity()) {
            throw new RuntimeException("Insufficient available stock (qty: " + inventory.getQuantity()
                    + ", reserved: " + inventory.getReserved() + ")");
        }

        inventory.setQuantity(inventory.getQuantity() - request.getQuantity());
        inventory = inventoryRepository.save(inventory);

        InventoryTransaction txn = new InventoryTransaction();
        txn.setProductId(request.getProductId());
        txn.setWarehouseId(request.getWarehouseId());
        txn.setType(TransactionType.OUTBOUND);
        txn.setQuantity(request.getQuantity());
        txn.setReference(request.getReference());
        transactionRepository.save(txn);

        eventPublisher.publishInventoryUpdated(txn);

        return InventoryResponse.fromEntity(inventory);
    }

    @Transactional
    public void transfer(TransferRequest request) {
        InventoryRequest outboundReq = new InventoryRequest();
        outboundReq.setProductId(request.getProductId());
        outboundReq.setWarehouseId(request.getFromWarehouseId());
        outboundReq.setQuantity(request.getQuantity());
        outboundReq.setReference("TRANSFER-OUT: " + request.getReference());
        outbound(outboundReq);

        InventoryRequest inboundReq = new InventoryRequest();
        inboundReq.setProductId(request.getProductId());
        inboundReq.setWarehouseId(request.getToWarehouseId());
        inboundReq.setQuantity(request.getQuantity());
        inboundReq.setReference("TRANSFER-IN: " + request.getReference());
        inbound(inboundReq);

        eventPublisher.publishStockTransferred(request.getProductId(), request.getFromWarehouseId(),
                request.getToWarehouseId(), request.getQuantity());
    }

    @Transactional
    public InventoryResponse adjustment(InventoryRequest request) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(request.getProductId(), request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setQuantity(request.getQuantity());
        inventory = inventoryRepository.save(inventory);

        InventoryTransaction txn = new InventoryTransaction();
        txn.setProductId(request.getProductId());
        txn.setWarehouseId(request.getWarehouseId());
        txn.setType(TransactionType.ADJUSTMENT);
        txn.setQuantity(request.getQuantity());
        txn.setReference(request.getReference());
        transactionRepository.save(txn);

        eventPublisher.publishInventoryUpdated(txn);

        return InventoryResponse.fromEntity(inventory);
    }

    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream().map(InventoryResponse::fromEntity).toList();
    }

    public List<InventoryResponse> getInventoryByProduct(Long productId) {
        return inventoryRepository.findByProductId(productId).stream().map(InventoryResponse::fromEntity).toList();
    }

    public List<InventoryResponse> getInventoryByWarehouse(Long warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId).stream().map(InventoryResponse::fromEntity).toList();
    }

    public List<InventoryTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Inventory> getLowStockItems(Integer threshold) {
        return inventoryRepository.findByQuantityLessThanEqual(threshold);
    }

    @Transactional
    public ReservationResponse reserve(ReservationRequest request) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(request.getProductId(), request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        if (inventory.getAvailable() < request.getQuantity()) {
            throw new RuntimeException("Insufficient available stock to reserve (qty: "
                    + inventory.getQuantity() + ", reserved: " + inventory.getReserved()
                    + ", requested: " + request.getQuantity() + ")");
        }

        inventory.setReserved(inventory.getReserved() + request.getQuantity());
        inventoryRepository.save(inventory);

        Reservation reservation = new Reservation();
        reservation.setOrderId(request.getOrderId());
        reservation.setProductId(request.getProductId());
        reservation.setWarehouseId(request.getWarehouseId());
        reservation.setQuantity(request.getQuantity());
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation = reservationRepository.save(reservation);

        InventoryTransaction txn = new InventoryTransaction();
        txn.setProductId(request.getProductId());
        txn.setWarehouseId(request.getWarehouseId());
        txn.setType(TransactionType.RESERVE);
        txn.setQuantity(request.getQuantity());
        txn.setReference("RESERVE:" + request.getOrderId());
        transactionRepository.save(txn);

        eventPublisher.publishInventoryUpdated(txn);

        return ReservationResponse.fromEntity(reservation);
    }

    @Transactional
    public void release(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        if (reservation.getStatus() == ReservationStatus.RELEASED
                || reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new RuntimeException("Reservation already " + reservation.getStatus());
        }

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(reservation.getProductId(), reservation.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setReserved(inventory.getReserved() - reservation.getQuantity());
        inventoryRepository.save(inventory);

        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(reservation);

        InventoryTransaction txn = new InventoryTransaction();
        txn.setProductId(reservation.getProductId());
        txn.setWarehouseId(reservation.getWarehouseId());
        txn.setType(TransactionType.RELEASE);
        txn.setQuantity(reservation.getQuantity());
        txn.setReference("RELEASE:" + reservation.getOrderId());
        transactionRepository.save(txn);

        eventPublisher.publishInventoryUpdated(txn);
    }

    @Transactional
    public void releaseByOrderId(String orderId) {
        List<Reservation> reservations = reservationRepository.findByOrderId(orderId);
        if (reservations.isEmpty()) {
            throw new RuntimeException("No reservations found for order: " + orderId);
        }
        for (Reservation r : reservations) {
            release(r.getId());
        }
    }

    @Transactional
    public void ship(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new RuntimeException("Reservation must be in RESERVED status to ship, current: " + reservation.getStatus());
        }

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(reservation.getProductId(), reservation.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setQuantity(inventory.getQuantity() - reservation.getQuantity());
        inventory.setReserved(inventory.getReserved() - reservation.getQuantity());
        inventoryRepository.save(inventory);

        reservation.setStatus(ReservationStatus.SHIPPED);
        reservationRepository.save(reservation);

        InventoryTransaction txn = new InventoryTransaction();
        txn.setProductId(reservation.getProductId());
        txn.setWarehouseId(reservation.getWarehouseId());
        txn.setType(TransactionType.SHIPPED);
        txn.setQuantity(reservation.getQuantity());
        txn.setReference("SHIP:" + reservation.getOrderId());
        transactionRepository.save(txn);

        eventPublisher.publishInventoryUpdated(txn);
    }

    public ReservationResponse getReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + id));
        return ReservationResponse.fromEntity(reservation);
    }

    public List<ReservationResponse> getReservationsByOrder(String orderId) {
        return reservationRepository.findByOrderId(orderId).stream()
                .map(ReservationResponse::fromEntity).toList();
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::fromEntity).toList();
    }
}
