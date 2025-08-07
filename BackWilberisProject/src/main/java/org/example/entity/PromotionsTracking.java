package org.example.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions_tracking")
public class PromotionsTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // ТАБЛИЦА 3: УЧЕТ АКЦИЙ - Точные колонки согласно спецификации

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber; // №

    @Column(name = "wb_article", length = 100)
    private String wbArticle; // Артикул ВБ

    @Column(name = "supplier_article", length = 100)
    private String supplierArticle; // Артикул поставщика

    @Column(name = "grouping", length = 200)
    private String grouping; // Склейка

    @Column(name = "abc_analysis", length = 50)
    private String abcAnalysis; // АВС Анализ

    @Column(name = "subgroup_f_preparation_d_sale", length = 100)
    private String subgroupFPreparationDSale; // подгруппа F подготовка D распродажа

    @Column(name = "gross_profit", precision = 12, scale = 2)
    private BigDecimal grossProfit; // Валовая прибыль

    @Column(name = "current_price", precision = 12, scale = 2)
    private BigDecimal currentPrice; // Текщая цена

    @Column(name = "action", length = 200)
    private String action; // Действие

    @Column(name = "price_for_promotion_participation", precision = 12, scale = 2)
    private BigDecimal priceForPromotionParticipation; // Цена для участия в акции

    @Column(name = "gross_profit_in_promotion", precision = 12, scale = 2)
    private BigDecimal grossProfitInPromotion; // Валовая прибыль в акции

    @Column(name = "turnover_days", precision = 8, scale = 2)
    private BigDecimal turnoverDays; // Оборачиваемость

    @Column(name = "wb_stock_balance", precision = 10, scale = 0)
    private BigDecimal wbStockBalance; // Остатки ВБ

    // Метаданные
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Конструкторы
    public PromotionsTracking() {
        this.createdAt = LocalDateTime.now();
    }

    public PromotionsTracking(Seller seller, String wbArticle, String supplierArticle) {
        this();
        this.seller = seller;
        this.wbArticle = wbArticle;
        this.supplierArticle = supplierArticle;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getWbArticle() {
        return wbArticle;
    }

    public void setWbArticle(String wbArticle) {
        this.wbArticle = wbArticle;
    }

    public String getSupplierArticle() {
        return supplierArticle;
    }

    public void setSupplierArticle(String supplierArticle) {
        this.supplierArticle = supplierArticle;
    }

    public String getGrouping() {
        return grouping;
    }

    public void setGrouping(String grouping) {
        this.grouping = grouping;
    }

    public String getAbcAnalysis() {
        return abcAnalysis;
    }

    public void setAbcAnalysis(String abcAnalysis) {
        this.abcAnalysis = abcAnalysis;
    }

    public String getSubgroupFPreparationDSale() {
        return subgroupFPreparationDSale;
    }

    public void setSubgroupFPreparationDSale(String subgroupFPreparationDSale) {
        this.subgroupFPreparationDSale = subgroupFPreparationDSale;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public BigDecimal getPriceForPromotionParticipation() {
        return priceForPromotionParticipation;
    }

    public void setPriceForPromotionParticipation(BigDecimal priceForPromotionParticipation) {
        this.priceForPromotionParticipation = priceForPromotionParticipation;
    }

    public BigDecimal getGrossProfitInPromotion() {
        return grossProfitInPromotion;
    }

    public void setGrossProfitInPromotion(BigDecimal grossProfitInPromotion) {
        this.grossProfitInPromotion = grossProfitInPromotion;
    }

    public BigDecimal getTurnoverDays() {
        return turnoverDays;
    }

    public void setTurnoverDays(BigDecimal turnoverDays) {
        this.turnoverDays = turnoverDays;
    }

    public BigDecimal getWbStockBalance() {
        return wbStockBalance;
    }

    public void setWbStockBalance(BigDecimal wbStockBalance) {
        this.wbStockBalance = wbStockBalance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 