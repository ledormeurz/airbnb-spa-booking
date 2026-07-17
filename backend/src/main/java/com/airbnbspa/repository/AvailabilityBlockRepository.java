package com.airbnbspa.repository;

import com.airbnbspa.entity.AvailabilityBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvailabilityBlockRepository extends JpaRepository<AvailabilityBlock, Long> {
    List<AvailabilityBlock> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate end, LocalDate start);

    @Query("SELECT a FROM AvailabilityBlock a WHERE a.startDate < :endDate AND a.endDate > :startDate")
    List<AvailabilityBlock> findOverlapping(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<AvailabilityBlock> findByStartDateGreaterThanEqual(LocalDate date);
    List<AvailabilityBlock> findByStartDateGreaterThanEqualAndEndDateLessThanEqual(LocalDate start, LocalDate end);
}