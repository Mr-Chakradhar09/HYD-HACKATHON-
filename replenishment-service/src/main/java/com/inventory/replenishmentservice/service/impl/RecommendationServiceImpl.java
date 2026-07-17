package com.inventory.replenishmentservice.service.impl;

import com.inventory.replenishmentservice.dto.request.CreateManualRecommendationRequest;
import com.inventory.replenishmentservice.entity.ReplenishmentRecommendation;
import com.inventory.replenishmentservice.enums.RecommendationStatus;
import com.inventory.replenishmentservice.enums.ReplenishmentType;
import com.inventory.replenishmentservice.exception.InvalidOperationException;
import com.inventory.replenishmentservice.exception.ResourceNotFoundException;
import com.inventory.replenishmentservice.repository.ReplenishmentRecommendationRepository;
import com.inventory.replenishmentservice.service.RecommendationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final ReplenishmentRecommendationRepository recommendationRepository;
    private static final AtomicLong counter = new AtomicLong(1);

    public RecommendationServiceImpl(ReplenishmentRecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    @Transactional
    public ReplenishmentRecommendation createManualRecommendation(CreateManualRecommendationRequest request, String createdBy) {
        ReplenishmentRecommendation rec = new ReplenishmentRecommendation();
        rec.setRecommendationNumber("REP-" + String.format("%06d", counter.getAndIncrement()));
        rec.setProductId(request.getProductId());
        rec.setWarehouseId(request.getWarehouseId());
        rec.setReplenishmentType(ReplenishmentType.MANUAL);
        rec.setRecommendedQuantity(request.getRecommendedQuantity());
        rec.setPriority(request.getPriority());
        rec.setCreatedBy(createdBy);
        return recommendationRepository.save(rec);
    }

    @Override
    public ReplenishmentRecommendation getRecommendationById(Long id) {
        return recommendationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Recommendation not found"));
    }

    @Override
    public Page<ReplenishmentRecommendation> getAllRecommendations(Pageable pageable) {
        return recommendationRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void approveRecommendation(Long id, String approvedBy) {
        ReplenishmentRecommendation rec = getRecommendationById(id);
        if (rec.getStatus() != RecommendationStatus.PENDING_APPROVAL) throw new InvalidOperationException("Cannot approve recommendation in current status");
        rec.setStatus(RecommendationStatus.APPROVED);
        rec.setApprovedBy(approvedBy);
        rec.setApprovedAt(LocalDateTime.now());
        recommendationRepository.save(rec);
    }

    @Override
    @Transactional
    public void rejectRecommendation(Long id, String rejectedBy, String reason) {
        ReplenishmentRecommendation rec = getRecommendationById(id);
        if (rec.getStatus() != RecommendationStatus.PENDING_APPROVAL) throw new InvalidOperationException("Cannot reject recommendation in current status");
        rec.setStatus(RecommendationStatus.REJECTED);
        rec.setRejectionReason(reason);
        recommendationRepository.save(rec);
    }
}
