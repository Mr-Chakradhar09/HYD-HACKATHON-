package com.inventory.inventory.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.inventory.dto.InventoryRequest;
import com.inventory.inventory.entity.IdempotentEvent;
import com.inventory.inventory.repository.IdempotentEventRepository;
import com.inventory.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventorySagaListenerTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @Mock
    private IdempotentEventRepository idempotentEventRepository;

    private ObjectMapper objectMapper;

    @InjectMocks
    private InventorySagaListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new InventorySagaListener(inventoryService, eventPublisher, objectMapper, idempotentEventRepository);
    }

    @Test
    void testHandlePurchaseRequestCreated_Success() {
        String message = "{\"id\":100,\"productId\":1,\"warehouseId\":2,\"requiredQuantity\":50}";

        when(idempotentEventRepository.existsById("purchase-request-100")).thenReturn(false);

        listener.handlePurchaseRequestCreated(message);

        ArgumentCaptor<InventoryRequest> requestCaptor = ArgumentCaptor.forClass(InventoryRequest.class);
        verify(inventoryService).inbound(requestCaptor.capture());
        
        InventoryRequest capturedRequest = requestCaptor.getValue();
        assertEquals(1L, capturedRequest.getProductId());
        assertEquals(2L, capturedRequest.getWarehouseId());
        assertEquals(50, capturedRequest.getQuantity());
        assertEquals("REPLENISHMENT:100", capturedRequest.getReference());

        verify(idempotentEventRepository).save(any(IdempotentEvent.class));
        verify(eventPublisher).publishInventoryReplenished(100L);
    }

    @Test
    void testHandlePurchaseRequestCreated_IdempotentSkip() {
        String message = "{\"id\":100,\"productId\":1,\"warehouseId\":2,\"requiredQuantity\":50}";

        when(idempotentEventRepository.existsById("purchase-request-100")).thenReturn(true);

        listener.handlePurchaseRequestCreated(message);

        verify(inventoryService, never()).inbound(any());
        verify(eventPublisher, never()).publishInventoryReplenished(anyLong());
    }

    @Test
    void testHandlePurchaseRequestCreated_InvalidQuantityFails() {
        String message = "{\"id\":100,\"productId\":1,\"warehouseId\":2,\"requiredQuantity\":-10}";

        when(idempotentEventRepository.existsById("purchase-request-100")).thenReturn(false);

        listener.handlePurchaseRequestCreated(message);

        verify(inventoryService, never()).inbound(any());
        verify(eventPublisher).publishInventoryReplenishmentFailed(eq(100L), anyString());
    }
}
