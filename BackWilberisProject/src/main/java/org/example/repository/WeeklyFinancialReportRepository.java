package org.example.repository;

import org.example.entity.Seller;
import org.example.entity.WeeklyFinancialReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyFinancialReportRepository extends JpaRepository<WeeklyFinancialReport, Long> {
    
    List<WeeklyFinancialReport> findBySeller(Seller seller);
    
    List<WeeklyFinancialReport> findBySellerOrderByDatePeriodDesc(Seller seller);
    
    Optional<WeeklyFinancialReport> findBySellerAndWeekNumber(Seller seller, Integer weekNumber);
    
    @Query("SELECT w FROM WeeklyFinancialReport w WHERE w.seller = :seller AND w.datePeriod >= :fromDate AND w.datePeriod <= :toDate")
    List<WeeklyFinancialReport> findBySellerAndDateRange(@Param("seller") Seller seller, 
                                                        @Param("fromDate") LocalDate fromDate,
                                                        @Param("toDate") LocalDate toDate);
    
    @Query("SELECT SUM(w.netProfit) FROM WeeklyFinancialReport w WHERE w.seller = :seller")
    java.math.BigDecimal getTotalNetProfitBySeller(@Param("seller") Seller seller);
    
    @Query("SELECT COUNT(w) FROM WeeklyFinancialReport w WHERE w.seller = :seller")
    long countBySeller(@Param("seller") Seller seller);
} 