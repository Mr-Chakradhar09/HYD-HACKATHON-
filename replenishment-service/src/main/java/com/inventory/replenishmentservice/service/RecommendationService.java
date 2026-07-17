package com.inventory.replenishmentservice.service;

import com.inventory.replenishmentservice.dto.request.CreateManualRecommendationRequest;
import com.inventory.replenishmentservice.entity.ReplenishmentRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecommendationService {
    ReplenishmentRecommendation createManualRecommendation(CreateManualRecommendationRequest request, String createdBy);
    ReplenishmentRecommendation getRecommendationById(Long id);
    Page<ReplenishmentRecommendation> getAllRecommendations(Pageable pageable);
    void approveRecommendation(Long id, String approvedBy);
    void rejectRecommendation(Long id, String rejectedBy, String reason);
}
