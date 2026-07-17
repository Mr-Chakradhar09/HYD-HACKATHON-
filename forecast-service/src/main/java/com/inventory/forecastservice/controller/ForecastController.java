package com.inventory.forecastservice.controller;

import com.inventory.forecastservice.entity.InventoryForecast;
import com.inventory.forecastservice.repository.InventoryForecastRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/forecast")
public class ForecastController {

    private final InventoryForecastRepository repository;

    public ForecastController(InventoryForecastRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryForecast>> getForecast(
            @PathVariable Long productId,
            @PathVariable Long warehouseId) {
            
        List<InventoryForecast> forecasts = repository.findByProductIdAndWarehouseIdAndForecastDateGreaterThanEqual(
            productId, warehouseId, LocalDate.now());
            
        return ResponseEntity.ok(forecasts);
    }

    @PostMapping("/generate")
    public ResponseEntity<InventoryForecast> generateManualForecast(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Integer predictedDemand) {
            
        InventoryForecast forecast = new InventoryForecast();
        forecast.setProductId(productId);
        forecast.setWarehouseId(warehouseId);
        forecast.setPredictedDemand(predictedDemand);
        forecast.setConfidenceScore(0.85); // Mocked confidence score
        forecast.setForecastDate(LocalDate.now().plusDays(7));
        forecast.setGeneratedAt(LocalDateTime.now());
        
        return ResponseEntity.ok(repository.save(forecast));
    }
}
