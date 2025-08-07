package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sellers")
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "seller_name", nullable = false, length = 255)
    private String sellerName; // Название ИП/ООО

    @Column(name = "inn", length = 20)
    private String inn; // ИНН

    @Column(name = "wb_api_key", length = 1000)
    private String wbApiKey; // API ключ Wildberries для этого продавца

    @Column(name = "wb_seller_id")
    private Long wbSellerId; // ID продавца в системе WB

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt; // Последняя синхронизация с WB API

    // Связи с аналитическими данными
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AnalyticsData> analyticsData;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;

    // Конструкторы
    public Seller() {
        this.createdAt = LocalDateTime.now();
    }

    public Seller(User user, String sellerName, String inn, String wbApiKey) {
        this();
        this.user = user;
        this.sellerName = sellerName;
        this.inn = inn;
        this.wbApiKey = wbApiKey;
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

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getWbApiKey() {
        return wbApiKey;
    }

    public void setWbApiKey(String wbApiKey) {
        this.wbApiKey = wbApiKey;
    }

    public Long getWbSellerId() {
        return wbSellerId;
    }

    public void setWbSellerId(Long wbSellerId) {
        this.wbSellerId = wbSellerId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public List<AnalyticsData> getAnalyticsData() {
        return analyticsData;
    }

    public void setAnalyticsData(List<AnalyticsData> analyticsData) {
        this.analyticsData = analyticsData;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Seller{" +
                "id=" + id +
                ", sellerName='" + sellerName + '\'' +
                ", inn='" + inn + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
} 