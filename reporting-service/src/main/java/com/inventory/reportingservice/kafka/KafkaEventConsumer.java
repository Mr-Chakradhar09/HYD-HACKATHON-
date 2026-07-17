package com.inventory.reportingservice.kafka;

import com.inventory.reportingservice.entity.*;
import com.inventory.reportingservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class KafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    private final ProcessedEventRepository processedEventRepo;
    private final ProductReadModelRepository productRepo;
    private final WarehouseReadModelRepository warehouseRepo;
    private final InventoryReadModelRepository inventoryRepo;
    private final MovementReadModelRepository movementRepo;
    private final MovementHistoryRepository movementHistoryRepo;

    public KafkaEventConsumer(ProcessedEventRepository processedEventRepo,
                              ProductReadModelRepository productRepo,
                              WarehouseReadModelRepository warehouseRepo,
                              InventoryReadModelRepository inventoryRepo,
                              MovementReadModelRepository movementRepo,
                              MovementHistoryRepository movementHistoryRepo) {
        this.processedEventRepo = processedEventRepo;
        this.productRepo = productRepo;
        this.warehouseRepo = warehouseRepo;
        this.inventoryRepo = inventoryRepo;
        this.movementRepo = movementRepo;
        this.movementHistoryRepo = movementHistoryRepo;
    }

    @KafkaListener(topics = "product-events", groupId = "reporting-service")
    public void handleProductEvent(Map<String, Object> event) {
        String eventId = (String) event.get("eventId");
        if (eventId == null || processedEventRepo.existsByEventId(eventId)) return;
        String eventType = (String) event.get("eventType");
        Long productId = ((Number) event.get("productId")).longValue();
        log.info("Processing product event: {} for product: {}", eventType, productId);
        ProductReadModel model = productRepo.findByProductId(productId).orElse(new ProductReadModel());
        model.setProductId(productId);
        model.setSku((String) event.get("sku"));
        model.setProductName((String) event.get("productName"));
        model.setBrand((String) event.get("brand"));
        productRepo.save(model);
        processedEventRepo.save(new ProcessedEvent(eventId, eventType));
    }

    @KafkaListener(topics = "warehouse-events", groupId = "reporting-service")
    public void handleWarehouseEvent(Map<String, Object> event) {
        String eventId = (String) event.get("eventId");
        if (eventId == null || processedEventRepo.existsByEventId(eventId)) return;
        String eventType = (String) event.get("eventType");
        Long warehouseId = ((Number) event.get("warehouseId")).longValue();
        log.info("Processing warehouse event: {} for warehouse: {}", eventType, warehouseId);
        WarehouseReadModel model = warehouseRepo.findByWarehouseId(warehouseId).orElse(new WarehouseReadModel());
        model.setWarehouseId(warehouseId);
        model.setWarehouseCode((String) event.get("warehouseCode"));
        model.setWarehouseName((String) event.get("warehouseName"));
        warehouseRepo.save(model);
        processedEventRepo.save(new ProcessedEvent(eventId, eventType));
    }

    @KafkaListener(topics = "inventory-events", groupId = "reporting-service")
    public void handleInventoryEvent(Map<String, Object> event) {
        String eventId = (String) event.get("eventId");
        if (eventId == null || processedEventRepo.existsByEventId(eventId)) return;
        String eventType = (String) event.get("eventType");
        Long inventoryId = ((Number) event.get("inventoryId")).longValue();
        log.info("Processing inventory event: {} for inventory: {}", eventType, inventoryId);
        InventoryReadModel model = inventoryRepo.findByInventoryId(inventoryId).orElse(new InventoryReadModel());
        model.setInventoryId(inventoryId);
        model.setProductId(((Number) event.get("productId")).longValue());
        model.setWarehouseId(((Number) event.get("warehouseId")).longValue());
        inventoryRepo.save(model);
        processedEventRepo.save(new ProcessedEvent(eventId, eventType));
    }

    @KafkaListener(topics = "movement-events", groupId = "reporting-service")
    public void handleMovementEvent(Map<String, Object> event) {
        String eventId = (String) event.get("eventId");
        if (eventId == null || processedEventRepo.existsByEventId(eventId)) return;
        String eventType = (String) event.get("eventType");
        log.info("Processing movement event: {}", eventType);

        // Update MovementReadModel (quick lookup)
        MovementReadModel model = new MovementReadModel();
        model.setMovementId(event.get("movementId") != null ? event.get("movementId").toString() : null);
        model.setMovementNumber((String) event.get("movementNumber"));
        model.setMovementType((String) event.get("movementType"));
        movementRepo.save(model);

        // Persist full history in MovementHistory
        MovementHistory history = new MovementHistory();
        history.setEventId(eventId);
        history.setMovementNumber((String) event.get("movementNumber"));
        history.setMovementType((String) event.get("movementType"));
        if (event.get("productId") != null) {
            history.setProductId(((Number) event.get("productId")).longValue());
        }
        if (event.get("sourceWarehouseId") != null) {
            history.setSourceWarehouseId(((Number) event.get("sourceWarehouseId")).longValue());
        }
        if (event.get("destinationWarehouseId") != null) {
            history.setDestinationWarehouseId(((Number) event.get("destinationWarehouseId")).longValue());
        }
        if (event.get("quantity") != null) {
            history.setQuantity(((Number) event.get("quantity")).intValue());
        }
        history.setPerformedBy((String) event.get("performedBy"));
        history.setMovementTimestamp(LocalDateTime.now());
        movementHistoryRepo.save(history);

        processedEventRepo.save(new ProcessedEvent(eventId, eventType));
    }
}
