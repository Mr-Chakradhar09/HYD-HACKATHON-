package com.company.reporting.service;

import com.company.reporting.dto.LocationReportDTO;
import com.company.reporting.entity.SurveySnapshot;
import java.util.List;

public interface ReportService {
    List<LocationReportDTO> getReportsByType(String locationType, String month);
    LocationReportDTO getReportByNameAndType(String locationName, String locationType, String month);
    List<SurveySnapshot> getRawSnapshotsByMonth(String month);
    List<SurveySnapshot> getRawSnapshotsBySurvey(Long surveyId);
}
