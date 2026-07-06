package com.inventory.inventory;

import com.inventory.inventory.controller.InventoryController;
import com.inventory.inventory.dto.InventoryRequest;
import com.inventory.inventory.dto.InventoryResponse;
import com.inventory.inventory.dto.ReservationRequest;
import com.inventory.inventory.dto.ReservationResponse;
import com.inventory.inventory.dto.TransferRequest;
import com.inventory.inventory.enums.ReservationStatus;
import com.inventory.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    @Test
    void inbound_ShouldSucceed() {
        when(inventoryService.inbound(any())).thenReturn(new InventoryResponse());
        assertTrue(inventoryController.inbound(new InventoryRequest()).getStatusCode().is2xxSuccessful());
    }

    @Test
    void outbound_ShouldSucceed() {
        when(inventoryService.outbound(any())).thenReturn(new InventoryResponse());
        assertTrue(inventoryController.outbound(new InventoryRequest()).getStatusCode().is2xxSuccessful());
    }

    @Test
    void transfer_ShouldSucceed() {
        doNothing().when(inventoryService).transfer(any());
        assertTrue(inventoryController.transfer(new TransferRequest()).getStatusCode().is2xxSuccessful());
    }

    @Test
    void adjustment_ShouldSucceed() {
        when(inventoryService.adjustment(any())).thenReturn(new InventoryResponse());
        assertTrue(inventoryController.adjustment(new InventoryRequest()).getStatusCode().is2xxSuccessful());
    }

    @Test
    void getAllInventory_ShouldReturnAll() {
        when(inventoryService.getAllInventory()).thenReturn(List.of(new InventoryResponse()));
        assertEquals(1, inventoryController.getAllInventory(null).getBody().size());
    }

    @Test
    void getAllInventory_WithWarehouseFilter_ShouldFilter() {
        when(inventoryService.getInventoryByWarehouse(1L)).thenReturn(List.of(new InventoryResponse()));
        assertEquals(1, inventoryController.getAllInventory(1L).getBody().size());
        verify(inventoryService).getInventoryByWarehouse(1L);
    }

    @Test
    void reserve_ShouldSucceed() {
        when(inventoryService.reserve(any())).thenReturn(new ReservationResponse());
        assertTrue(inventoryController.reserve(new ReservationRequest()).getStatusCode().is2xxSuccessful());
    }

    @Test
    void release_ShouldSucceed() {
        doNothing().when(inventoryService).release(1L);
        assertTrue(inventoryController.release(1L).getStatusCode().is2xxSuccessful());
        verify(inventoryService).release(1L);
    }

    @Test
    void releaseByOrderId_ShouldSucceed() {
        doNothing().when(inventoryService).releaseByOrderId("ORD-001");
        assertTrue(inventoryController.releaseByOrderId("ORD-001").getStatusCode().is2xxSuccessful());
        verify(inventoryService).releaseByOrderId("ORD-001");
    }

    @Test
    void ship_ShouldSucceed() {
        doNothing().when(inventoryService).ship(1L);
        assertTrue(inventoryController.ship(1L).getStatusCode().is2xxSuccessful());
        verify(inventoryService).ship(1L);
    }

    @Test
    void getAllReservations_ShouldReturnList() {
        when(inventoryService.getAllReservations()).thenReturn(List.of(new ReservationResponse()));
        assertEquals(1, inventoryController.getAllReservations().getBody().size());
    }

    @Test
    void getReservation_ShouldReturnById() {
        ReservationResponse resp = new ReservationResponse();
        when(inventoryService.getReservation(1L)).thenReturn(resp);
        assertNotNull(inventoryController.getReservation(1L).getBody());
    }

    @Test
    void getReservationsByOrder_ShouldReturnList() {
        when(inventoryService.getReservationsByOrder("ORD-001")).thenReturn(List.of(new ReservationResponse()));
        assertEquals(1, inventoryController.getReservationsByOrder("ORD-001").getBody().size());
    }
}
