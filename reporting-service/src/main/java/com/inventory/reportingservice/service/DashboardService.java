package com.inventory.reportingservice.service;

import com.inventory.reportingservice.dto.response.DashboardSummary;
import com.inventory.reportingservice.entity.*;
import com.inventory.reportingservice.repository.*;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final ProductReadModelRepository productRepo;
    private final WarehouseReadModelRepository warehouseRepo;
    private final InventoryReadModelRepository inventoryRepo;
    private final MovementReadModelRepository movementRepo;

    public DashboardService(ProductReadModelRepository productRepo, WarehouseReadModelRepository warehouseRepo,
                           InventoryReadModelRepository inventoryRepo, MovementReadModelRepository movementRepo) {
        this.productRepo = productRepo;
        this.warehouseRepo = warehouseRepo;
        this.inventoryRepo = inventoryRepo;
        this.movementRepo = movementRepo;
    }

    public DashboardSummary getSummary() {
        DashboardSummary summary = new DashboardSummary();
        summary.setTotalProducts(productRepo.count());
        summary.setTotalWarehouses(warehouseRepo.count());
        summary.setTotalInventoryItems(inventoryRepo.count());
        summary.setLowStockItems(inventoryRepo.countByStockStatus("LOW_STOCK"));
        summary.setOutOfStockItems(inventoryRepo.countByStockStatus("OUT_OF_STOCK"));
        summary.setTotalMovements(movementRepo.count());
        return summary;
    }
}
