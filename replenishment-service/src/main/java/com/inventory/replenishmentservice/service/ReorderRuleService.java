package com.inventory.replenishmentservice.service;

import com.inventory.replenishmentservice.dto.request.CreateReorderRuleRequest;
import com.inventory.replenishmentservice.entity.ReorderRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReorderRuleService {
    ReorderRule createRule(CreateReorderRuleRequest request, String createdBy);
    ReorderRule getRuleById(Long id);
    Page<ReorderRule> getAllRules(Pageable pageable);
    ReorderRule updateRule(Long id, CreateReorderRuleRequest request, String updatedBy);
    void deactivateRule(Long id, String updatedBy);
}
