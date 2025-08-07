package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "plan_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlanType planType;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status = SubscriptionStatus.PENDING;
    
    @Column(name = "price", nullable = false)
    private Double price;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew = false;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "payment_transaction_id")
    private String paymentTransactionId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Перечисления для типов планов и статусов подписки
    public enum PlanType {
        PLAN_30_DAYS,
        PLAN_60_DAYS,
        PLAN_90_DAYS,
        PLAN_FREE // Добавляем бесплатный план
    }
    
    public enum SubscriptionStatus {
        ACTIVE,
        EXPIRED,
        CANCELLED,
        PENDING
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Устанавливаем статус ACTIVE при создании, если не указан другой
        if (this.status == null) {
            this.status = SubscriptionStatus.ACTIVE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Геттеры и сеттеры
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public PlanType getPlanType() {
        return planType;
    }
    
    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }
    
    public SubscriptionStatus getStatus() {
        return status;
    }
    
    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public boolean isAutoRenew() {
        return autoRenew;
    }
    
    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }
    
    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
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
    
    // Вспомогательные методы
    
    public boolean isActive() {
        return this.status == SubscriptionStatus.ACTIVE;
    }
    
    // Для обратной совместимости с существующим кодом
    public String getPlanId() {
        return this.planType != null ? this.planType.name() : null;
    }
    
    public void setPlanId(String planId) {
        try {
            if (planId != null) {
                if (planId.equals("free")) {
                    this.planType = PlanType.PLAN_FREE;
                } else {
                    this.planType = PlanType.valueOf(planId);
                }
            }
        } catch (IllegalArgumentException e) {
            // Если не удалось преобразовать строку в enum, используем значение по умолчанию
            this.planType = PlanType.PLAN_30_DAYS;
        }
    }
    
    public void setActive(boolean active) {
        this.status = active ? SubscriptionStatus.ACTIVE : SubscriptionStatus.CANCELLED;
    }
    
    // Для обратной совместимости
    public boolean isTrialPeriod() {
        return this.planType == PlanType.PLAN_FREE;
    }
    
    public void setTrialPeriod(boolean trialPeriod) {
        if (trialPeriod) {
            this.planType = PlanType.PLAN_FREE;
        }
    }
} 