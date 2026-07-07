package com.inventory.warehouse.service;

import com.inventory.warehouse.dto.WarehouseRequest;
import com.inventory.warehouse.dto.WarehouseResponse;
import com.inventory.warehouse.entity.Warehouse;
import com.inventory.warehouse.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        Warehouse w = new Warehouse();
        w.setCode(request.getCode());
        w.setName(request.getName());
        w.setCity(request.getCity());
        w.setState(request.getState());
        w.setCountry(request.getCountry());
        w.setCapacity(request.getCapacity());
        w = warehouseRepository.save(w);
        return WarehouseResponse.fromEntity(w);
    }

    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll().stream().map(WarehouseResponse::fromEntity).toList();
    }

    public WarehouseResponse getWarehouseById(Long id) {
        Warehouse w = warehouseRepository.findById(id).orElseThrow(() -> new RuntimeException("Warehouse not found"));
        return WarehouseResponse.fromEntity(w);
    }

    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse w = warehouseRepository.findById(id).orElseThrow(() -> new RuntimeException("Warehouse not found"));
        w.setCode(request.getCode());
        w.setName(request.getName());
        w.setCity(request.getCity());
        w.setState(request.getState());
        w.setCountry(request.getCountry());
        w.setCapacity(request.getCapacity());
        w = warehouseRepository.save(w);
        return WarehouseResponse.fromEntity(w);
    }

    public void deactivateWarehouse(Long id) {
        Warehouse w = warehouseRepository.findById(id).orElseThrow(() -> new RuntimeException("Warehouse not found"));
        w.setStatus("INACTIVE");
        warehouseRepository.save(w);
    }

    public void activateWarehouse(Long id) {
        Warehouse w = warehouseRepository.findById(id).orElseThrow(() -> new RuntimeException("Warehouse not found"));
        w.setStatus("ACTIVE");
        warehouseRepository.save(w);
    }
}
