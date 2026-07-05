package com.company.reporting.service.impl;

import com.company.reporting.dto.TrendDTO;
import com.company.reporting.entity.LocationSummary;
import com.company.reporting.entity.PulseSummary;
import com.company.reporting.entity.ThemeSummary;
import com.company.reporting.repository.LocationSummaryRepository;
import com.company.reporting.repository.PulseSummaryRepository;
import com.company.reporting.repository.ThemeSummaryRepository;
import com.company.reporting.service.TrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrendServiceImpl implements TrendService {

    private final PulseSummaryRepository pulseSummaryRepository;
    private final LocationSummaryRepository locationSummaryRepository;
    private final ThemeSummaryRepository themeSummaryRepository;

    @Override
    public List<TrendDTO> getOrganizationTrends() {
        return pulseSummaryRepository.findAll().stream()
                .sorted(Comparator.comparing(PulseSummary::getMonth))
                .map(p -> TrendDTO.builder()
                        .month(p.getMonth())
                        .pulseScore(p.getPulseScore())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<TrendDTO> getLocationTrends(String location) {
        return locationSummaryRepository.findByLocationNameIgnoreCase(location).stream()
                .sorted(Comparator.comparing(LocationSummary::getMonth))
                .map(l -> TrendDTO.builder()
                        .month(l.getMonth())
                        .pulseScore(l.getScore())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<TrendDTO> getThemeTrends(String theme) {
        return themeSummaryRepository.findByThemeIgnoreCase(theme).stream()
                .sorted(Comparator.comparing(ThemeSummary::getMonth))
                .map(t -> TrendDTO.builder()
                        .month(t.getMonth())
                        .pulseScore(t.getScore())
                        .build())
                .collect(Collectors.toList());
    }
}
