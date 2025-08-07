package org.example.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_data")
public class AnalyticsData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;
    
    @Column(name = "period_start")
    private LocalDate periodStart;
    
    @Column(name = "period_end")
    private LocalDate periodEnd;
    
    @Column(name = "period_type")
    private String periodType; // "WEEK", "MONTH"
    
    // Основные метрики из Excel таблицы
    @Column(name = "orders_count")
    private Integer ordersCount = 0;
    
    @Column(name = "purchases_count")
    private Integer purchasesCount = 0;
    
    @Column(name = "wb_sales_amount", precision = 19, scale = 2)
    private BigDecimal wbSalesAmount = BigDecimal.ZERO;
    
    @Column(name = "to_pay_amount", precision = 19, scale = 2)
    private BigDecimal toPayAmount = BigDecimal.ZERO;
    
    @Column(name = "to_transfer_for_goods", precision = 19, scale = 2)
    private BigDecimal toTransferForGoods = BigDecimal.ZERO;
    
    @Column(name = "logistics_cost", precision = 19, scale = 2)
    private BigDecimal logisticsCost = BigDecimal.ZERO;
    
    @Column(name = "commission_wb", precision = 19, scale = 2)
    private BigDecimal commissionWb = BigDecimal.ZERO;
    
    @Column(name = "penalty_amount", precision = 19, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;
    
    @Column(name = "additional_payment", precision = 19, scale = 2)
    private BigDecimal additionalPayment = BigDecimal.ZERO;
    
    @Column(name = "storage_cost", precision = 19, scale = 2)
    private BigDecimal storageCost = BigDecimal.ZERO;
    
    @Column(name = "deduction_amount", precision = 19, scale = 2)
    private BigDecimal deductionAmount = BigDecimal.ZERO;
    
    @Column(name = "acceptance_cost", precision = 19, scale = 2)
    private BigDecimal acceptanceCost = BigDecimal.ZERO;
    
    // Дополнительные метрики
    @Column(name = "buyouts_count")
    private Integer buyoutsCount = 0;
    
    @Column(name = "buyouts_amount", precision = 19, scale = 2)
    private BigDecimal buyoutsAmount = BigDecimal.ZERO;
    
    @Column(name = "to_cart_count")
    private Integer toCartCount = 0;
    
    @Column(name = "views_count")
    private Integer viewsCount = 0;
    
    @Column(name = "sales_amount", precision = 19, scale = 2)
    private BigDecimal salesAmount = BigDecimal.ZERO;
    
    // Рассчитываемые поля
    @Column(name = "net_profit", precision = 19, scale = 2)
    private BigDecimal netProfit = BigDecimal.ZERO;
    
    @Column(name = "profit_margin", precision = 5, scale = 2)
    private BigDecimal profitMargin = BigDecimal.ZERO;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Конструкторы
    public AnalyticsData() {}
    
    public AnalyticsData(User user, LocalDate periodStart, LocalDate periodEnd, String periodType) {
        this.user = user;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.periodType = periodType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public AnalyticsData(User user, Seller seller, LocalDate periodStart, LocalDate periodEnd, String periodType) {
        this.user = user;
        this.seller = seller;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.periodType = periodType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Seller getSeller() { return seller; }
    public void setSeller(Seller seller) { this.seller = seller; }
    
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    
    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }
    
    public Integer getOrdersCount() { return ordersCount; }
    public void setOrdersCount(Integer ordersCount) { this.ordersCount = ordersCount; }
    
    public Integer getPurchasesCount() { return purchasesCount; }
    public void setPurchasesCount(Integer purchasesCount) { this.purchasesCount = purchasesCount; }
    
    public BigDecimal getWbSalesAmount() { return wbSalesAmount; }
    public void setWbSalesAmount(BigDecimal wbSalesAmount) { this.wbSalesAmount = wbSalesAmount; }
    
    public BigDecimal getToPayAmount() { return toPayAmount; }
    public void setToPayAmount(BigDecimal toPayAmount) { this.toPayAmount = toPayAmount; }
    
    public BigDecimal getToTransferForGoods() { return toTransferForGoods; }
    public void setToTransferForGoods(BigDecimal toTransferForGoods) { this.toTransferForGoods = toTransferForGoods; }
    
    public BigDecimal getLogisticsCost() { return logisticsCost; }
    public void setLogisticsCost(BigDecimal logisticsCost) { this.logisticsCost = logisticsCost; }
    
    public BigDecimal getCommissionWb() { return commissionWb; }
    public void setCommissionWb(BigDecimal commissionWb) { this.commissionWb = commissionWb; }
    
    public BigDecimal getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(BigDecimal penaltyAmount) { this.penaltyAmount = penaltyAmount; }
    
    public BigDecimal getAdditionalPayment() { return additionalPayment; }
    public void setAdditionalPayment(BigDecimal additionalPayment) { this.additionalPayment = additionalPayment; }
    
    public BigDecimal getStorageCost() { return storageCost; }
    public void setStorageCost(BigDecimal storageCost) { this.storageCost = storageCost; }
    
    public BigDecimal getDeductionAmount() { return deductionAmount; }
    public void setDeductionAmount(BigDecimal deductionAmount) { this.deductionAmount = deductionAmount; }
    
    public BigDecimal getAcceptanceCost() { return acceptanceCost; }
    public void setAcceptanceCost(BigDecimal acceptanceCost) { this.acceptanceCost = acceptanceCost; }
    
    public BigDecimal getNetProfit() { return netProfit; }
    public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }
    
    public BigDecimal getProfitMargin() { return profitMargin; }
    public void setProfitMargin(BigDecimal profitMargin) { this.profitMargin = profitMargin; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Дополнительные геттеры и сеттеры
    public Integer getBuyoutsCount() { return buyoutsCount; }
    public void setBuyoutsCount(Integer buyoutsCount) { this.buyoutsCount = buyoutsCount; }
    
    public BigDecimal getBuyoutsAmount() { return buyoutsAmount; }
    public void setBuyoutsAmount(BigDecimal buyoutsAmount) { this.buyoutsAmount = buyoutsAmount; }
    
    public Integer getToCartCount() { return toCartCount; }
    public void setToCartCount(Integer toCartCount) { this.toCartCount = toCartCount; }
    
    public Integer getViewsCount() { return viewsCount; }
    public void setViewsCount(Integer viewsCount) { this.viewsCount = viewsCount; }
    
    public BigDecimal getSalesAmount() { return salesAmount; }
    public void setSalesAmount(BigDecimal salesAmount) { this.salesAmount = salesAmount; }
    
    // Дополнительные методы для совместимости с API
    public void setSoldQuantity(int soldQuantity) { 
        this.purchasesCount = soldQuantity; 
    }
    
    public int getSoldQuantity() { 
        return this.purchasesCount != null ? this.purchasesCount : 0; 
    }
    
    public void setPrice(BigDecimal price) {
        // Можно использовать для средней цены или другой логики
        this.salesAmount = price;
    }
    
    public BigDecimal getPrice() {
        return this.salesAmount;
    }
} 