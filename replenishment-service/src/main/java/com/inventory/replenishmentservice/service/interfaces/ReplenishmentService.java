package com.inventory.replenishmentservice.service.interfaces;

import com.inventory.replenishmentservice.dto.request.CreateReplenishmentRequest;
import com.inventory.replenishmentservice.dto.response.ReplenishmentResponse;
import com.inventory.replenishmentservice.enums.ReplenishmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReplenishmentService {
    ReplenishmentResponse createRequest(CreateReplenishmentRequest request, String requestedBy);
    Page<ReplenishmentResponse> getAllRequests(Pageable pageable);
    ReplenishmentResponse updateStatus(Long id, ReplenishmentStatus status, String updatedBy);
}
