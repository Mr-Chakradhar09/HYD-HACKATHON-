package com.inventory.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class NotificationService {

    private final ObjectMapper objectMapper;

    public NotificationService() {
        this.objectMapper = new ObjectMapper();
    }

    public void processInventoryUpdated(String message) {
        try {
            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            String type = (String) data.get("type");
            Number productId = (Number) data.get("productId");
            Number quantity = (Number) data.get("quantity");

            System.out.println("=== NOTIFICATION: Inventory Updated ===");
            System.out.println("Type: " + type);
            System.out.println("Product ID: " + productId);
            System.out.println("Quantity: " + quantity);

            if ("OUTBOUND".equals(type) && quantity.intValue() < 10) {
                System.out.println("*** LOW STOCK ALERT *** Product ID: " + productId + " is running low!");
            }
        } catch (Exception e) {
            System.err.println("Failed to process inventory update: " + e.getMessage());
        }
    }

    public void processStockTransferred(String message) {
        try {
            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            System.out.println("=== NOTIFICATION: Stock Transfer Completed ===");
            System.out.println("Product ID: " + data.get("productId"));
            System.out.println("From Warehouse: " + data.get("fromWarehouse"));
            System.out.println("To Warehouse: " + data.get("toWarehouse"));
            System.out.println("Quantity: " + data.get("quantity"));
        } catch (Exception e) {
            System.err.println("Failed to process stock transfer: " + e.getMessage());
        }
    }

    public void processPurchaseRequestCreated(String message) {
        try {
            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            System.out.println("=== NOTIFICATION: Purchase Request Created ===");
            System.out.println("Request ID: " + data.get("id"));
            System.out.println("Product ID: " + data.get("productId"));
            System.out.println("Required Quantity: " + data.get("requiredQuantity"));
            System.out.println("Status: " + data.get("status"));
        } catch (Exception e) {
            System.err.println("Failed to process purchase request: " + e.getMessage());
        }
    }
}
