package org.example.repository;

import org.example.entity.Seller;
import org.example.entity.SupplyPlanning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplyPlanningRepository extends JpaRepository<SupplyPlanning, Long> {
    
    List<SupplyPlanning> findBySeller(Seller seller);
    
    List<SupplyPlanning> findBySellerOrderByCalculationDateDesc(Seller seller);
    
    Optional<SupplyPlanning> findBySellerAndWbArticle(Seller seller, String wbArticle);
    
    @Query("SELECT s FROM SupplyPlanning s WHERE s.seller = :seller AND s.calculationDate >= :fromDate")
    List<SupplyPlanning> findBySellerAndCalculationDateAfter(@Param("seller") Seller seller,
                                                            @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(s) FROM SupplyPlanning s WHERE s.seller = :seller")
    long countBySeller(@Param("seller") Seller seller);
    
    @Query("SELECT s FROM SupplyPlanning s WHERE s.seller = :seller AND s.turnoverDays <= :criticalDays ORDER BY s.turnoverDays ASC")
    List<SupplyPlanning> findCriticalStock(@Param("seller") Seller seller,
                                          @Param("criticalDays") java.math.BigDecimal criticalDays);
    
    @Query("SELECT SUM(s.demandFor30Days) FROM SupplyPlanning s WHERE s.seller = :seller")
    java.math.BigDecimal getTotalDemandFor30Days(@Param("seller") Seller seller);
} 