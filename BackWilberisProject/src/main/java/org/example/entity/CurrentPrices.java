package org.example.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "current_prices")
public class CurrentPrices {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(name = "wb_article", length = 100, nullable = false)
    private String wbArticle; // Артикул ВБ

    @Column(name = "supplier_article", length = 100)
    private String supplierArticle; // Артикул продавца

    // Цены от WB
    @Column(name = "wb_price", precision = 12, scale = 2)
    private BigDecimal wbPrice; // Цена от WB

    @Column(name = "our_set_price", precision = 12, scale = 2)
    private BigDecimal ourSetPrice; // Цена которую мы поставили

    @Column(name = "our_discount_percent", precision = 5, scale = 2)
    private BigDecimal ourDiscountPercent; // Процент скидки которую мы поставили

    // ФОРМУЛА: ourSetPrice - (ourSetPrice * ourDiscountPercent / 100)
    @Column(name = "final_price", precision = 12, scale = 2)
    private BigDecimal finalPrice; // Конечная цена (столбец N в Excel)

    @Column(name = "competitor_price", precision = 12, scale = 2)
    private BigDecimal competitorPrice; // Цена конкурентов

    @Column(name = "min_price", precision = 12, scale = 2)
    private BigDecimal minPrice; // Минимальная цена

    @Column(name = "max_price", precision = 12, scale = 2)
    private BigDecimal maxPrice; // Максимальная цена

    @Column(name = "recommended_price", precision = 12, scale = 2)
    private BigDecimal recommendedPrice; // Рекомендованная цена

    // Статус цены
    @Column(name = "price_status", length = 50)
    private String priceStatus; // Статус цены (активна, на модерации и т.д.)

    @Column(name = "is_promotion_active")
    private Boolean isPromotionActive; // Активна ли акция

    // Метаданные
    @Column(name = "price_update_date")
    private LocalDateTime priceUpdateDate; // Дата обновления цены

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Конструкторы
    public CurrentPrices() {
        this.createdAt = LocalDateTime.now();
        this.priceUpdateDate = LocalDateTime.now();
        this.isPromotionActive = false;
    }

    // Метод для автоматического расчета конечной цены
    public void calculateFinalPrice() {
        if (ourSetPrice != null && ourDiscountPercent != null) {
            // Формула: Цена которую мы поставили - (Цена которую мы поставили * процент скидки / 100)
            BigDecimal discountAmount = ourSetPrice.multiply(ourDiscountPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            this.finalPrice = ourSetPrice.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        }
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Seller getSeller() { return seller; }
    public void setSeller(Seller seller) { this.seller = seller; }

    public String getWbArticle() { return wbArticle; }
    public void setWbArticle(String wbArticle) { this.wbArticle = wbArticle; }

    public String getSupplierArticle() { return supplierArticle; }
    public void setSupplierArticle(String supplierArticle) { this.supplierArticle = supplierArticle; }

    public BigDecimal getWbPrice() { return wbPrice; }
    public void setWbPrice(BigDecimal wbPrice) { this.wbPrice = wbPrice; }

    public BigDecimal getOurSetPrice() { return ourSetPrice; }
    public void setOurSetPrice(BigDecimal ourSetPrice) { 
        this.ourSetPrice = ourSetPrice;
        calculateFinalPrice(); // Автоматический пересчет при изменении
    }

    public BigDecimal getOurDiscountPercent() { return ourDiscountPercent; }
    public void setOurDiscountPercent(BigDecimal ourDiscountPercent) { 
        this.ourDiscountPercent = ourDiscountPercent;
        calculateFinalPrice(); // Автоматический пересчет при изменении
    }

    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }

    public BigDecimal getCompetitorPrice() { return competitorPrice; }
    public void setCompetitorPrice(BigDecimal competitorPrice) { this.competitorPrice = competitorPrice; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public BigDecimal getRecommendedPrice() { return recommendedPrice; }
    public void setRecommendedPrice(BigDecimal recommendedPrice) { this.recommendedPrice = recommendedPrice; }

    public String getPriceStatus() { return priceStatus; }
    public void setPriceStatus(String priceStatus) { this.priceStatus = priceStatus; }

    public Boolean getIsPromotionActive() { return isPromotionActive; }
    public void setIsPromotionActive(Boolean isPromotionActive) { this.isPromotionActive = isPromotionActive; }

    public LocalDateTime getPriceUpdateDate() { return priceUpdateDate; }
    public void setPriceUpdateDate(LocalDateTime priceUpdateDate) { this.priceUpdateDate = priceUpdateDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateFinalPrice(); // Пересчет при обновлении
    }

    @PrePersist
    public void prePersist() {
        calculateFinalPrice(); // Пересчет перед сохранением
    }
} 