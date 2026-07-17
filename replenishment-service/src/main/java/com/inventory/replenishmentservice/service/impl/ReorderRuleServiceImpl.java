package com.inventory.replenishmentservice.service.impl;

import com.inventory.replenishmentservice.dto.request.CreateReorderRuleRequest;
import com.inventory.replenishmentservice.entity.ReorderRule;
import com.inventory.replenishmentservice.enums.RuleStatus;
import com.inventory.replenishmentservice.exception.DuplicateResourceException;
import com.inventory.replenishmentservice.exception.InvalidOperationException;
import com.inventory.replenishmentservice.exception.ResourceNotFoundException;
import com.inventory.replenishmentservice.repository.ReorderRuleRepository;
import com.inventory.replenishmentservice.service.ReorderRuleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReorderRuleServiceImpl implements ReorderRuleService {

    private final ReorderRuleRepository ruleRepository;

    public ReorderRuleServiceImpl(ReorderRuleRepository ruleRepository) { this.ruleRepository = ruleRepository; }

    @Override
    @Transactional
    public ReorderRule createRule(CreateReorderRuleRequest request, String createdBy) {
        if (ruleRepository.existsByProductIdAndWarehouseIdAndStatus(request.getProductId(), request.getWarehouseId(), RuleStatus.ACTIVE)) {
            throw new DuplicateResourceException("Active rule already exists for this product and warehouse");
        }
        ReorderRule rule = new ReorderRule();
        rule.setProductId(request.getProductId());
        rule.setWarehouseId(request.getWarehouseId());
        rule.setMinimumStock(request.getMinimumStock());
        rule.setMaximumStock(request.getMaximumStock());
        rule.setReorderPoint(request.getReorderPoint());
        rule.setSafetyStock(request.getSafetyStock());
        rule.setReorderQuantity(request.getReorderQuantity());
        rule.setReplenishmentMode(request.getReplenishmentMode());
        rule.setCreatedBy(createdBy);
        return ruleRepository.save(rule);
    }

    @Override
    public ReorderRule getRuleById(Long id) {
        return ruleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rule not found"));
    }

    @Override
    public Page<ReorderRule> getAllRules(Pageable pageable) { return ruleRepository.findAll(pageable); }

    @Override
    @Transactional
    public ReorderRule updateRule(Long id, CreateReorderRuleRequest request, String updatedBy) {
        ReorderRule rule = getRuleById(id);
        rule.setMinimumStock(request.getMinimumStock());
        rule.setMaximumStock(request.getMaximumStock());
        rule.setReorderPoint(request.getReorderPoint());
        rule.setSafetyStock(request.getSafetyStock());
        rule.setReorderQuantity(request.getReorderQuantity());
        rule.setReplenishmentMode(request.getReplenishmentMode());
        rule.setUpdatedBy(updatedBy);
        return ruleRepository.save(rule);
    }

    @Override
    @Transactional
    public void deactivateRule(Long id, String updatedBy) {
        ReorderRule rule = getRuleById(id);
        if (rule.getStatus() == RuleStatus.INACTIVE) throw new InvalidOperationException("Rule is already inactive");
        rule.setStatus(RuleStatus.INACTIVE);
        rule.setUpdatedBy(updatedBy);
        ruleRepository.save(rule);
    }
}
