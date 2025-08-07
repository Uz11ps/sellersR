package org.example.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_report_raw")
public class FinancialReportRaw {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    // Основные поля отчета WB
    @Column(name = "wb_article", length = 100)
    private String wbArticle; // Артикул ВБ

    @Column(name = "supplier_article", length = 100)
    private String supplierArticle; // Артикул продавца

    @Column(name = "operation_type", length = 50)
    private String operationType; // Тип операции (продажа, возврат, и т.д.) - столбец J

    @Column(name = "operation_date")
    private LocalDateTime operationDate; // Дата операции

    @Column(name = "sale_amount", precision = 12, scale = 2)
    private BigDecimal saleAmount; // Сумма продажи

    @Column(name = "commission_percent", precision = 5, scale = 2)
    private BigDecimal commissionPercent; // Процент комиссии

    @Column(name = "commission_amount", precision = 12, scale = 2)
    private BigDecimal commissionAmount; // Сумма комиссии

    @Column(name = "logistics_cost", precision = 12, scale = 2)
    private BigDecimal logisticsCost; // Стоимость логистики

    @Column(name = "storage_cost", precision = 12, scale = 2)
    private BigDecimal storageCost; // Стоимость хранения

    @Column(name = "other_deductions", precision = 12, scale = 2)
    private BigDecimal otherDeductions; // Прочие удержания

    @Column(name = "to_pay_amount", precision = 12, scale = 2)
    private BigDecimal toPayAmount; // К доплате (столбец AH)

    @Column(name = "quantity", precision = 8, scale = 0)
    private BigDecimal quantity; // Количество

    // ФОРМУЛА ВОЗВРАТОВ (столбец BP): =ЕСЛИ(J:J="возврат";AH2;0)*2
    @Column(name = "return_payment_calculated", precision = 12, scale = 2)
    private BigDecimal returnPaymentCalculated; // Рассчитанная сумма возврата

    // Дополнительные поля для расчетов
    @Column(name = "week_number")
    private Integer weekNumber; // Номер недели

    @Column(name = "is_return_operation")
    private Boolean isReturnOperation; // Является ли операция возвратом

    @Column(name = "penalty_amount", precision = 12, scale = 2)
    private BigDecimal penaltyAmount; // Штрафы

    @Column(name = "bonus_amount", precision = 12, scale = 2)
    private BigDecimal bonusAmount; // Бонусы

    // Метаданные
    @Column(name = "report_date", nullable = false)
    private LocalDateTime reportDate; // Дата отчета

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Конструкторы
    public FinancialReportRaw() {
        this.createdAt = LocalDateTime.now();
        this.reportDate = LocalDateTime.now();
        this.isReturnOperation = false;
    }

    // Метод для расчета возвратов по формуле Excel
    public void calculateReturnPayment() {
        // Формула: =ЕСЛИ(J:J="возврат";AH2;0)*2
        if ("возврат".equalsIgnoreCase(this.operationType) && this.toPayAmount != null) {
            this.returnPaymentCalculated = this.toPayAmount.multiply(BigDecimal.valueOf(2));
            this.isReturnOperation = true;
        } else {
            this.returnPaymentCalculated = BigDecimal.ZERO;
            this.isReturnOperation = false;
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

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { 
        this.operationType = operationType;
        calculateReturnPayment(); // Автоматический пересчет
    }

    public LocalDateTime getOperationDate() { return operationDate; }
    public void setOperationDate(LocalDateTime operationDate) { this.operationDate = operationDate; }

    public BigDecimal getSaleAmount() { return saleAmount; }
    public void setSaleAmount(BigDecimal saleAmount) { this.saleAmount = saleAmount; }

    public BigDecimal getCommissionPercent() { return commissionPercent; }
    public void setCommissionPercent(BigDecimal commissionPercent) { this.commissionPercent = commissionPercent; }

    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }

    public BigDecimal getLogisticsCost() { return logisticsCost; }
    public void setLogisticsCost(BigDecimal logisticsCost) { this.logisticsCost = logisticsCost; }

    public BigDecimal getStorageCost() { return storageCost; }
    public void setStorageCost(BigDecimal storageCost) { this.storageCost = storageCost; }

    public BigDecimal getOtherDeductions() { return otherDeductions; }
    public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }

    public BigDecimal getToPayAmount() { return toPayAmount; }
    public void setToPayAmount(BigDecimal toPayAmount) { 
        this.toPayAmount = toPayAmount;
        calculateReturnPayment(); // Автоматический пересчет
    }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getReturnPaymentCalculated() { return returnPaymentCalculated; }
    public void setReturnPaymentCalculated(BigDecimal returnPaymentCalculated) { this.returnPaymentCalculated = returnPaymentCalculated; }

    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }

    public Boolean getIsReturnOperation() { return isReturnOperation; }
    public void setIsReturnOperation(Boolean isReturnOperation) { this.isReturnOperation = isReturnOperation; }

    public BigDecimal getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(BigDecimal penaltyAmount) { this.penaltyAmount = penaltyAmount; }

    public BigDecimal getBonusAmount() { return bonusAmount; }
    public void setBonusAmount(BigDecimal bonusAmount) { this.bonusAmount = bonusAmount; }

    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateReturnPayment();
    }

    @PrePersist
    public void prePersist() {
        calculateReturnPayment();
    }
} 