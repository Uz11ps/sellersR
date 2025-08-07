package org.example.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "supply_planning")
public class SupplyPlanning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // ТАБЛИЦА 5: ПЛАН ПОСТАВОК - Точные колонки согласно спецификации

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber; // №

    @Column(name = "wb_article", length = 100)
    private String wbArticle; // Артикул вб

    @Column(name = "supplier_article", length = 100)
    private String supplierArticle; // Артикул продавца

    @Column(name = "goods_in_transit_quantity", precision = 10, scale = 0)
    private BigDecimal goodsInTransitQuantity; // Кол-во товара в пути

    @Column(name = "goods_on_sale_quantity", precision = 10, scale = 0)
    private BigDecimal goodsOnSaleQuantity; // Количество товара на продаже

    @Column(name = "total_stock_balance", precision = 10, scale = 0)
    private BigDecimal totalStockBalance; // Общий остаток

    @Column(name = "average_orders_per_day", precision = 8, scale = 2)
    private BigDecimal averageOrdersPerDay; // Среднее количество заказов в день

    @Column(name = "turnover_days", precision = 8, scale = 2)
    private BigDecimal turnoverDays; // Оборачиваемость (дней)

    @Column(name = "coverage_plan_30_days", precision = 10, scale = 0)
    private BigDecimal coveragePlan30Days; // План покрытия (30 дней)

    @Column(name = "demand_for_30_days", precision = 10, scale = 0)
    private BigDecimal demandFor30Days; // Потребность на 30 дней

    @Column(name = "seasonality_coefficient", precision = 5, scale = 2)
    private BigDecimal seasonalityCoefficient; // Коэффициент сезонности

    @Column(name = "demand_for_30_days_with_seasonality", precision = 10, scale = 0)
    private BigDecimal demandFor30DaysWithSeasonality; // Потребность на 30 дней с учетом сезонности

    // Метаданные
    @Column(name = "calculation_date", nullable = false)
    private LocalDateTime calculationDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Конструкторы
    public SupplyPlanning() {
        this.createdAt = LocalDateTime.now();
        this.calculationDate = LocalDateTime.now();
    }

    public SupplyPlanning(Seller seller, String wbArticle, String supplierArticle) {
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

    public BigDecimal getGoodsInTransitQuantity() {
        return goodsInTransitQuantity;
    }

    public void setGoodsInTransitQuantity(BigDecimal goodsInTransitQuantity) {
        this.goodsInTransitQuantity = goodsInTransitQuantity;
    }

    public BigDecimal getGoodsOnSaleQuantity() {
        return goodsOnSaleQuantity;
    }

    public void setGoodsOnSaleQuantity(BigDecimal goodsOnSaleQuantity) {
        this.goodsOnSaleQuantity = goodsOnSaleQuantity;
    }

    public BigDecimal getTotalStockBalance() {
        return totalStockBalance;
    }

    public void setTotalStockBalance(BigDecimal totalStockBalance) {
        this.totalStockBalance = totalStockBalance;
    }

    public BigDecimal getAverageOrdersPerDay() {
        return averageOrdersPerDay;
    }

    public void setAverageOrdersPerDay(BigDecimal averageOrdersPerDay) {
        this.averageOrdersPerDay = averageOrdersPerDay;
    }

    public BigDecimal getTurnoverDays() {
        return turnoverDays;
    }

    public void setTurnoverDays(BigDecimal turnoverDays) {
        this.turnoverDays = turnoverDays;
    }

    public BigDecimal getCoveragePlan30Days() {
        return coveragePlan30Days;
    }

    public void setCoveragePlan30Days(BigDecimal coveragePlan30Days) {
        this.coveragePlan30Days = coveragePlan30Days;
    }

    public BigDecimal getDemandFor30Days() {
        return demandFor30Days;
    }

    public void setDemandFor30Days(BigDecimal demandFor30Days) {
        this.demandFor30Days = demandFor30Days;
    }

    public BigDecimal getSeasonalityCoefficient() {
        return seasonalityCoefficient;
    }

    public void setSeasonalityCoefficient(BigDecimal seasonalityCoefficient) {
        this.seasonalityCoefficient = seasonalityCoefficient;
    }

    public BigDecimal getDemandFor30DaysWithSeasonality() {
        return demandFor30DaysWithSeasonality;
    }

    public void setDemandFor30DaysWithSeasonality(BigDecimal demandFor30DaysWithSeasonality) {
        this.demandFor30DaysWithSeasonality = demandFor30DaysWithSeasonality;
    }

    public LocalDateTime getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(LocalDateTime calculationDate) {
        this.calculationDate = calculationDate;
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