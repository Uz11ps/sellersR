package org.example.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wildberries_card_data")
public class WildberriesCardData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    // Основные данные карточки WB
    @Column(name = "wb_article", length = 100, nullable = false)
    private String wbArticle; // Артикул ВБ

    @Column(name = "supplier_article", length = 100)
    private String supplierArticle; // Артикул продавца

    @Column(name = "product_name", length = 500)
    private String productName; // Название товара

    @Column(name = "brand", length = 200)
    private String brand; // Бренд

    @Column(name = "category", length = 200)
    private String category; // Категория

    @Column(name = "subject", length = 200)
    private String subject; // Предмет

    // Склейка - ключевое поле из столбца W
    @Column(name = "cluster_group", length = 100)
    private String clusterGroup; // Склейка (группа товаров)

    // Характеристики товара
    @Column(name = "size", length = 100)
    private String size; // Размер

    @Column(name = "color", length = 100)
    private String color; // Цвет

    @Column(name = "barcode", length = 50)
    private String barcode; // Штрихкод

    // Статус карточки
    @Column(name = "status", length = 50)
    private String status; // Статус карточки

    @Column(name = "is_active")
    private Boolean isActive; // Активна ли карточка

    // Габариты
    @Column(name = "length_cm", precision = 8, scale = 2)
    private BigDecimal lengthCm;

    @Column(name = "width_cm", precision = 8, scale = 2)
    private BigDecimal widthCm;

    @Column(name = "height_cm", precision = 8, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_g", precision = 8, scale = 2)
    private BigDecimal weightG;

    // Метаданные
    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate; // Дата загрузки из WB

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Конструкторы
    public WildberriesCardData() {
        this.createdAt = LocalDateTime.now();
        this.uploadDate = LocalDateTime.now();
        this.isActive = true;
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

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getClusterGroup() { return clusterGroup; }
    public void setClusterGroup(String clusterGroup) { this.clusterGroup = clusterGroup; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public BigDecimal getLengthCm() { return lengthCm; }
    public void setLengthCm(BigDecimal lengthCm) { this.lengthCm = lengthCm; }

    public BigDecimal getWidthCm() { return widthCm; }
    public void setWidthCm(BigDecimal widthCm) { this.widthCm = widthCm; }

    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }

    public BigDecimal getWeightG() { return weightG; }
    public void setWeightG(BigDecimal weightG) { this.weightG = weightG; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 