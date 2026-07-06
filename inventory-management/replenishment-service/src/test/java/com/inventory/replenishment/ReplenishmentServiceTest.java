package com.inventory.replenishment;

import com.inventory.replenishment.dto.PurchaseRequestRequest;
import com.inventory.replenishment.dto.PurchaseRequestResponse;
import com.inventory.replenishment.entity.PurchaseRequest;
import com.inventory.replenishment.enums.PurchaseRequestStatus;
import com.inventory.replenishment.kafka.PurchaseRequestEventPublisher;
import com.inventory.replenishment.repository.PurchaseRequestRepository;
import com.inventory.replenishment.service.ReplenishmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplenishmentServiceTest {

    @Mock
    private PurchaseRequestRepository purchaseRequestRepository;
    @Mock
    private PurchaseRequestEventPublisher eventPublisher;

    @InjectMocks
    private ReplenishmentService replenishmentService;

    @Test
    void createPurchaseRequest_ShouldSucceed() {
        PurchaseRequestRequest req = new PurchaseRequestRequest();
        req.setProductId(1L);
        req.setWarehouseId(1L);
        req.setRequiredQuantity(50);

        when(purchaseRequestRepository.save(any(PurchaseRequest.class))).thenAnswer(i -> {
            PurchaseRequest pr = i.getArgument(0);
            pr.setId(1L);
            return pr;
        });

        PurchaseRequestResponse res = replenishmentService.createPurchaseRequest(req);
        assertEquals(1L, res.getProductId());
        assertEquals(1L, res.getWarehouseId());
        assertEquals(50, res.getRequiredQuantity());
        assertEquals(PurchaseRequestStatus.PENDING, res.getStatus());
        verify(eventPublisher).publishPurchaseRequestCreated(any(PurchaseRequest.class));
    }

    @Test
    void getAllPurchaseRequests_ShouldReturnList() {
        PurchaseRequest pr1 = new PurchaseRequest(); pr1.setId(1L); pr1.setProductId(1L);
        PurchaseRequest pr2 = new PurchaseRequest(); pr2.setId(2L); pr2.setProductId(2L);

        when(purchaseRequestRepository.findAll()).thenReturn(List.of(pr1, pr2));

        assertEquals(2, replenishmentService.getAllPurchaseRequests().size());
    }

    @Test
    void getPurchaseRequestsByWarehouse_ShouldFilter() {
        PurchaseRequest pr = new PurchaseRequest(); pr.setId(1L); pr.setWarehouseId(1L);

        when(purchaseRequestRepository.findByWarehouseId(1L)).thenReturn(List.of(pr));

        List<PurchaseRequestResponse> results = replenishmentService.getPurchaseRequestsByWarehouse(1L);
        assertEquals(1, results.size());
    }

    @Test
    void approvePurchaseRequest_ShouldSetStatusApproved() {
        PurchaseRequest pr = new PurchaseRequest();
        pr.setId(1L);
        pr.setStatus(PurchaseRequestStatus.PENDING);

        when(purchaseRequestRepository.findById(1L)).thenReturn(Optional.of(pr));
        when(purchaseRequestRepository.save(any(PurchaseRequest.class))).thenAnswer(i -> i.getArgument(0));

        PurchaseRequestResponse res = replenishmentService.approvePurchaseRequest(1L);
        assertEquals(PurchaseRequestStatus.APPROVED, res.getStatus());
    }

    @Test
    void approvePurchaseRequest_NotFound_ShouldThrow() {
        when(purchaseRequestRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> replenishmentService.approvePurchaseRequest(99L));
    }
}
