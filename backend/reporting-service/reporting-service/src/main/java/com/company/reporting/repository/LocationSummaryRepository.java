package com.company.reporting.repository;

import com.company.reporting.entity.LocationSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationSummaryRepository extends JpaRepository<LocationSummary, Long> {
    Optional<LocationSummary> findByLocationNameAndLocationTypeAndMonth(String locationName, String locationType, String month);
    List<LocationSummary> findByMonth(String month);
    List<LocationSummary> findByLocationNameIgnoreCase(String locationName);
    List<LocationSummary> findByLocationNameIgnoreCaseAndLocationType(String locationName, String locationType);
}
