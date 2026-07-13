package com.inventory.inventory.repository;

import com.inventory.inventory.entity.IdempotentApiRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotentApiRequestRepository extends JpaRepository<IdempotentApiRequest, String> {
}
