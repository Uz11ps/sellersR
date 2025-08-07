package org.example.repository;

import org.example.entity.Seller;
import org.example.entity.PromotionsTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionsTrackingRepository extends JpaRepository<PromotionsTracking, Long> {
    
    List<PromotionsTracking> findBySeller(Seller seller);
    
    List<PromotionsTracking> findBySellerOrderByCreatedAtDesc(Seller seller);
    
    Optional<PromotionsTracking> findBySellerAndWbArticle(Seller seller, String wbArticle);
    
    List<PromotionsTracking> findBySellerAndAbcAnalysis(Seller seller, String abcAnalysis);
    
    List<PromotionsTracking> findBySellerAndSubgroupFPreparationDSale(Seller seller, String subgroupFPreparationDSale);
    
    @Query("SELECT COUNT(p) FROM PromotionsTracking p WHERE p.seller = :seller")
    long countBySeller(@Param("seller") Seller seller);
    
    @Query("SELECT p FROM PromotionsTracking p WHERE p.seller = :seller AND p.grossProfit > 0 ORDER BY p.grossProfit DESC")
    List<PromotionsTracking> findProfitablePromotions(@Param("seller") Seller seller);
} 