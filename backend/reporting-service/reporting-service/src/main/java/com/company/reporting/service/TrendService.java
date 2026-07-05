package com.company.reporting.service;

import com.company.reporting.dto.TrendDTO;
import java.util.List;

public interface TrendService {
    List<TrendDTO> getOrganizationTrends();
    List<TrendDTO> getLocationTrends(String location);
    List<TrendDTO> getThemeTrends(String theme);
}
