package com.inventory.warehouse;

import com.inventory.warehouse.dto.WarehouseRequest;
import com.inventory.warehouse.dto.WarehouseResponse;
import com.inventory.warehouse.entity.Warehouse;
import com.inventory.warehouse.repository.WarehouseRepository;
import com.inventory.warehouse.service.WarehouseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    @Test
    void createWarehouse_ShouldSucceed() {
        WarehouseRequest req = new WarehouseRequest();
        req.setCode("WH01");
        req.setName("Main Warehouse");
        req.setCity("New York");
        req.setState("NY");
        req.setCountry("USA");
        req.setCapacity(10000);

        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(i -> {
            Warehouse w = i.getArgument(0);
            w.setId(1L);
            return w;
        });

        WarehouseResponse res = warehouseService.createWarehouse(req);
        assertEquals("WH01", res.getCode());
        assertEquals("Main Warehouse", res.getName());
        assertEquals("New York", res.getCity());
        assertEquals(10000, res.getCapacity());
        assertNotNull(res.getId());
    }

    @Test
    void getAllWarehouses_ShouldReturnList() {
        Warehouse w1 = new Warehouse(); w1.setId(1L); w1.setCode("WH01"); w1.setName("Main");
        Warehouse w2 = new Warehouse(); w2.setId(2L); w2.setCode("WH02"); w2.setName("Secondary");

        when(warehouseRepository.findAll()).thenReturn(List.of(w1, w2));

        List<WarehouseResponse> list = warehouseService.getAllWarehouses();
        assertEquals(2, list.size());
    }

    @Test
    void getWarehouseById_ShouldReturnWarehouse() {
        Warehouse w = new Warehouse(); w.setId(1L); w.setCode("WH01"); w.setName("Main");

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));

        WarehouseResponse res = warehouseService.getWarehouseById(1L);
        assertEquals("WH01", res.getCode());
    }

    @Test
    void getWarehouseById_NotFound_ShouldThrow() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> warehouseService.getWarehouseById(99L));
    }

    @Test
    void updateWarehouse_ShouldUpdateFields() {
        Warehouse w = new Warehouse(); w.setId(1L); w.setCode("WH01"); w.setName("Old Name");

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(w));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(i -> i.getArgument(0));

        WarehouseRequest req = new WarehouseRequest();
        req.setCode("WH01-UPDATED");
        req.setName("Updated Name");
        req.setCapacity(5000);

        WarehouseResponse res = warehouseService.updateWarehouse(1L, req);
        assertEquals("WH01-UPDATED", res.getCode());
        assertEquals("Updated Name", res.getName());
        assertEquals(5000, res.getCapacity());
    }
}
