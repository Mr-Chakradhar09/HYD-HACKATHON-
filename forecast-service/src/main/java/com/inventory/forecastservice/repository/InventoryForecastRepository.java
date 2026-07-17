package com.inventory.forecastservice.repository;

import com.inventory.forecastservice.entity.InventoryForecast;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryForecastRepository extends MongoRepository<InventoryForecast, String> {
    List<InventoryForecast> findByProductIdAndWarehouseIdAndForecastDateGreaterThanEqual(Long productId, Long warehouseId, LocalDate date);
}
