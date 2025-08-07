package org.example.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "advertising_campaigns")
public class AdvertisingCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // ТАБЛИЦА 4: РК ТАБЛИЦА - Точные колонки согласно спецификации

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber; // №

    @Column(name = "wb_article", length = 100)
    private String wbArticle; // Артикул вб

    @Column(name = "supplier_article", length = 100)
    private String supplierArticle; // Артикул продавца

    @Column(name = "grouping", length = 200)
    private String grouping; // Склейка

    @Column(name = "indicator", length = 100)
    private String indicator; // Показатель

    // Недельные показатели (динамические даты)
    @Column(name = "week_1_period", length = 50)
    private String week1Period; // 05.05-11.05

    @Column(name = "week_1_value", precision = 12, scale = 2)
    private BigDecimal week1Value;

    @Column(name = "week_2_period", length = 50)
    private String week2Period; // 12.05-18.05

    @Column(name = "week_2_value", precision = 12, scale = 2)
    private BigDecimal week2Value;

    @Column(name = "week_3_period", length = 50)
    private String week3Period; // 19.05-25.05

    @Column(name = "week_3_value", precision = 12, scale = 2)
    private BigDecimal week3Value;

    @Column(name = "week_4_period", length = 50)
    private String week4Period; // 26.05-01.06

    @Column(name = "week_4_value", precision = 12, scale = 2)
    private BigDecimal week4Value;

    @Column(name = "week_5_period", length = 50)
    private String week5Period; // 02.06-08.06 (и тд)

    @Column(name = "week_5_value", precision = 12, scale = 2)
    private BigDecimal week5Value;

    @Column(name = "calculation", precision = 12, scale = 2)
    private BigDecimal calculation; // Расчет

    // Метаданные
    @Column(name = "report_period_start", nullable = false)
    private LocalDate reportPeriodStart;

    @Column(name = "report_period_end", nullable = false)
    private LocalDate reportPeriodEnd;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Конструкторы
    public AdvertisingCampaign() {
        this.createdAt = LocalDateTime.now();
    }

    public AdvertisingCampaign(Seller seller, String wbArticle, String supplierArticle, String indicator) {
        this();
        this.seller = seller;
        this.wbArticle = wbArticle;
        this.supplierArticle = supplierArticle;
        this.indicator = indicator;
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

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public String getWeek1Period() {
        return week1Period;
    }

    public void setWeek1Period(String week1Period) {
        this.week1Period = week1Period;
    }

    public BigDecimal getWeek1Value() {
        return week1Value;
    }

    public void setWeek1Value(BigDecimal week1Value) {
        this.week1Value = week1Value;
    }

    public String getWeek2Period() {
        return week2Period;
    }

    public void setWeek2Period(String week2Period) {
        this.week2Period = week2Period;
    }

    public BigDecimal getWeek2Value() {
        return week2Value;
    }

    public void setWeek2Value(BigDecimal week2Value) {
        this.week2Value = week2Value;
    }

    public String getWeek3Period() {
        return week3Period;
    }

    public void setWeek3Period(String week3Period) {
        this.week3Period = week3Period;
    }

    public BigDecimal getWeek3Value() {
        return week3Value;
    }

    public void setWeek3Value(BigDecimal week3Value) {
        this.week3Value = week3Value;
    }

    public String getWeek4Period() {
        return week4Period;
    }

    public void setWeek4Period(String week4Period) {
        this.week4Period = week4Period;
    }

    public BigDecimal getWeek4Value() {
        return week4Value;
    }

    public void setWeek4Value(BigDecimal week4Value) {
        this.week4Value = week4Value;
    }

    public String getWeek5Period() {
        return week5Period;
    }

    public void setWeek5Period(String week5Period) {
        this.week5Period = week5Period;
    }

    public BigDecimal getWeek5Value() {
        return week5Value;
    }

    public void setWeek5Value(BigDecimal week5Value) {
        this.week5Value = week5Value;
    }

    public BigDecimal getCalculation() {
        return calculation;
    }

    public void setCalculation(BigDecimal calculation) {
        this.calculation = calculation;
    }

    public LocalDate getReportPeriodStart() {
        return reportPeriodStart;
    }

    public void setReportPeriodStart(LocalDate reportPeriodStart) {
        this.reportPeriodStart = reportPeriodStart;
    }

    public LocalDate getReportPeriodEnd() {
        return reportPeriodEnd;
    }

    public void setReportPeriodEnd(LocalDate reportPeriodEnd) {
        this.reportPeriodEnd = reportPeriodEnd;
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