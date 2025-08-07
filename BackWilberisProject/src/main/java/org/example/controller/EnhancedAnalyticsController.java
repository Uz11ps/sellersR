package org.example.controller;

import org.example.entity.*;
import org.example.repository.*;
import org.example.service.WildberriesApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/enhanced-analytics")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class EnhancedAnalyticsController {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UnitEconomicsRepository unitEconomicsRepository;

    @Autowired
    private WeeklyFinancialReportRepository weeklyFinancialReportRepository;

    @Autowired
    private PromotionsTrackingRepository promotionsTrackingRepository;

    @Autowired
    private AdvertisingCampaignRepository advertisingCampaignRepository;

    @Autowired
    private SupplyPlanningRepository supplyPlanningRepository;

    @Autowired
    private WildberriesApiService wildberriesApiService;

    /**
     * Helper метод для получения пользователя из Authentication
     */
    private User getUserFromAuth(Authentication auth) {
        String userEmail = auth.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    /**
     * Helper метод для получения продавца
     */
    private Seller getSellerFromUser(User user, Long sellerId) {
        if (sellerId == null) {
            // Если не указан sellerId, возвращаем первого активного продавца
            List<Seller> sellers = sellerRepository.findByUserAndIsActiveTrue(user);
            if (sellers.isEmpty()) {
                throw new RuntimeException("У пользователя нет активных продавцов");
            }
            return sellers.get(0);
        }
        
        return sellerRepository.findByUserAndId(user, sellerId)
                .orElseThrow(() -> new RuntimeException("Продавец не найден"));
    }

    /**
     * ЮНИТ-ЭКОНОМИКА WB - Получение расширенной юнит-экономики
     */
    @GetMapping("/unit-economics")
    public ResponseEntity<?> getUnitEconomics(Authentication auth,
                                             @RequestParam(required = false) Long sellerId) {
        try {
            User user = getUserFromAuth(auth);
            Seller seller = getSellerFromUser(user, sellerId);
            
            List<UnitEconomics> unitEconomics = unitEconomicsRepository.findBySellerOrderByCalculationDateDesc(seller);
            
            // Создаем тестовые данные если таблица пуста
            if (unitEconomics.isEmpty()) {
                unitEconomics = generateTestUnitEconomics(seller);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("seller", Map.of(
                "id", seller.getId(),
                "name", seller.getSellerName(),
                "inn", seller.getInn() != null ? seller.getInn() : ""
            ));
            response.put("data", unitEconomics);
            response.put("totalItems", unitEconomics.size());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения юнит-экономики: " + e.getMessage()
            ));
        }
    }

    /**
     * ФИНАНСОВЫЙ ОТЧЕТ - Недельные финансовые отчеты
     */
    @GetMapping("/weekly-financial-report")
    public ResponseEntity<?> getWeeklyFinancialReport(Authentication auth,
                                                     @RequestParam(required = false) Long sellerId,
                                                     @RequestParam(value = "weeks", defaultValue = "12") int weeks) {
        try {
            User user = getUserFromAuth(auth);
            Seller seller = getSellerFromUser(user, sellerId);

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusWeeks(weeks);
            
            List<WeeklyFinancialReport> reports = weeklyFinancialReportRepository.findBySellerAndDateRange(
                seller, startDate, endDate);

            // Создаем тестовые данные если таблица пуста
            if (reports.isEmpty()) {
                reports = generateTestWeeklyReports(seller, weeks);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("seller", Map.of(
                "id", seller.getId(),
                "name", seller.getSellerName()
            ));
            response.put("data", reports);
            response.put("dateRange", Map.of(
                "start", startDate,
                "end", endDate
            ));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения недельного отчета: " + e.getMessage()
            ));
        }
    }

    /**
     * УЧЕТ АКЦИЙ - Трекинг промо-акций
     */
    @GetMapping("/promotions-tracking")
    public ResponseEntity<?> getPromotionsTracking(Authentication auth,
                                                  @RequestParam(required = false) Long sellerId) {
        try {
            User user = getUserFromAuth(auth);
            Seller seller = getSellerFromUser(user, sellerId);

            List<PromotionsTracking> promotions = promotionsTrackingRepository.findBySellerOrderByCreatedAtDesc(seller);

            // Создаем тестовые данные если таблица пуста
            if (promotions.isEmpty()) {
                promotions = generateTestPromotions(seller);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("seller", Map.of(
                "id", seller.getId(),
                "name", seller.getSellerName()
            ));
            response.put("data", promotions);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения данных по акциям: " + e.getMessage()
            ));
        }
    }

    /**
     * РК ТАБЛИЦА - Рекламные кампании
     */
    @GetMapping("/advertising-campaigns-table")
    public ResponseEntity<?> getAdvertisingCampaignsTable(Authentication auth,
                                                         @RequestParam(required = false) Long sellerId,
                                                         @RequestParam(value = "weeks", defaultValue = "5") int weeks) {
        try {
            User user = getUserFromAuth(auth);
            Seller seller = getSellerFromUser(user, sellerId);

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusWeeks(weeks);

            List<AdvertisingCampaign> campaigns = advertisingCampaignRepository.findBySellerAndDateRange(
                seller, startDate, endDate);

            // Создаем тестовые данные если таблица пуста
            if (campaigns.isEmpty()) {
                campaigns = generateTestAdvertisingCampaigns(seller, weeks);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("seller", Map.of(
                "id", seller.getId(),
                "name", seller.getSellerName()
            ));
            response.put("data", campaigns);
            response.put("weeksPeriod", weeks);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения РК таблицы: " + e.getMessage()
            ));
        }
    }

    /**
     * ПЛАН ПОСТАВОК - Планирование закупок
     */
    @GetMapping("/supply-planning")
    public ResponseEntity<?> getSupplyPlanning(Authentication auth,
                                              @RequestParam(required = false) Long sellerId) {
        try {
            User user = getUserFromAuth(auth);
            Seller seller = getSellerFromUser(user, sellerId);

            List<SupplyPlanning> supplyPlans = supplyPlanningRepository.findBySellerOrderByCalculationDateDesc(seller);

            // Создаем тестовые данные если таблица пуста
            if (supplyPlans.isEmpty()) {
                supplyPlans = generateTestSupplyPlanning(seller);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("seller", Map.of(
                "id", seller.getId(),
                "name", seller.getSellerName()
            ));
            response.put("data", supplyPlans);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения плана поставок: " + e.getMessage()
            ));
        }
    }

    /**
     * КОМПЛЕКСНАЯ АНАЛИТИКА - Все данные для dashboard
     */
    @GetMapping("/dashboard-data")
    public ResponseEntity<?> getDashboardData(Authentication auth,
                                             @RequestParam(required = false) Long sellerId) {
        try {
            User user = getUserFromAuth(auth);
            Seller seller = getSellerFromUser(user, sellerId);

            Map<String, Object> dashboardData = new HashMap<>();
            
            // Юнит-экономика (топ-10 по ROI)
            List<UnitEconomics> topRoi = unitEconomicsRepository.findBySellerWithMinRoi(seller, BigDecimal.ZERO);
            dashboardData.put("unitEconomics", topRoi.stream().limit(10).toList());

            // Недельные отчеты (последние 4 недели)
            LocalDate fourWeeksAgo = LocalDate.now().minusWeeks(4);
            List<WeeklyFinancialReport> recentReports = weeklyFinancialReportRepository.findBySellerAndDateRange(
                seller, fourWeeksAgo, LocalDate.now());
            dashboardData.put("weeklyReports", recentReports);

            // Критичные остатки
            List<SupplyPlanning> criticalStock = supplyPlanningRepository.findCriticalStock(seller, BigDecimal.valueOf(7));
            dashboardData.put("criticalStock", criticalStock);

            // Прибыльные акции
            List<PromotionsTracking> profitablePromotions = promotionsTrackingRepository.findProfitablePromotions(seller);
            dashboardData.put("profitablePromotions", profitablePromotions.stream().limit(5).toList());

            // Суммарная статистика
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalNetProfit", weeklyFinancialReportRepository.getTotalNetProfitBySeller(seller));
            summary.put("totalAdCalculation", advertisingCampaignRepository.getTotalCalculationBySeller(seller));
            summary.put("averageCalculation", advertisingCampaignRepository.getTotalCalculationBySeller(seller));
            summary.put("totalDemand30Days", supplyPlanningRepository.getTotalDemandFor30Days(seller));
            
            dashboardData.put("summary", summary);
            dashboardData.put("seller", Map.of(
                "id", seller.getId(),
                "name", seller.getSellerName(),
                "lastSync", seller.getLastSyncAt()
            ));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", dashboardData
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Ошибка получения данных dashboard: " + e.getMessage()
            ));
        }
    }

    // Методы для генерации тестовых данных
    private List<UnitEconomics> generateTestUnitEconomics(Seller seller) {
        List<UnitEconomics> testData = new ArrayList<>();
        String[] articles = {"WB123456", "WB789012", "WB345678", "WB901234", "WB567890"};
        String[] supplierArticles = {"SP001", "SP002", "SP003", "SP004", "SP005"};
        
        for (int i = 0; i < articles.length; i++) {
            UnitEconomics ue = new UnitEconomics();
            ue.setSeller(seller);
            ue.setWbArticle(articles[i]);
            ue.setSupplierArticle(supplierArticles[i]);
            ue.setCostPrice(BigDecimal.valueOf(100 + i * 50));
            ue.setDeliveryToWb(BigDecimal.valueOf(20 + i * 5));
            ue.setPriceBeforeSpp(BigDecimal.valueOf(300 + i * 100));
            ue.setSppPercent(BigDecimal.valueOf(10 + i * 2));
            ue.setPriceAfterSpp(BigDecimal.valueOf(270 + i * 90));
            ue.setHeight(BigDecimal.valueOf(10 + i));
            ue.setWidth(BigDecimal.valueOf(15 + i));
            ue.setLength(BigDecimal.valueOf(20 + i));
            ue.setTotalVolumeLiters(BigDecimal.valueOf(3.0 + i * 0.5));
            ue.setRoi(BigDecimal.valueOf(15 + i * 5));
            ue.setFinalMarginality(BigDecimal.valueOf(25 + i * 3));
            
            testData.add(ue);
        }
        
        return testData;
    }

    private List<WeeklyFinancialReport> generateTestWeeklyReports(Seller seller, int weeks) {
        List<WeeklyFinancialReport> testData = new ArrayList<>();
        
        for (int i = 0; i < weeks; i++) {
            WeeklyFinancialReport report = new WeeklyFinancialReport();
            report.setSeller(seller);
            report.setWeekNumber(52 - i);
            report.setDatePeriod(LocalDate.now().minusWeeks(i));
            report.setBuyoutQuantity(BigDecimal.valueOf(100 + i * 10));
            report.setWbSales(BigDecimal.valueOf(50000 + i * 5000));
            report.setLogistics(BigDecimal.valueOf(5000 + i * 500));
            report.setNetProfit(BigDecimal.valueOf(15000 + i * 1500));
            
            testData.add(report);
        }
        
        return testData;
    }

    private List<PromotionsTracking> generateTestPromotions(Seller seller) {
        List<PromotionsTracking> testData = new ArrayList<>();
        String[] abcTypes = {"A", "B", "C", "D", "F"};
        String[] subgroups = {"F подготовка", "D распродажа", "Стандарт", "Премиум"};
        
        for (int i = 0; i < 8; i++) {
            PromotionsTracking pt = new PromotionsTracking();
            pt.setSeller(seller);
            pt.setWbArticle("WB" + (100000 + i));
            pt.setSupplierArticle("SP" + (100 + i));
            pt.setAbcAnalysis(abcTypes[i % abcTypes.length]);
            pt.setSubgroupFPreparationDSale(subgroups[i % subgroups.length]);
            pt.setCurrentPrice(BigDecimal.valueOf(1000 + i * 200));
            pt.setPriceForPromotionParticipation(BigDecimal.valueOf(800 + i * 150));
            pt.setGrossProfit(BigDecimal.valueOf(300 + i * 50));
            pt.setTurnoverDays(BigDecimal.valueOf(30 + i * 5));
            pt.setWbStockBalance(BigDecimal.valueOf(50 + i * 10));
            
            testData.add(pt);
        }
        
        return testData;
    }

    private List<AdvertisingCampaign> generateTestAdvertisingCampaigns(Seller seller, int weeks) {
        List<AdvertisingCampaign> testData = new ArrayList<>();
        String[] indicators = {"Расходы РК", "CPC", "CTR", "Показы", "Клики"};
        
        for (int i = 0; i < 6; i++) {
            AdvertisingCampaign ac = new AdvertisingCampaign();
            ac.setSeller(seller);
            ac.setWbArticle("WB" + (200000 + i));
            ac.setSupplierArticle("SP" + (200 + i));
            ac.setIndicator(indicators[i % indicators.length]);
            
            // Заполняем недельные данные
            LocalDate currentWeek = LocalDate.now().minusWeeks(weeks - 1);
            ac.setWeek1Period("05.05-11.05");
            ac.setWeek1Value(BigDecimal.valueOf(1000 + i * 100));
            ac.setWeek2Period("12.05-18.05");
            ac.setWeek2Value(BigDecimal.valueOf(1100 + i * 110));
            ac.setWeek3Period("19.05-25.05");
            ac.setWeek3Value(BigDecimal.valueOf(1200 + i * 120));
            ac.setWeek4Period("26.05-01.06");
            ac.setWeek4Value(BigDecimal.valueOf(1300 + i * 130));
            ac.setWeek5Period("02.06-08.06");
            ac.setWeek5Value(BigDecimal.valueOf(1400 + i * 140));
            
            ac.setCalculation(BigDecimal.valueOf(6000 + i * 600));
            ac.setReportPeriodStart(currentWeek);
            ac.setReportPeriodEnd(LocalDate.now());
            
            testData.add(ac);
        }
        
        return testData;
    }

    private List<SupplyPlanning> generateTestSupplyPlanning(Seller seller) {
        List<SupplyPlanning> testData = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            SupplyPlanning sp = new SupplyPlanning();
            sp.setSeller(seller);
            sp.setWbArticle("WB" + (300000 + i));
            sp.setSupplierArticle("SP" + (300 + i));
            sp.setGoodsInTransitQuantity(BigDecimal.valueOf(20 + i * 5));
            sp.setGoodsOnSaleQuantity(BigDecimal.valueOf(50 + i * 10));
            sp.setTotalStockBalance(BigDecimal.valueOf(70 + i * 15));
            sp.setAverageOrdersPerDay(BigDecimal.valueOf(5 + i));
            sp.setTurnoverDays(BigDecimal.valueOf(14 - i));
            sp.setCoveragePlan30Days(BigDecimal.valueOf(150 + i * 20));
            sp.setSeasonalityCoefficient(BigDecimal.valueOf(1.0 + i * 0.1));
            
            testData.add(sp);
        }
        
        return testData;
    }
} 