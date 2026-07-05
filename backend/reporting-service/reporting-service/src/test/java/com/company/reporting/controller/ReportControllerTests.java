package com.company.reporting.controller;

import com.company.reporting.dto.DashboardResponseDTO;
import com.company.reporting.entity.SurveySnapshot;
import com.company.reporting.service.AnalyticsService;
import com.company.reporting.service.DashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.CoreMatchers.containsString;

@SpringBootTest(properties = "app.seeding.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class ReportControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void testGetDashboardOverview() throws Exception {
        DashboardResponseDTO mockOverview = DashboardResponseDTO.builder()
                .pulseScore(78.5)
                .participationRate(89.2)
                .positiveSentiment(72.0)
                .neutralSentiment(16.0)
                .negativeSentiment(12.0)
                .totalResponses(4500)
                .topConcern("Workload")
                .highestRatedLocation("Chennai")
                .build();

        when(dashboardService.getOverviewDashboard(anyString())).thenReturn(mockOverview);

        mockMvc.perform(get("/api/reports/dashboard/overview")
                        .param("month", "2026-07")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pulseScore").value(78.5))
                .andExpect(jsonPath("$.participationRate").value(89.2))
                .andExpect(jsonPath("$.topConcern").value("Workload"))
                .andExpect(jsonPath("$.highestRatedLocation").value("Chennai"));
    }

    @Test
    void testIngestSurveySnapshot() throws Exception {
        SurveySnapshot snapshot = SurveySnapshot.builder()
                .employeeId(101L)
                .city("Chennai")
                .department("Engineering")
                .sentiment("POSITIVE")
                .sentimentScore(80.0)
                .theme("Work-Life Balance")
                .month("2026-07")
                .build();

        doNothing().when(analyticsService).processSurveySubmission(any(SurveySnapshot.class));

        mockMvc.perform(post("/api/reports/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(snapshot)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Successfully processed")));

        verify(analyticsService, times(1)).processSurveySubmission(any(SurveySnapshot.class));
    }
}
