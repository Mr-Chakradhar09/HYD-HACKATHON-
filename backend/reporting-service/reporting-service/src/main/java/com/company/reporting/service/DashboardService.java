package com.company.reporting.service;

import com.company.reporting.dto.DashboardResponseDTO;
import com.company.reporting.dto.LocationReportDTO;
import com.company.reporting.dto.ThemeReportDTO;
import java.util.List;

public interface DashboardService {
    DashboardResponseDTO getOverviewDashboard(String month);
    List<LocationReportDTO> getLocationDashboard(String month);
    List<ThemeReportDTO> getThemeDashboard(String month);
    List<LocationReportDTO> getDepartmentDashboard(String month);
}
