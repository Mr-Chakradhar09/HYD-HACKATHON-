package com.inventory.inventory.repository;

import com.inventory.inventory.entity.IdempotentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotentEventRepository extends JpaRepository<IdempotentEvent, String> {
}
