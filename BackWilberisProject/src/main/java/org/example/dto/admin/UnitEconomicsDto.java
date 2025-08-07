package org.example.dto.admin;

import java.util.List;

public class UnitEconomicsDto {
    private List<UnitEconomicsProductDto> products;
    private UnitEconomicsSummaryDto summary;

    public UnitEconomicsDto() {}

    public UnitEconomicsDto(List<UnitEconomicsProductDto> products, UnitEconomicsSummaryDto summary) {
        this.products = products;
        this.summary = summary;
    }

    public List<UnitEconomicsProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<UnitEconomicsProductDto> products) {
        this.products = products;
    }

    public UnitEconomicsSummaryDto getSummary() {
        return summary;
    }

    public void setSummary(UnitEconomicsSummaryDto summary) {
        this.summary = summary;
    }

    // Вложенный класс для продукта
    public static class UnitEconomicsProductDto {
        private Long id;
        private Long nmId;
        private String vendorCode;
        private String brandName;
        
        // Себестоимость и доставка
        private double costPrice;
        private double deliveryToCostPrice;
        
        // Валовая прибыль и цены
        private double grossProfit;
        private double mpPriceBefore;
        private double sppDiscount;
        private double priceAfterSpp;
        private double breakEvenPoint;
        
        // Выкуп и комиссии
        private double buyout;
        private double mpCommissionPercent;
        
        // Габариты
        private double height;
        private double width;
        private double length;
        
        // Логистика
        private double logisticsMp;
        private double logisticsWithBuyout;
        private double logisticsFinal;
        
        // Финансовые итоги
        private double mpCommissionRub;
        private double totalMp;
        private double toPay;
        private double tax;
        private double revenueAfterTax;
        private double finalGrossProfit;
        
        // Показатели эффективности
        private double markupPercent;
        private double marginality;
        private double profitabilityGross;
        private double roi;

        public UnitEconomicsProductDto() {}

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getNmId() { return nmId; }
        public void setNmId(Long nmId) { this.nmId = nmId; }
        
        public String getVendorCode() { return vendorCode; }
        public void setVendorCode(String vendorCode) { this.vendorCode = vendorCode; }
        
        public String getBrandName() { return brandName; }
        public void setBrandName(String brandName) { this.brandName = brandName; }
        
        public double getCostPrice() { return costPrice; }
        public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
        
        public double getDeliveryToCostPrice() { return deliveryToCostPrice; }
        public void setDeliveryToCostPrice(double deliveryToCostPrice) { this.deliveryToCostPrice = deliveryToCostPrice; }
        
        public double getGrossProfit() { return grossProfit; }
        public void setGrossProfit(double grossProfit) { this.grossProfit = grossProfit; }
        
        public double getMpPriceBefore() { return mpPriceBefore; }
        public void setMpPriceBefore(double mpPriceBefore) { this.mpPriceBefore = mpPriceBefore; }
        
        public double getSppDiscount() { return sppDiscount; }
        public void setSppDiscount(double sppDiscount) { this.sppDiscount = sppDiscount; }
        
        public double getPriceAfterSpp() { return priceAfterSpp; }
        public void setPriceAfterSpp(double priceAfterSpp) { this.priceAfterSpp = priceAfterSpp; }
        
        public double getBreakEvenPoint() { return breakEvenPoint; }
        public void setBreakEvenPoint(double breakEvenPoint) { this.breakEvenPoint = breakEvenPoint; }
        
        public double getBuyout() { return buyout; }
        public void setBuyout(double buyout) { this.buyout = buyout; }
        
        public double getMpCommissionPercent() { return mpCommissionPercent; }
        public void setMpCommissionPercent(double mpCommissionPercent) { this.mpCommissionPercent = mpCommissionPercent; }
        
        public double getHeight() { return height; }
        public void setHeight(double height) { this.height = height; }
        
        public double getWidth() { return width; }
        public void setWidth(double width) { this.width = width; }
        
        public double getLength() { return length; }
        public void setLength(double length) { this.length = length; }
        
        public double getLogisticsMp() { return logisticsMp; }
        public void setLogisticsMp(double logisticsMp) { this.logisticsMp = logisticsMp; }
        
        public double getLogisticsWithBuyout() { return logisticsWithBuyout; }
        public void setLogisticsWithBuyout(double logisticsWithBuyout) { this.logisticsWithBuyout = logisticsWithBuyout; }
        
        public double getLogisticsFinal() { return logisticsFinal; }
        public void setLogisticsFinal(double logisticsFinal) { this.logisticsFinal = logisticsFinal; }
        
        public double getMpCommissionRub() { return mpCommissionRub; }
        public void setMpCommissionRub(double mpCommissionRub) { this.mpCommissionRub = mpCommissionRub; }
        
        public double getTotalMp() { return totalMp; }
        public void setTotalMp(double totalMp) { this.totalMp = totalMp; }
        
        public double getToPay() { return toPay; }
        public void setToPay(double toPay) { this.toPay = toPay; }
        
        public double getTax() { return tax; }
        public void setTax(double tax) { this.tax = tax; }
        
        public double getRevenueAfterTax() { return revenueAfterTax; }
        public void setRevenueAfterTax(double revenueAfterTax) { this.revenueAfterTax = revenueAfterTax; }
        
        public double getFinalGrossProfit() { return finalGrossProfit; }
        public void setFinalGrossProfit(double finalGrossProfit) { this.finalGrossProfit = finalGrossProfit; }
        
        public double getMarkupPercent() { return markupPercent; }
        public void setMarkupPercent(double markupPercent) { this.markupPercent = markupPercent; }
        
        public double getMarginality() { return marginality; }
        public void setMarginality(double marginality) { this.marginality = marginality; }
        
        public double getProfitabilityGross() { return profitabilityGross; }
        public void setProfitabilityGross(double profitabilityGross) { this.profitabilityGross = profitabilityGross; }
        
        public double getRoi() { return roi; }
        public void setRoi(double roi) { this.roi = roi; }
    }

    // Вложенный класс для сводки
    public static class UnitEconomicsSummaryDto {
        private int totalProducts;
        private double totalRevenue;
        private double totalProfit;
        private double totalCosts;
        private double avgMargin;
        private double avgROI;

        public UnitEconomicsSummaryDto() {}

        public UnitEconomicsSummaryDto(int totalProducts, double totalRevenue, double totalProfit, 
                                      double totalCosts, double avgMargin, double avgROI) {
            this.totalProducts = totalProducts;
            this.totalRevenue = totalRevenue;
            this.totalProfit = totalProfit;
            this.totalCosts = totalCosts;
            this.avgMargin = avgMargin;
            this.avgROI = avgROI;
        }

        public int getTotalProducts() { return totalProducts; }
        public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
        
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
        
        public double getTotalProfit() { return totalProfit; }
        public void setTotalProfit(double totalProfit) { this.totalProfit = totalProfit; }
        
        public double getTotalCosts() { return totalCosts; }
        public void setTotalCosts(double totalCosts) { this.totalCosts = totalCosts; }
        
        public double getAvgMargin() { return avgMargin; }
        public void setAvgMargin(double avgMargin) { this.avgMargin = avgMargin; }
        
        public double getAvgROI() { return avgROI; }
        public void setAvgROI(double avgROI) { this.avgROI = avgROI; }
    }
} 
 
 