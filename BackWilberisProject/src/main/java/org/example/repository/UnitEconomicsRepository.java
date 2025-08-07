package org.example.repository;

import org.example.entity.Seller;
import org.example.entity.UnitEconomics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UnitEconomicsRepository extends JpaRepository<UnitEconomics, Long> {
    
    List<UnitEconomics> findBySeller(Seller seller);
    
    List<UnitEconomics> findBySellerOrderByCalculationDateDesc(Seller seller);
    
    Optional<UnitEconomics> findBySellerAndWbArticle(Seller seller, String wbArticle);
    
    @Query("SELECT u FROM UnitEconomics u WHERE u.seller = :seller AND u.calculationDate >= :fromDate")
    List<UnitEconomics> findBySellerAndCalculationDateAfter(@Param("seller") Seller seller, 
                                                            @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(u) FROM UnitEconomics u WHERE u.seller = :seller")
    long countBySeller(@Param("seller") Seller seller);
    
    @Query("SELECT u FROM UnitEconomics u WHERE u.seller = :seller AND u.roi >= :minRoi ORDER BY u.roi DESC")
    List<UnitEconomics> findBySellerWithMinRoi(@Param("seller") Seller seller, 
                                              @Param("minRoi") java.math.BigDecimal minRoi);
    
    void deleteBySellerAndWbArticle(Seller seller, String wbArticle);
} 