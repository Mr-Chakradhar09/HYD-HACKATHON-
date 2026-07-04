package com.inventory.inventory;

import com.inventory.inventory.dto.InventoryRequest;
import com.inventory.inventory.dto.InventoryResponse;
import com.inventory.inventory.dto.ReservationRequest;
import com.inventory.inventory.dto.ReservationResponse;
import com.inventory.inventory.dto.TransferRequest;
import com.inventory.inventory.entity.Inventory;
import com.inventory.inventory.entity.InventoryTransaction;
import com.inventory.inventory.entity.Reservation;
import com.inventory.inventory.enums.ReservationStatus;
import com.inventory.inventory.kafka.InventoryEventPublisher;
import com.inventory.inventory.repository.InventoryRepository;
import com.inventory.inventory.repository.InventoryTransactionRepository;
import com.inventory.inventory.repository.ReservationRepository;
import com.inventory.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryTransactionRepository transactionRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private InventoryEventPublisher eventPublisher;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(inventoryRepository, transactionRepository, reservationRepository, eventPublisher);
    }

    // ---- Existing Inventory Tests ----

    @Test
    void inbound_NewProduct_ShouldCreateInventory() {
        InventoryRequest req = new InventoryRequest();
        req.setProductId(1L);
        req.setWarehouseId(1L);
        req.setQuantity(100);
        req.setReference("PO-001");

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> {
            Inventory inv = i.getArgument(0);
            inv.setId(1L);
            return inv;
        });

        InventoryResponse res = inventoryService.inbound(req);
        assertEquals(100, res.getQuantity());
        verify(transactionRepository).save(any(InventoryTransaction.class));
        verify(eventPublisher).publishInventoryUpdated(any(InventoryTransaction.class));
    }

    @Test
    void inbound_ExistingProduct_ShouldIncreaseQuantity() {
        Inventory existing = new Inventory();
        existing.setId(1L);
        existing.setProductId(1L);
        existing.setWarehouseId(1L);
        existing.setQuantity(50);

        InventoryRequest req = new InventoryRequest();
        req.setProductId(1L);
        req.setWarehouseId(1L);
        req.setQuantity(30);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        InventoryResponse res = inventoryService.inbound(req);
        assertEquals(80, res.getQuantity());
    }

    @Test
    void outbound_ShouldDecreaseQuantity() {
        Inventory existing = new Inventory();
        existing.setId(1L);
        existing.setProductId(1L);
        existing.setWarehouseId(1L);
        existing.setQuantity(100);
        existing.setReserved(0);

        InventoryRequest req = new InventoryRequest();
        req.setProductId(1L);
        req.setWarehouseId(1L);
        req.setQuantity(30);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        InventoryResponse res = inventoryService.outbound(req);
        assertEquals(70, res.getQuantity());
    }

    @Test
    void outbound_InsufficientStock_ShouldThrow() {
        Inventory existing = new Inventory();
        existing.setProductId(1L);
        existing.setWarehouseId(1L);
        existing.setQuantity(10);
        existing.setReserved(5);

        InventoryRequest req = new InventoryRequest();
        req.setProductId(1L);
        req.setWarehouseId(1L);
        req.setQuantity(100);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class, () -> inventoryService.outbound(req));
    }

    @Test
    void outbound_ConsidersReservedStock_ShouldThrowWhenOnlyReservedStockLeft() {
        Inventory existing = new Inventory();
        existing.setProductId(1L);
        existing.setWarehouseId(1L);
        existing.setQuantity(100);
        existing.setReserved(95);

        InventoryRequest req = new InventoryRequest();
        req.setProductId(1L);
        req.setWarehouseId(1L);
        req.setQuantity(10);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class, () -> inventoryService.outbound(req));
    }

    @Test
    void outbound_NoInventory_ShouldThrow() {
        InventoryRequest req = new InventoryRequest();
        req.setProductId(99L);
        req.setWarehouseId(1L);
        req.setQuantity(10);

        when(inventoryRepository.findByProductIdAndWarehouseId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.outbound(req));
    }

    @Test
    void transfer_ShouldMoveStock() {
        Inventory source = new Inventory();
        source.setId(1L);
        source.setProductId(1L);
        source.setWarehouseId(1L);
        source.setQuantity(100);
        source.setReserved(0);

        Inventory dest = new Inventory();
        dest.setId(2L);
        dest.setProductId(1L);
        dest.setWarehouseId(2L);
        dest.setQuantity(0);

        TransferRequest req = new TransferRequest();
        req.setProductId(1L);
        req.setFromWarehouseId(1L);
        req.setToWarehouseId(2L);
        req.setQuantity(30);
        req.setReference("TR-001");

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(source));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 2L)).thenReturn(Optional.of(dest));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.transfer(req);
        assertEquals(70, source.getQuantity());
        assertEquals(30, dest.getQuantity());
        verify(eventPublisher).publishStockTransferred(1L, 1L, 2L, 30);
    }

    @Test
    void adjustment_ShouldSetExactQuantity() {
        Inventory existing = new Inventory();
        existing.setId(1L);
        existing.setProductId(1L);
        existing.setWarehouseId(1L);
        existing.setQuantity(50);

        InventoryRequest req = new InventoryRequest();
        req.setProductId(1L);
        req.setWarehouseId(1L);
        req.setQuantity(200);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        InventoryResponse res = inventoryService.adjustment(req);
        assertEquals(200, res.getQuantity());
    }

    @Test
    void getAllInventory_ShouldReturnAll() {
        Inventory i1 = new Inventory(); i1.setId(1L); i1.setQuantity(10);
        Inventory i2 = new Inventory(); i2.setId(2L); i2.setQuantity(20);

        when(inventoryRepository.findAll()).thenReturn(List.of(i1, i2));

        assertEquals(2, inventoryService.getAllInventory().size());
    }

    @Test
    void getInventoryByWarehouse_ShouldFilter() {
        Inventory i1 = new Inventory(); i1.setId(1L); i1.setWarehouseId(1L); i1.setQuantity(10);

        when(inventoryRepository.findByWarehouseId(1L)).thenReturn(List.of(i1));

        List<InventoryResponse> results = inventoryService.getInventoryByWarehouse(1L);
        assertEquals(1, results.size());
        assertEquals(10, results.get(0).getQuantity());
    }

    // ---- Reservation Tests ----

    @Test
    void reserve_ShouldReserveStock() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setProductId(1L);
        inventory.setWarehouseId(1L);
        inventory.setQuantity(100);
        inventory.setReserved(0);

        ReservationRequest req = new ReservationRequest();
        req.setOrderId("ORD-001");
        req.setProductId(1L);
        req.setWarehouseId(1L);
        req.setQuantity(20);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> {
            Reservation r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReservationResponse res = inventoryService.reserve(req);

        assertNotNull(res);
        assertEquals("ORD-001", res.getOrderId());
        assertEquals(20, res.getQuantity());
        assertEquals(ReservationStatus.RESERVED, res.getStatus());
        assertEquals(20, inventory.getReserved());
        verify(transactionRepository).save(any(InventoryTransaction.class));
        verify(eventPublisher).publishInventoryUpdated(any(InventoryTransaction.class));
    }

    @Test
    void reserve_InsufficientAvailableStock_ShouldThrow() {
        Inventory inventory = new Inventory();
        inventory.setProductId(1L);
        inventory.setWarehouseId(1L);
        inventory.setQuantity(100);
        inventory.setReserved(95);

        ReservationRequest req = new ReservationRequest();
        req.setOrderId("ORD-001");
        req.setProductId(1L);
        req.setWarehouseId(1L);
        req.setQuantity(20);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));

        assertThrows(RuntimeException.class, () -> inventoryService.reserve(req));
    }

    @Test
    void reserve_NoInventory_ShouldThrow() {
        ReservationRequest req = new ReservationRequest();
        req.setOrderId("ORD-001");
        req.setProductId(99L);
        req.setWarehouseId(1L);
        req.setQuantity(10);

        when(inventoryRepository.findByProductIdAndWarehouseId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.reserve(req));
    }

    @Test
    void release_ShouldReleaseReservedStock() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setProductId(1L);
        inventory.setWarehouseId(1L);
        inventory.setQuantity(100);
        inventory.setReserved(20);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setOrderId("ORD-001");
        reservation.setProductId(1L);
        reservation.setWarehouseId(1L);
        reservation.setQuantity(20);
        reservation.setStatus(ReservationStatus.RESERVED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.release(1L);

        assertEquals(0, inventory.getReserved());
        assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
        verify(transactionRepository).save(any(InventoryTransaction.class));
    }

    @Test
    void release_AlreadyReleased_ShouldThrow() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.RELEASED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(RuntimeException.class, () -> inventoryService.release(1L));
    }

    @Test
    void release_NotFound_ShouldThrow() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> inventoryService.release(99L));
    }

    @Test
    void ship_ShouldReduceQuantityAndReserved() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setProductId(1L);
        inventory.setWarehouseId(1L);
        inventory.setQuantity(100);
        inventory.setReserved(20);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setOrderId("ORD-001");
        reservation.setProductId(1L);
        reservation.setWarehouseId(1L);
        reservation.setQuantity(20);
        reservation.setStatus(ReservationStatus.RESERVED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.ship(1L);

        assertEquals(80, inventory.getQuantity());
        assertEquals(0, inventory.getReserved());
        assertEquals(ReservationStatus.SHIPPED, reservation.getStatus());
        verify(transactionRepository).save(any(InventoryTransaction.class));
    }

    @Test
    void ship_NotReservedStatus_ShouldThrow() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.PICKING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(RuntimeException.class, () -> inventoryService.ship(1L));
    }

    @Test
    void ship_NotFound_ShouldThrow() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> inventoryService.ship(99L));
    }

    @Test
    void releaseByOrderId_ShouldReleaseAll() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setProductId(1L);
        inventory.setWarehouseId(1L);
        inventory.setQuantity(100);
        inventory.setReserved(30);

        Reservation r1 = new Reservation();
        r1.setId(1L);
        r1.setOrderId("ORD-001");
        r1.setProductId(1L);
        r1.setWarehouseId(1L);
        r1.setQuantity(20);
        r1.setStatus(ReservationStatus.RESERVED);

        Reservation r2 = new Reservation();
        r2.setId(2L);
        r2.setOrderId("ORD-001");
        r2.setProductId(1L);
        r2.setWarehouseId(1L);
        r2.setQuantity(10);
        r2.setStatus(ReservationStatus.RESERVED);

        when(reservationRepository.findByOrderId("ORD-001")).thenReturn(List.of(r1, r2));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r1));
        when(reservationRepository.findById(2L)).thenReturn(Optional.of(r2));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.releaseByOrderId("ORD-001");

        assertEquals(0, inventory.getReserved());
        assertEquals(ReservationStatus.RELEASED, r1.getStatus());
        assertEquals(ReservationStatus.RELEASED, r2.getStatus());
    }

    @Test
    void reserve_ResponseHasCorrectFields() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setProductId(1L);
        inventory.setWarehouseId(1L);
        inventory.setQuantity(100);
        inventory.setReserved(0);

        ReservationRequest req = new ReservationRequest();
        req.setOrderId("ORD-001");
        req.setProductId(1L);
        req.setWarehouseId(1L);
        req.setQuantity(20);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> {
            Reservation r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReservationResponse res = inventoryService.reserve(req);
        assertEquals(20, res.getQuantity());
        assertEquals(1L, res.getProductId());
        assertEquals(1L, res.getWarehouseId());
        assertEquals("ORD-001", res.getOrderId());
        assertEquals(ReservationStatus.RESERVED, res.getStatus());
    }
}
