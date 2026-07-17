package com.inventory.replenishment.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.replenishment.entity.PurchaseRequest;
import com.inventory.replenishment.enums.PurchaseRequestStatus;
import com.inventory.replenishment.repository.PurchaseRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReplenishmentSagaListenerTest {

    @Mock
    private PurchaseRequestRepository repository;

    private ObjectMapper objectMapper;

    @InjectMocks
    private ReplenishmentSagaListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new ReplenishmentSagaListener(repository, objectMapper);
    }

    @Test
    void testHandleInventoryReplenished() {
        String message = "{\"purchaseRequestId\":100,\"status\":\"SUCCESS\"}";
        PurchaseRequest pr = new PurchaseRequest();
        pr.setId(100L);
        pr.setStatus(PurchaseRequestStatus.PENDING);

        when(repository.findById(100L)).thenReturn(Optional.of(pr));

        listener.handleInventoryReplenished(message);

        assertEquals(PurchaseRequestStatus.APPROVED, pr.getStatus());
        verify(repository).save(pr);
    }

    @Test
    void testHandleInventoryReplenishmentFailed() {
        String message = "{\"purchaseRequestId\":100,\"status\":\"FAILED\",\"reason\":\"Out of bounds\"}";
        PurchaseRequest pr = new PurchaseRequest();
        pr.setId(100L);
        pr.setStatus(PurchaseRequestStatus.PENDING);

        when(repository.findById(100L)).thenReturn(Optional.of(pr));

        listener.handleInventoryReplenishmentFailed(message);

        assertEquals(PurchaseRequestStatus.REJECTED, pr.getStatus());
        verify(repository).save(pr);
    }
}
