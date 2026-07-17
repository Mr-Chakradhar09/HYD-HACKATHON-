package com.inventory.inventoryservice.kafka;

import com.inventory.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MovementEventConsumerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private MovementEventConsumer movementEventConsumer;

    @Test
    void testConsumeMovementEventDelegatesToInventoryService() {
        // Arrange
        MovementEvent event = new MovementEvent();
        event.setEventId("test-event-123");
        event.setEventType("StockIssued");
        event.setProductId(1L);

        // Act
        movementEventConsumer.consumeMovementEvent(event);

        // Assert
        verify(inventoryService, times(1)).updateInventoryFromMovementEvent(event);
    }
}
