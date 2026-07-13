package com.inventory.warehouse.controller;

import com.inventory.warehouse.dto.WarehouseResponse;
import com.inventory.warehouse.entity.Warehouse;
import com.inventory.warehouse.service.WarehouseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WarehouseControllerTest {

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private WarehouseController warehouseController;

    @Test
    void testGetAllWarehouses() {
        Warehouse w = new Warehouse();
        w.setId(1L);
        WarehouseResponse res = WarehouseResponse.fromEntity(w);
        
        when(warehouseService.getAllWarehouses()).thenReturn(Collections.singletonList(res));

        List<WarehouseResponse> list = warehouseController.getAllWarehouses().getBody();

        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());
    }
}
