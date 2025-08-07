package org.example.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_financial_reports")
public class WeeklyFinancialReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // ТАБЛИЦА 2: ФИНАНСОВЫЙ ОТЧЕТ (по каждому типу товара) - Точные колонки согласно спецификации

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber; // Неделя

    @Column(name = "date_period", nullable = false)
    private LocalDate datePeriod; // Дата

    @Column(name = "buyout_quantity", precision = 10, scale = 0)
    private BigDecimal buyoutQuantity; // Выкуп ШТ

    @Column(name = "wb_sales", precision = 12, scale = 2)
    private BigDecimal wbSales; // Продажи ВБ

    @Column(name = "to_recalculation_for_goods", precision = 12, scale = 2)
    private BigDecimal toRecalculationForGoods; // К пречеслению за товар

    @Column(name = "logistics", precision = 12, scale = 2)
    private BigDecimal logistics; // Логистика

    @Column(name = "storage", precision = 12, scale = 2)
    private BigDecimal storage; // Хранение

    @Column(name = "acceptance", precision = 12, scale = 2)
    private BigDecimal acceptance; // Приемка

    @Column(name = "penalty", precision = 12, scale = 2)
    private BigDecimal penalty; // Штраф

    @Column(name = "deductions_advertising", precision = 12, scale = 2)
    private BigDecimal deductionsAdvertising; // Удержания/ реклама

    @Column(name = "to_payout", precision = 12, scale = 2)
    private BigDecimal toPayout; // К выплате

    @Column(name = "tax", precision = 12, scale = 2)
    private BigDecimal tax; // Налог

    @Column(name = "other_expenses", precision = 12, scale = 2)
    private BigDecimal otherExpenses; // Прочие расходы

    @Column(name = "cost_of_goods_sold", precision = 12, scale = 2)
    private BigDecimal costOfGoodsSold; // Себестоимость проданного товара

    @Column(name = "net_profit", precision = 12, scale = 2)
    private BigDecimal netProfit; // Чистая прибыль

    // Метаданные
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Конструкторы
    public WeeklyFinancialReport() {
        this.createdAt = LocalDateTime.now();
    }

    public WeeklyFinancialReport(Seller seller, Integer weekNumber, LocalDate datePeriod) {
        this();
        this.seller = seller;
        this.weekNumber = weekNumber;
        this.datePeriod = datePeriod;
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

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public LocalDate getDatePeriod() {
        return datePeriod;
    }

    public void setDatePeriod(LocalDate datePeriod) {
        this.datePeriod = datePeriod;
    }

    public BigDecimal getBuyoutQuantity() {
        return buyoutQuantity;
    }

    public void setBuyoutQuantity(BigDecimal buyoutQuantity) {
        this.buyoutQuantity = buyoutQuantity;
    }

    public BigDecimal getWbSales() {
        return wbSales;
    }

    public void setWbSales(BigDecimal wbSales) {
        this.wbSales = wbSales;
    }

    public BigDecimal getToRecalculationForGoods() {
        return toRecalculationForGoods;
    }

    public void setToRecalculationForGoods(BigDecimal toRecalculationForGoods) {
        this.toRecalculationForGoods = toRecalculationForGoods;
    }

    public BigDecimal getLogistics() {
        return logistics;
    }

    public void setLogistics(BigDecimal logistics) {
        this.logistics = logistics;
    }

    public BigDecimal getStorage() {
        return storage;
    }

    public void setStorage(BigDecimal storage) {
        this.storage = storage;
    }

    public BigDecimal getAcceptance() {
        return acceptance;
    }

    public void setAcceptance(BigDecimal acceptance) {
        this.acceptance = acceptance;
    }

    public BigDecimal getPenalty() {
        return penalty;
    }

    public void setPenalty(BigDecimal penalty) {
        this.penalty = penalty;
    }

    public BigDecimal getDeductionsAdvertising() {
        return deductionsAdvertising;
    }

    public void setDeductionsAdvertising(BigDecimal deductionsAdvertising) {
        this.deductionsAdvertising = deductionsAdvertising;
    }

    public BigDecimal getToPayout() {
        return toPayout;
    }

    public void setToPayout(BigDecimal toPayout) {
        this.toPayout = toPayout;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getOtherExpenses() {
        return otherExpenses;
    }

    public void setOtherExpenses(BigDecimal otherExpenses) {
        this.otherExpenses = otherExpenses;
    }

    public BigDecimal getCostOfGoodsSold() {
        return costOfGoodsSold;
    }

    public void setCostOfGoodsSold(BigDecimal costOfGoodsSold) {
        this.costOfGoodsSold = costOfGoodsSold;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
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