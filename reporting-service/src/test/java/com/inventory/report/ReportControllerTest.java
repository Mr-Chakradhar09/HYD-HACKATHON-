package com.inventory.report;

import com.inventory.report.controller.ReportController;
import com.inventory.report.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @Test
    void getMovementReport_ShouldReturnOk() {
        when(reportService.getMovementReport()).thenReturn(List.of(Map.of("type", "INBOUND")));
        ResponseEntity<List<Map<String, Object>>> res = reportController.getMovementReport();
        assertTrue(res.getStatusCode().is2xxSuccessful());
        assertEquals(1, res.getBody().size());
    }

    @Test
    void getWarehouseReport_ShouldReturnOk() {
        when(reportService.getWarehouseReport()).thenReturn(List.of(Map.of("warehouseId", 1)));
        ResponseEntity<List<Map<String, Object>>> res = reportController.getWarehouseReport();
        assertTrue(res.getStatusCode().is2xxSuccessful());
    }
}
