package org.example.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "unit_economics")
public class UnitEconomics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // ТАБЛИЦА 1: ЮНИТ-ЭКОНОМИКА WB - Точные колонки согласно спецификации
    
    @Column(name = "wb_article", length = 100)
    private String wbArticle; // Артикул ВБ

    @Column(name = "supplier_article", length = 100) 
    private String supplierArticle; // Артикул продавца

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice; // Себестоимость

    @Column(name = "delivery_to_wb", precision = 12, scale = 2)
    private BigDecimal deliveryToWb; // доставка до ВБ

    @Column(name = "gross_profit", precision = 12, scale = 2)
    private BigDecimal grossProfit; // Валовая прибыль

    @Column(name = "mp_price_before", precision = 12, scale = 2)
    private BigDecimal mpPriceBefore; // МП цена ДО

    @Column(name = "mp_discount", precision = 12, scale = 2)
    private BigDecimal mpDiscount; // МП скидка

    @Column(name = "price_before_spp", precision = 12, scale = 2)
    private BigDecimal priceBeforeSpp; // Цена до СПП

    @Column(name = "spp_percent", precision = 5, scale = 2)
    private BigDecimal sppPercent; // % СПП

    @Column(name = "price_after_spp", precision = 12, scale = 2)
    private BigDecimal priceAfterSpp; // Цена после СПП

    @Column(name = "break_even_point_before_spp", precision = 12, scale = 2)
    private BigDecimal breakEvenPointBeforeSpp; // Точка безубыточности до СПП

    @Column(name = "buyout", precision = 12, scale = 2)
    private BigDecimal buyout; // Выкуп

    @Column(name = "mp_commission_percent", precision = 5, scale = 2)
    private BigDecimal mpCommissionPercent; // Комиссия МП %

    @Column(name = "first_liter_delivery_cost", precision = 12, scale = 2)
    private BigDecimal firstLiterDeliveryCost; // Стоимость доставки первого литра

    @Column(name = "next_liter_delivery_cost", precision = 12, scale = 2)
    private BigDecimal nextLiterDeliveryCost; // Стоимость доставки каждого следующего литра

    @Column(name = "height", precision = 8, scale = 2)
    private BigDecimal height; // Высота

    @Column(name = "width", precision = 8, scale = 2)
    private BigDecimal width; // Ширина

    @Column(name = "length", precision = 8, scale = 2)
    private BigDecimal length; // длина

    @Column(name = "total_volume_liters", precision = 8, scale = 3)
    private BigDecimal totalVolumeLiters; // Общий обьем в литрах

    @Column(name = "warehouse_coefficient", precision = 5, scale = 2)
    private BigDecimal warehouseCoefficient; // Коэффициент склада

    @Column(name = "logistics_mp", precision = 12, scale = 2)
    private BigDecimal logisticsMp; // Логистика МП

    @Column(name = "logistics_with_buyout", precision = 12, scale = 2)
    private BigDecimal logisticsWithBuyout; // Логистика с учетом выкупа

    @Column(name = "final_logistics_with_index", precision = 12, scale = 2)
    private BigDecimal finalLogisticsWithIndex; // Итоговая с учетом индекса

    @Column(name = "storage_mp", precision = 12, scale = 2)
    private BigDecimal storageMp; // Хранение МП

    @Column(name = "mp_commission_rub", precision = 12, scale = 2)
    private BigDecimal mpCommissionRub; // Комиссия МП руб

    @Column(name = "total_mp", precision = 12, scale = 2)
    private BigDecimal totalMp; // ИТОГО МП

    @Column(name = "total_to_pay", precision = 12, scale = 2)
    private BigDecimal totalToPay; // ИТОГО к оплате

    @Column(name = "tax", precision = 12, scale = 2)
    private BigDecimal tax; // Налог

    @Column(name = "revenue_after_tax", precision = 12, scale = 2)
    private BigDecimal revenueAfterTax; // Выручка после налога

    @Column(name = "final_gross_profit", precision = 12, scale = 2)
    private BigDecimal finalGrossProfit; // Валовая прибыль

    @Column(name = "markup_from_final_price", precision = 5, scale = 2)
    private BigDecimal markupFromFinalPrice; // Наценка от итоговой цены

    @Column(name = "final_marginality", precision = 5, scale = 2)
    private BigDecimal finalMarginality; // Маржинальность итоговая

    @Column(name = "gross_profitability_final", precision = 5, scale = 2)
    private BigDecimal grossProfitabilityFinal; // Рентабельность по Валовой итоговая

    @Column(name = "roi", precision = 5, scale = 2)
    private BigDecimal roi; // ROI

    // Метаданные
    @Column(name = "calculation_date", nullable = false)
    private LocalDateTime calculationDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Конструкторы
    public UnitEconomics() {
        this.createdAt = LocalDateTime.now();
        this.calculationDate = LocalDateTime.now();
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

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getDeliveryToWb() {
        return deliveryToWb;
    }

    public void setDeliveryToWb(BigDecimal deliveryToWb) {
        this.deliveryToWb = deliveryToWb;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit;
    }

    public BigDecimal getMpPriceBefore() {
        return mpPriceBefore;
    }

    public void setMpPriceBefore(BigDecimal mpPriceBefore) {
        this.mpPriceBefore = mpPriceBefore;
    }

    public BigDecimal getMpDiscount() {
        return mpDiscount;
    }

    public void setMpDiscount(BigDecimal mpDiscount) {
        this.mpDiscount = mpDiscount;
    }

    public BigDecimal getPriceBeforeSpp() {
        return priceBeforeSpp;
    }

    public void setPriceBeforeSpp(BigDecimal priceBeforeSpp) {
        this.priceBeforeSpp = priceBeforeSpp;
    }

    public BigDecimal getSppPercent() {
        return sppPercent;
    }

    public void setSppPercent(BigDecimal sppPercent) {
        this.sppPercent = sppPercent;
    }

    public BigDecimal getPriceAfterSpp() {
        return priceAfterSpp;
    }

    public void setPriceAfterSpp(BigDecimal priceAfterSpp) {
        this.priceAfterSpp = priceAfterSpp;
    }

    public BigDecimal getBreakEvenPointBeforeSpp() {
        return breakEvenPointBeforeSpp;
    }

    public void setBreakEvenPointBeforeSpp(BigDecimal breakEvenPointBeforeSpp) {
        this.breakEvenPointBeforeSpp = breakEvenPointBeforeSpp;
    }

    public BigDecimal getBuyout() {
        return buyout;
    }

    public void setBuyout(BigDecimal buyout) {
        this.buyout = buyout;
    }

    public BigDecimal getMpCommissionPercent() {
        return mpCommissionPercent;
    }

    public void setMpCommissionPercent(BigDecimal mpCommissionPercent) {
        this.mpCommissionPercent = mpCommissionPercent;
    }

    public BigDecimal getFirstLiterDeliveryCost() {
        return firstLiterDeliveryCost;
    }

    public void setFirstLiterDeliveryCost(BigDecimal firstLiterDeliveryCost) {
        this.firstLiterDeliveryCost = firstLiterDeliveryCost;
    }

    public BigDecimal getNextLiterDeliveryCost() {
        return nextLiterDeliveryCost;
    }

    public void setNextLiterDeliveryCost(BigDecimal nextLiterDeliveryCost) {
        this.nextLiterDeliveryCost = nextLiterDeliveryCost;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getLength() {
        return length;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public BigDecimal getTotalVolumeLiters() {
        return totalVolumeLiters;
    }

    public void setTotalVolumeLiters(BigDecimal totalVolumeLiters) {
        this.totalVolumeLiters = totalVolumeLiters;
    }

    public BigDecimal getWarehouseCoefficient() {
        return warehouseCoefficient;
    }

    public void setWarehouseCoefficient(BigDecimal warehouseCoefficient) {
        this.warehouseCoefficient = warehouseCoefficient;
    }

    public BigDecimal getLogisticsMp() {
        return logisticsMp;
    }

    public void setLogisticsMp(BigDecimal logisticsMp) {
        this.logisticsMp = logisticsMp;
    }

    public BigDecimal getLogisticsWithBuyout() {
        return logisticsWithBuyout;
    }

    public void setLogisticsWithBuyout(BigDecimal logisticsWithBuyout) {
        this.logisticsWithBuyout = logisticsWithBuyout;
    }

    public BigDecimal getFinalLogisticsWithIndex() {
        return finalLogisticsWithIndex;
    }

    public void setFinalLogisticsWithIndex(BigDecimal finalLogisticsWithIndex) {
        this.finalLogisticsWithIndex = finalLogisticsWithIndex;
    }

    public BigDecimal getStorageMp() {
        return storageMp;
    }

    public void setStorageMp(BigDecimal storageMp) {
        this.storageMp = storageMp;
    }

    public BigDecimal getMpCommissionRub() {
        return mpCommissionRub;
    }

    public void setMpCommissionRub(BigDecimal mpCommissionRub) {
        this.mpCommissionRub = mpCommissionRub;
    }

    public BigDecimal getTotalMp() {
        return totalMp;
    }

    public void setTotalMp(BigDecimal totalMp) {
        this.totalMp = totalMp;
    }

    public BigDecimal getTotalToPay() {
        return totalToPay;
    }

    public void setTotalToPay(BigDecimal totalToPay) {
        this.totalToPay = totalToPay;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getRevenueAfterTax() {
        return revenueAfterTax;
    }

    public void setRevenueAfterTax(BigDecimal revenueAfterTax) {
        this.revenueAfterTax = revenueAfterTax;
    }

    public BigDecimal getFinalGrossProfit() {
        return finalGrossProfit;
    }

    public void setFinalGrossProfit(BigDecimal finalGrossProfit) {
        this.finalGrossProfit = finalGrossProfit;
    }

    public BigDecimal getMarkupFromFinalPrice() {
        return markupFromFinalPrice;
    }

    public void setMarkupFromFinalPrice(BigDecimal markupFromFinalPrice) {
        this.markupFromFinalPrice = markupFromFinalPrice;
    }

    public BigDecimal getFinalMarginality() {
        return finalMarginality;
    }

    public void setFinalMarginality(BigDecimal finalMarginality) {
        this.finalMarginality = finalMarginality;
    }

    public BigDecimal getGrossProfitabilityFinal() {
        return grossProfitabilityFinal;
    }

    public void setGrossProfitabilityFinal(BigDecimal grossProfitabilityFinal) {
        this.grossProfitabilityFinal = grossProfitabilityFinal;
    }

    public BigDecimal getRoi() {
        return roi;
    }

    public void setRoi(BigDecimal roi) {
        this.roi = roi;
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