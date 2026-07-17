package com.inventory.reportingservice.dto.response;

public class DashboardSummary {
    private long totalProducts;
    private long totalWarehouses;
    private long totalInventoryItems;
    private long lowStockItems;
    private long outOfStockItems;
    private long totalMovements;

    public DashboardSummary() {}
    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }
    public long getTotalWarehouses() { return totalWarehouses; }
    public void setTotalWarehouses(long totalWarehouses) { this.totalWarehouses = totalWarehouses; }
    public long getTotalInventoryItems() { return totalInventoryItems; }
    public void setTotalInventoryItems(long totalInventoryItems) { this.totalInventoryItems = totalInventoryItems; }
    public long getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(long lowStockItems) { this.lowStockItems = lowStockItems; }
    public long getOutOfStockItems() { return outOfStockItems; }
    public void setOutOfStockItems(long outOfStockItems) { this.outOfStockItems = outOfStockItems; }
    public long getTotalMovements() { return totalMovements; }
    public void setTotalMovements(long totalMovements) { this.totalMovements = totalMovements; }
}
