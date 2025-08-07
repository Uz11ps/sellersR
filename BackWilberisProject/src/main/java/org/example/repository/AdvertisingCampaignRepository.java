package org.example.repository;

import org.example.entity.Seller;
import org.example.entity.AdvertisingCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdvertisingCampaignRepository extends JpaRepository<AdvertisingCampaign, Long> {
    
    List<AdvertisingCampaign> findBySeller(Seller seller);
    
    List<AdvertisingCampaign> findBySellerOrderByReportPeriodStartDesc(Seller seller);
    
    Optional<AdvertisingCampaign> findBySellerAndWbArticleAndIndicator(Seller seller, String wbArticle, String indicator);
    
    @Query("SELECT a FROM AdvertisingCampaign a WHERE a.seller = :seller AND a.reportPeriodStart >= :fromDate AND a.reportPeriodEnd <= :toDate")
    List<AdvertisingCampaign> findBySellerAndDateRange(@Param("seller") Seller seller,
                                                      @Param("fromDate") LocalDate fromDate,
                                                      @Param("toDate") LocalDate toDate);
    
    @Query("SELECT COUNT(a) FROM AdvertisingCampaign a WHERE a.seller = :seller")
    long countBySeller(@Param("seller") Seller seller);
    
    @Query("SELECT SUM(a.calculation) FROM AdvertisingCampaign a WHERE a.seller = :seller")
    java.math.BigDecimal getTotalCalculationBySeller(@Param("seller") Seller seller);
} 