package com.inventory.replenishment.service;

import com.inventory.replenishment.dto.PurchaseRequestRequest;
import com.inventory.replenishment.dto.PurchaseRequestResponse;
import com.inventory.replenishment.entity.PurchaseRequest;
import com.inventory.replenishment.enums.PurchaseRequestStatus;
import com.inventory.replenishment.kafka.PurchaseRequestEventPublisher;
import com.inventory.replenishment.repository.PurchaseRequestRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReplenishmentService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PurchaseRequestEventPublisher eventPublisher;

    public ReplenishmentService(PurchaseRequestRepository purchaseRequestRepository,
                                PurchaseRequestEventPublisher eventPublisher) {
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.eventPublisher = eventPublisher;
    }

    public PurchaseRequestResponse createPurchaseRequest(PurchaseRequestRequest request) {
        PurchaseRequest pr = new PurchaseRequest();
        pr.setProductId(request.getProductId());
        pr.setWarehouseId(request.getWarehouseId());
        pr.setRequiredQuantity(request.getRequiredQuantity());
        pr = purchaseRequestRepository.save(pr);

        eventPublisher.publishPurchaseRequestCreated(pr);

        return PurchaseRequestResponse.fromEntity(pr);
    }

    public List<PurchaseRequestResponse> getAllPurchaseRequests() {
        return purchaseRequestRepository.findAll().stream().map(PurchaseRequestResponse::fromEntity).toList();
    }

    public List<PurchaseRequestResponse> getPurchaseRequestsByWarehouse(Long warehouseId) {
        return purchaseRequestRepository.findByWarehouseId(warehouseId).stream().map(PurchaseRequestResponse::fromEntity).toList();
    }

    public PurchaseRequestResponse approvePurchaseRequest(Long id) {
        PurchaseRequest pr = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase request not found"));
        pr.setStatus(PurchaseRequestStatus.APPROVED);
        pr = purchaseRequestRepository.save(pr);
        return PurchaseRequestResponse.fromEntity(pr);
    }
}
