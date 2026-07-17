package com.inventory.replenishmentservice.service.impl;

import com.inventory.replenishmentservice.dto.request.CreateReplenishmentRequest;
import com.inventory.replenishmentservice.dto.response.ReplenishmentResponse;
import com.inventory.replenishmentservice.entity.ReplenishmentRequest;
import com.inventory.replenishmentservice.enums.ReplenishmentStatus;
import com.inventory.replenishmentservice.exception.ResourceNotFoundException;
import com.inventory.replenishmentservice.feign.ProductFeignClient;
import com.inventory.replenishmentservice.feign.WarehouseFeignClient;
import com.inventory.replenishmentservice.repository.ReplenishmentRequestRepository;
import com.inventory.replenishmentservice.service.interfaces.ReplenishmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReplenishmentServiceImpl implements ReplenishmentService {

    private final ReplenishmentRequestRepository repository;
    private final ProductFeignClient productFeignClient;
    private final WarehouseFeignClient warehouseFeignClient;

    public ReplenishmentServiceImpl(ReplenishmentRequestRepository repository,
                                    ProductFeignClient productFeignClient,
                                    WarehouseFeignClient warehouseFeignClient) {
        this.repository = repository;
        this.productFeignClient = productFeignClient;
        this.warehouseFeignClient = warehouseFeignClient;
    }

    @Override
    public ReplenishmentResponse createRequest(CreateReplenishmentRequest request, String requestedBy) {
        try {
            productFeignClient.getProductById(request.getProductId(), null);
        } catch (Exception e) {
            // Product validation is best-effort; proceed even if unavailable
        }

        try {
            warehouseFeignClient.getWarehouseById(request.getWarehouseId());
        } catch (Exception e) {
            // Warehouse validation is best-effort; proceed even if unavailable
        }

        ReplenishmentRequest entity = new ReplenishmentRequest();
        entity.setProductId(request.getProductId());
        entity.setWarehouseId(request.getWarehouseId());
        entity.setRequestedQuantity(request.getRequestedQuantity());
        entity.setStatus(ReplenishmentStatus.PENDING);
        entity.setRequestedBy(requestedBy);

        entity = repository.save(entity);
        return mapToResponse(entity);
    }

    @Override
    public Page<ReplenishmentResponse> getAllRequests(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public ReplenishmentResponse updateStatus(Long id, ReplenishmentStatus status, String updatedBy) {
        ReplenishmentRequest entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Replenishment request not found with id: " + id));
        entity.setStatus(status);
        entity.setUpdatedBy(updatedBy);
        entity = repository.save(entity);
        return mapToResponse(entity);
    }

    private ReplenishmentResponse mapToResponse(ReplenishmentRequest entity) {
        ReplenishmentResponse response = new ReplenishmentResponse();
        response.setId(entity.getId());
        response.setProductId(entity.getProductId());
        response.setWarehouseId(entity.getWarehouseId());
        response.setRequestedQuantity(entity.getRequestedQuantity());
        response.setStatus(entity.getStatus());
        response.setRequestedBy(entity.getRequestedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
