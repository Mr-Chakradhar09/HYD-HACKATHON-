package com.inventory.replenishment;

import com.inventory.replenishment.controller.PurchaseRequestController;
import com.inventory.replenishment.dto.PurchaseRequestRequest;
import com.inventory.replenishment.dto.PurchaseRequestResponse;
import com.inventory.replenishment.service.ReplenishmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseRequestControllerTest {

    @Mock
    private ReplenishmentService replenishmentService;

    @InjectMocks
    private PurchaseRequestController purchaseRequestController;

    @Test
    void getAll_ShouldReturnList() {
        when(replenishmentService.getAllPurchaseRequests()).thenReturn(List.of(new PurchaseRequestResponse()));
        assertEquals(1, purchaseRequestController.getAll(null).getBody().size());
    }

    @Test
    void getAll_WithWarehouseFilter_ShouldFilter() {
        when(replenishmentService.getPurchaseRequestsByWarehouse(1L)).thenReturn(List.of(new PurchaseRequestResponse()));
        assertEquals(1, purchaseRequestController.getAll(1L).getBody().size());
        verify(replenishmentService).getPurchaseRequestsByWarehouse(1L);
    }

    @Test
    void create_ShouldReturnCreated() {
        when(replenishmentService.createPurchaseRequest(any())).thenReturn(new PurchaseRequestResponse());
        assertEquals(HttpStatus.CREATED, purchaseRequestController.create(new PurchaseRequestRequest()).getStatusCode());
    }

    @Test
    void approve_ShouldSucceed() {
        when(replenishmentService.approvePurchaseRequest(1L)).thenReturn(new PurchaseRequestResponse());
        assertTrue(purchaseRequestController.approve(1L).getStatusCode().is2xxSuccessful());
    }
}
