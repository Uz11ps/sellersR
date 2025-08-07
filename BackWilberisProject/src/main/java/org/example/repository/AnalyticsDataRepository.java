package org.example.repository;

import org.example.entity.AnalyticsData;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsDataRepository extends JpaRepository<AnalyticsData, Long> {
    
    List<AnalyticsData> findByUserOrderByPeriodStartDesc(User user);
    
    List<AnalyticsData> findByUserAndPeriodTypeOrderByPeriodStartDesc(User user, String periodType);
    
    Optional<AnalyticsData> findByUserAndPeriodStartAndPeriodEnd(User user, LocalDate periodStart, LocalDate periodEnd);
    
    @Query("SELECT a FROM AnalyticsData a WHERE a.user = :user AND a.periodStart >= :startDate AND a.periodEnd <= :endDate ORDER BY a.periodStart DESC")
    List<AnalyticsData> findByUserAndDateRange(@Param("user") User user, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM AnalyticsData a WHERE a.user = :user ORDER BY a.periodStart DESC LIMIT 8")
    List<AnalyticsData> findLast8WeeksByUser(@Param("user") User user);
    
    // Дополнительные методы для контроллера
    List<AnalyticsData> findByUserAndPeriodStartGreaterThanEqual(User user, LocalDate startDate);
    
    long countByUser(User user);
} 