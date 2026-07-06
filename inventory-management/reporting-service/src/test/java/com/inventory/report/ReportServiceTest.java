package com.inventory.report;

import com.inventory.report.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Test
    void getMovementReport_ShouldReturnSampleData() {
        ReportService reportService = new ReportService("http://localhost:8084");
        List<Map<String, Object>> report = reportService.getMovementReport();

        assertNotNull(report);
        assertEquals(4, report.size());

        Map<String, Object> inbound = report.get(0);
        assertEquals("INBOUND", inbound.get("type"));
        assertTrue(((Number) inbound.get("count")).intValue() > 0);
    }

    @Test
    void getMovementReport_ContainsExpectedKeys() {
        ReportService reportService = new ReportService("http://localhost:8084");
        List<Map<String, Object>> report = reportService.getMovementReport();

        for (Map<String, Object> entry : report) {
            assertTrue(entry.containsKey("type"));
            assertTrue(entry.containsKey("count"));
            assertTrue(entry.containsKey("totalQuantity"));
        }
    }

    @Test
    void constructor_ShouldCreateInstanceWithoutError() {
        ReportService reportService = new ReportService("http://localhost:8084");
        assertNotNull(reportService);
    }
}
