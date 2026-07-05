package com.company.reporting.service.impl;

import com.company.reporting.dto.DashboardResponseDTO;
import com.company.reporting.dto.LocationReportDTO;
import com.company.reporting.dto.ThemeReportDTO;
import com.company.reporting.entity.LocationSummary;
import com.company.reporting.entity.PulseSummary;
import com.company.reporting.entity.ThemeSummary;
import com.company.reporting.mapper.DashboardMapper;
import com.company.reporting.mapper.ReportMapper;
import com.company.reporting.repository.LocationSummaryRepository;
import com.company.reporting.repository.PulseSummaryRepository;
import com.company.reporting.repository.ThemeSummaryRepository;
import com.company.reporting.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final PulseSummaryRepository pulseSummaryRepository;
    private final LocationSummaryRepository locationSummaryRepository;
    private final ThemeSummaryRepository themeSummaryRepository;
    private final DashboardMapper dashboardMapper;
    private final ReportMapper reportMapper;

    @Override
    public DashboardResponseDTO getOverviewDashboard(String month) {
        PulseSummary summary = pulseSummaryRepository.findByMonth(month).orElse(null);

        // Fetch top concern (lowest scoring theme)
        String topConcern = themeSummaryRepository.findByMonth(month).stream()
                .min(Comparator.comparing(ThemeSummary::getScore))
                .map(ThemeSummary::getTheme)
                .orElse("N/A");

        // Fetch highest rated location (highest scoring city)
        String highestRatedLocation = locationSummaryRepository.findByMonth(month).stream()
                .filter(l -> "CITY".equalsIgnoreCase(l.getLocationType()))
                .max(Comparator.comparing(LocationSummary::getScore))
                .map(LocationSummary::getLocationName)
                .orElse("N/A");

        return dashboardMapper.toDashboardResponseDTO(summary, topConcern, highestRatedLocation);
    }

    @Override
    public List<LocationReportDTO> getLocationDashboard(String month) {
        return locationSummaryRepository.findByMonth(month).stream()
                .filter(l -> "CITY".equalsIgnoreCase(l.getLocationType()))
                .map(reportMapper::toLocationReportDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ThemeReportDTO> getThemeDashboard(String month) {
        return themeSummaryRepository.findByMonth(month).stream()
                .map(reportMapper::toThemeReportDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LocationReportDTO> getDepartmentDashboard(String month) {
        // Since department aggregation is stored as location summaries with type "DEPARTMENT" or separate,
        // let's return location summaries with type "DEPARTMENT" or fetch them!
        // To be safe, we retrieve location summaries where type is "DEPARTMENT" or filter accordingly.
        return locationSummaryRepository.findByMonth(month).stream()
                .filter(l -> "DEPARTMENT".equalsIgnoreCase(l.getLocationType()))
                .map(reportMapper::toLocationReportDTO)
                .collect(Collectors.toList());
    }
}
