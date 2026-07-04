package com.inventory.warehouse;

import com.inventory.warehouse.controller.WarehouseController;
import com.inventory.warehouse.dto.WarehouseRequest;
import com.inventory.warehouse.dto.WarehouseResponse;
import com.inventory.warehouse.service.WarehouseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseControllerTest {

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private WarehouseController warehouseController;

    @Test
    void createWarehouse_ShouldReturnCreated() {
        when(warehouseService.createWarehouse(any())).thenReturn(new WarehouseResponse());
        ResponseEntity<WarehouseResponse> res = warehouseController.createWarehouse(new WarehouseRequest());
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    void getAllWarehouses_ShouldReturnList() {
        when(warehouseService.getAllWarehouses()).thenReturn(List.of(new WarehouseResponse()));
        ResponseEntity<List<WarehouseResponse>> res = warehouseController.getAllWarehouses();
        assertEquals(1, res.getBody().size());
    }

    @Test
    void getWarehouseById_ShouldReturnWarehouse() {
        when(warehouseService.getWarehouseById(1L)).thenReturn(new WarehouseResponse());
        assertTrue(warehouseController.getWarehouseById(1L).getStatusCode().is2xxSuccessful());
    }

    @Test
    void updateWarehouse_ShouldSucceed() {
        when(warehouseService.updateWarehouse(eq(1L), any())).thenReturn(new WarehouseResponse());
        assertTrue(warehouseController.updateWarehouse(1L, new WarehouseRequest()).getStatusCode().is2xxSuccessful());
    }
}
