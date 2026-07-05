package com.company.reporting.service.impl;

import com.company.reporting.dto.LocationReportDTO;
import com.company.reporting.entity.LocationSummary;
import com.company.reporting.entity.SurveySnapshot;
import com.company.reporting.exception.ReportNotFoundException;
import com.company.reporting.mapper.ReportMapper;
import com.company.reporting.repository.LocationSummaryRepository;
import com.company.reporting.repository.SurveySnapshotRepository;
import com.company.reporting.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final LocationSummaryRepository locationSummaryRepository;
    private final SurveySnapshotRepository surveySnapshotRepository;
    private final ReportMapper reportMapper;

    @Override
    public List<LocationReportDTO> getReportsByType(String locationType, String month) {
        return locationSummaryRepository.findByMonth(month).stream()
                .filter(l -> locationType.equalsIgnoreCase(l.getLocationType()))
                .map(reportMapper::toLocationReportDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LocationReportDTO getReportByNameAndType(String locationName, String locationType, String month) {
        LocationSummary summary = locationSummaryRepository
                .findByLocationNameAndLocationTypeAndMonth(locationName, locationType, month)
                .orElseThrow(() -> new ReportNotFoundException(
                        String.format("Report not found for location: %s (%s) in month: %s", locationName, locationType, month)));
        return reportMapper.toLocationReportDTO(summary);
    }

    @Override
    public List<SurveySnapshot> getRawSnapshotsByMonth(String month) {
        return surveySnapshotRepository.findByMonth(month);
    }

    @Override
    public List<SurveySnapshot> getRawSnapshotsBySurvey(Long surveyId) {
        return surveySnapshotRepository.findBySurveyId(surveyId);
    }
}
