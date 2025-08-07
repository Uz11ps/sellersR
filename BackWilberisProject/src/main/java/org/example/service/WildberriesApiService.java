package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.AnalyticsData;
import org.example.entity.Product;
import org.example.entity.User;
import org.example.repository.AnalyticsDataRepository;
import org.example.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class WildberriesApiService {

    @Autowired
    private AnalyticsDataRepository analyticsDataRepository;

    @Autowired
    private ProductRepository productRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Базовые URL для продакшена (реальные API Wildberries)
    private static final String STATISTICS_API = "https://statistics-api.wildberries.ru";
    private static final String ANALYTICS_API = "https://seller-analytics-api.wildberries.ru";
    private static final String ADVERT_API = "https://advert-api.wildberries.ru";
    private static final String FEEDBACKS_API = "https://feedbacks-api.wildberries.ru";
    private static final String COMMON_API = "https://common-api.wildberries.ru";
    private static final String CONTENT_API = "https://content-api.wildberries.ru";
    private static final String MARKETPLACE_API = "https://marketplace-api.wildberries.ru";
    private static final String SUPPLIES_API = "https://supplies-api.wildberries.ru";
    private static final String DOCUMENTS_API = "https://documents-api.wildberries.ru";
    private static final String FINANCE_API = "https://finance-api.wildberries.ru";
    
    // Время последнего запроса для контроля лимитов
    private static long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL = 2000; // Увеличиваем интервал до 2 секунд для безопасности
    
    // Счетчик попыток для предотвращения бесконечных циклов
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    // Флаг блокировки при ожидании восстановления лимитов
    private static volatile boolean isWaitingForRateLimit = false;
    private static volatile long rateLimitResetTime = 0;
    
    // Кеш для хранения последних успешных ответов
    private static final Map<String, CachedResponse> responseCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 60000; // 1 минута
    
    // Глобальная блокировка для предотвращения параллельных запросов к одному эндпоинту
    private static final ReentrantLock apiLock = new ReentrantLock();
    
    // Класс для хранения кешированных ответов
    private static class CachedResponse {
        final JsonNode data;
        final long timestamp;
        
        CachedResponse(JsonNode data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }
    
    // Backup sandbox URLs для тестирования
    private static final String STATISTICS_API_SANDBOX = "https://statistics-api-sandbox.wildberries.ru";
    private static final String ADVERT_API_SANDBOX = "https://advert-api-sandbox.wildberries.ru";
    private static final String FEEDBACKS_API_SANDBOX = "https://feedbacks-api-sandbox.wildberries.ru";

    /**
     * Метод для контроля лимитов запросов
     */
    private synchronized void rateLimitControl() {
        long currentTime = System.currentTimeMillis();
        
        // Проверяем, не ждем ли мы восстановления лимитов
        if (isWaitingForRateLimit && currentTime < rateLimitResetTime) {
            long waitTime = rateLimitResetTime - currentTime;
            System.out.println("⏸️ Ожидаем восстановления лимитов еще " + (waitTime / 1000) + " секунд...");
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            isWaitingForRateLimit = false;
        }
        
        // Обычная проверка интервала между запросами
        long timeSinceLastRequest = currentTime - lastRequestTime;
        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
            try {
                long sleepTime = MIN_REQUEST_INTERVAL - timeSinceLastRequest;
                System.out.println("⏳ Ждем " + sleepTime + " мс перед следующим запросом...");
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        lastRequestTime = System.currentTimeMillis();
    }
    
    /**
     * Очистка кеша (можно вызвать при необходимости)
     */
    public void clearCache() {
        responseCache.clear();
        System.out.println("🧹 Кеш очищен");
    }
    
    /**
     * Проверка валидности API ключа через ping endpoints
     */
    public boolean validateApiKey(String apiKey) {
        try {
            System.out.println("🔍 Проверка API ключа: " + apiKey.substring(0, Math.min(20, apiKey.length())) + "...");
            
            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Список эндпоинтов для проверки (в порядке приоритета)
            String[] pingEndpoints = {
                COMMON_API + "/ping",
                STATISTICS_API + "/ping"
            };
            
            for (String endpoint : pingEndpoints) {
                try {
                    System.out.println("📞 Запрос к: " + endpoint);
                    ResponseEntity<String> response = restTemplate.exchange(
                        endpoint,
                        HttpMethod.GET,
                        entity,
                        String.class
                    );
                    
                    if (response.getStatusCode() == HttpStatus.OK) {
                        System.out.println("✅ API ключ валиден! Эндпоинт: " + endpoint);
                        return true;
                    }
                } catch (HttpClientErrorException e) {
                    System.out.println("⚠️ " + endpoint + " - HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
                } catch (Exception e) {
                    System.out.println("❌ Ошибка запроса к " + endpoint + ": " + e.getMessage());
                }
            }
            
            System.err.println("❌ API ключ не валиден ни для одного эндпоинта");
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Общая ошибка валидации API ключа: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Проверка доступности Statistics API для реального токена
     * Тестируем базовые эндпоинты статистики WB
     */
    public boolean testStatisticsAccess(String apiKey) {
        try {
            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Тестируем различные Statistics API эндпоинты
            String[] testEndpoints = {
                STATISTICS_API + "/ping", // Проверка доступности
                STATISTICS_API + "/api/v1/supplier/incomes", // Поставки
                STATISTICS_API + "/api/v1/supplier/stocks" // Остатки
            };
            
            boolean hasAccess = false;
            for (String endpoint : testEndpoints) {
                try {
                    if (endpoint.contains("/ping")) {
                        // Ping эндпоинт без параметров
                        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, entity, String.class);
                        if (response.getStatusCode().is2xxSuccessful()) {
                            System.out.println("✅ Statistics API ping успешен");
                            hasAccess = true;
                        }
                    } else {
                        // Эндпоинты с минимальными параметрами
                        LocalDate yesterday = LocalDate.now().minusDays(1);
                        String dateFrom = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE);
                        String urlWithParams = endpoint + "?dateFrom=" + dateFrom;
                        
                        ResponseEntity<String> response = restTemplate.exchange(urlWithParams, HttpMethod.GET, entity, String.class);
                        if (response.getStatusCode().is2xxSuccessful()) {
                            System.out.println("✅ Statistics API эндпоинт доступен: " + endpoint);
                            hasAccess = true;
                        }
                    }
                } catch (HttpClientErrorException e) {
                    if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        System.err.println("❌ Statistics API: неавторизован для " + endpoint);
                        return false; // Если 401 - токен точно неправильный
                    } else if (e.getStatusCode().value() == 422 || e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        System.out.println("⚠️ Statistics API: " + endpoint + " требует другие параметры, но токен работает");
                        hasAccess = true; // 422/400 означает что токен правильный, но параметры неверные
                    } else {
                        System.out.println("⚠️ Statistics API: " + endpoint + " - " + e.getStatusCode());
                    }
                }
            }
            
            if (hasAccess) {
                System.out.println("✅ Statistics API доступен для данного токена");
            } else {
                System.err.println("❌ Statistics API недоступен для данного токена");
            }
            
            return hasAccess;
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка проверки Statistics API: " + e.getMessage());
            return false;
        }
    }

    /**
     * Получение финансового отчета через Statistics API
     * Используем getSalesReport как основной метод для финансовых данных
     */
    public JsonNode getFinanceReport(String apiKey, LocalDate startDate, LocalDate endDate) {
        // Finance API больше не используется, переходим на Statistics API
        return getSalesReport(apiKey, startDate, endDate);
    }

    /**
     * Получение баланса продавца через Finance API
     * Эндпоинт /api/v5/supplier/balance для получения текущего баланса
     */
    public JsonNode getSellerBalance(String apiKey) {
        try {
            String url = FINANCE_API + "/api/v5/supplier/balance";
            System.out.println("🔍 Запрос баланса продавца: " + url);

            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Баланс продавца получен успешно");
                return objectMapper.readTree(response.getBody());
            } else {
                System.err.println("⚠️ Неожиданный статус ответа: " + response.getStatusCode());
                return null;
            }
            
        } catch (HttpClientErrorException e) {
            System.err.println("❌ Ошибка получения баланса: HTTP " + e.getStatusCode());
            System.err.println("Ответ: " + e.getResponseBodyAsString());
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ Общая ошибка получения баланса: " + e.getMessage());
            return null;
        }
    }

    /**
     * Получение отчета по продажам и возвратам через Statistics API
     * Используется эндпоинт /api/v1/supplier/sales согласно документации WB
     */
    public JsonNode getSalesReport(String apiKey, LocalDate startDate, LocalDate endDate) {
        String cacheKey = "sales_" + apiKey.substring(0, 10) + "_" + startDate + "_" + endDate;
        
        // Проверяем кеш
        CachedResponse cached = responseCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            System.out.println("📦 Используем кешированные данные для sales report");
            return cached.data;
        }
        
        // Получаем блокировку для предотвращения параллельных запросов
        apiLock.lock();
        try {
            // Проверяем кеш еще раз после получения блокировки
            cached = responseCache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                System.out.println("📦 Используем кешированные данные для sales report (после блокировки)");
                return cached.data;
            }
            
            // Делаем запрос
            JsonNode result = getSalesReportWithRetry(apiKey, startDate, endDate, 0);
            
            // Сохраняем в кеш только успешные ответы
            if (result != null) {
                responseCache.put(cacheKey, new CachedResponse(result));
            }
            
            return result;
        } finally {
            apiLock.unlock();
        }
    }
    
    private JsonNode getSalesReportWithRetry(String apiKey, LocalDate startDate, LocalDate endDate, int attemptNumber) {
        try {
            // Контроль лимитов запросов
            rateLimitControl();
            
            // Формат даты для WB API: YYYY-MM-DD
            String dateFrom = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            
            // Строим URL с параметрами
            UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(STATISTICS_API + "/api/v1/supplier/sales")
                .queryParam("dateFrom", dateFrom);
            
            // Добавляем dateTo если указан
            if (endDate != null && !endDate.equals(startDate)) {
                String dateTo = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                builder.queryParam("dateTo", dateTo);
            }
            
            String url = builder.toUriString();
            System.out.println("🔍 Запрос отчета продаж: " + url);

            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Отчет продаж получен успешно");
                return objectMapper.readTree(response.getBody());
            } else {
                System.err.println("⚠️ Неожиданный статус ответа: " + response.getStatusCode());
                return null;
            }
            
        } catch (HttpClientErrorException e) {
            System.err.println("❌ HTTP ошибка при получении отчета продаж: " + e.getStatusCode());
            System.err.println("Ответ сервера: " + e.getResponseBodyAsString());
            
            // Обработка 429 Too Many Requests
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                // Проверяем количество попыток
                if (attemptNumber >= MAX_RETRY_ATTEMPTS - 1) {
                    System.err.println("❌ Превышено максимальное количество попыток (" + MAX_RETRY_ATTEMPTS + ")");
                    return null;
                }
                
                // Получаем заголовки из ответа
                HttpHeaders headers = e.getResponseHeaders();
                String retryAfter = headers != null ? headers.getFirst("X-Ratelimit-Retry") : null;
                String resetTime = headers != null ? headers.getFirst("X-Ratelimit-Reset") : null;
                
                int waitSeconds = 5; // По умолчанию 5 секунд
                
                if (retryAfter != null) {
                    try {
                        waitSeconds = Integer.parseInt(retryAfter);
                    } catch (NumberFormatException nfe) {
                        // Используем значение по умолчанию
                    }
                }
                
                // Устанавливаем глобальную блокировку
                isWaitingForRateLimit = true;
                rateLimitResetTime = System.currentTimeMillis() + (waitSeconds * 1000L);
                
                System.out.println("⏳ Превышен лимит запросов (попытка " + (attemptNumber + 1) + "/" + MAX_RETRY_ATTEMPTS + 
                                 "). Ждем " + waitSeconds + " секунд перед повторной попыткой...");
                if (resetTime != null) {
                    System.out.println("   Лимит восстановится через " + resetTime + " секунд");
                }
                
                try {
                    Thread.sleep(waitSeconds * 1000L); // Ждем указанное количество секунд
                    isWaitingForRateLimit = false; // Снимаем блокировку
                    // Рекурсивный вызов для повторной попытки
                    return getSalesReportWithRetry(apiKey, startDate, endDate, attemptNumber + 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    isWaitingForRateLimit = false; // Снимаем блокировку при прерывании
                    return null;
                }
            }
            
            // Пробуем альтернативный эндпоинт или sandbox
            if (e.getStatusCode() == HttpStatus.NOT_FOUND || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                System.out.println("🔄 Пробуем альтернативный эндпоинт...");
                return getSalesReportFallback(apiKey, startDate, endDate);
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("❌ Общая ошибка при получении отчета продаж: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Fallback метод для получения отчета продаж через альтернативные эндпоинты
     */
    private JsonNode getSalesReportFallback(String apiKey, LocalDate startDate, LocalDate endDate) {
        try {
            // Пробуем orders эндпоинт как альтернативу
            String dateFrom = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String url = STATISTICS_API + "/api/v1/supplier/orders?dateFrom=" + dateFrom + "&flag=0";
            
            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Fallback отчет получен через orders API");
                return objectMapper.readTree(response.getBody());
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Fallback метод также не сработал: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Получение информации о продавце через Common API
     * Для продакшен токенов должен работать без проблем
     */
    public JsonNode getSellerInfo(String apiKey) {
        try {
            String url = COMMON_API + "/api/v1/seller-info";
            System.out.println("🔍 Запрос информации о продавце: " + url);

            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Информация о продавце получена успешно");
                return objectMapper.readTree(response.getBody());
            } else {
                System.err.println("⚠️ Неожиданный статус ответа: " + response.getStatusCode());
                return null;
            }
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                System.err.println("❌ Токен не авторизован для получения информации о продавце");
                System.err.println("Проверьте правильность API ключа и его права доступа");
                return null;
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                System.err.println("❌ Доступ запрещен. Возможно токен тестовый (с опцией 'Тестовый контур')");
                
                // Для тестовых токенов возвращаем заглушку
                try {
                    return objectMapper.readTree("{\"name\": \"Тестовый продавец\", \"sid\": \"test-token\", \"tradeMark\": \"Тестовый магазин\", \"note\": \"Информация недоступна для тестового токена\"}");
                } catch (Exception parseE) {
                    return null;
                }
            }
            
            System.err.println("❌ HTTP ошибка при получении информации о продавце: " + e.getStatusCode());
            System.err.println("Ответ сервера: " + e.getResponseBodyAsString());
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ Общая ошибка при получении информации о продавце: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получение остатков товаров через Statistics API
     */
    public JsonNode getStocksReport(String apiKey, LocalDate dateFrom) {
        try {
            String dateFromStr = dateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String url = STATISTICS_API + "/api/v1/supplier/stocks?dateFrom=" + dateFromStr;
            System.out.println("🔍 Запрос остатков: " + url);

            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Отчет остатков получен успешно");
                return objectMapper.readTree(response.getBody());
            } else {
                System.err.println("⚠️ Неожиданный статус ответа: " + response.getStatusCode());
                return null;
            }
            
        } catch (HttpClientErrorException e) {
            System.err.println("❌ HTTP ошибка при получении остатков: " + e.getStatusCode());
            System.err.println("Ответ сервера: " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            System.err.println("❌ Общая ошибка при получении остатков: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Получение поставок через Statistics API
     */
    public JsonNode getIncomesReport(String apiKey, LocalDate dateFrom) {
        try {
            String dateFromStr = dateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String url = STATISTICS_API + "/api/v1/supplier/incomes?dateFrom=" + dateFromStr;
            System.out.println("🔍 Запрос поставок: " + url);

            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Отчет поставок получен успешно");
                return objectMapper.readTree(response.getBody());
            } else {
                System.err.println("⚠️ Неожиданный статус ответа: " + response.getStatusCode());
                return null;
            }
            
        } catch (HttpClientErrorException e) {
            System.err.println("❌ HTTP ошибка при получении поставок: " + e.getStatusCode());
            System.err.println("Ответ сервера: " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            System.err.println("❌ Общая ошибка при получении поставок: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Получение заказов через Statistics API
     */
    public JsonNode getOrdersReportNew(String apiKey, LocalDate dateFrom, int flag) {
        try {
            String dateFromStr = dateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String url = STATISTICS_API + "/api/v1/supplier/orders?dateFrom=" + dateFromStr + "&flag=" + flag;
            System.out.println("🔍 Запрос заказов: " + url);

            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Отчет заказов получен успешно");
                return objectMapper.readTree(response.getBody());
            } else {
                System.err.println("⚠️ Неожиданный статус ответа: " + response.getStatusCode());
                return null;
            }
            
        } catch (HttpClientErrorException e) {
            System.err.println("❌ HTTP ошибка при получении заказов: " + e.getStatusCode());
            System.err.println("Ответ сервера: " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            System.err.println("❌ Общая ошибка при получении заказов: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Получение новостей через Common API
     */
    public JsonNode getNews(String apiKey, LocalDate fromDate, Integer fromID) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(COMMON_API + "/api/communications/v2/news");
            
            if (fromDate != null) {
                builder.queryParam("from", fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            
            if (fromID != null) {
                builder.queryParam("fromID", fromID);
            }
            
            String url = builder.toUriString();
            System.out.println("🔍 Запрос новостей: " + url);

            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Новости получены успешно");
                return objectMapper.readTree(response.getBody());
            } else {
                System.err.println("⚠️ Неожиданный статус ответа: " + response.getStatusCode());
                return null;
            }
            
        } catch (HttpClientErrorException e) {
            System.err.println("❌ HTTP ошибка при получении новостей: " + e.getStatusCode());
            System.err.println("Ответ сервера: " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            System.err.println("❌ Общая ошибка при получении новостей: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получение заказов
     */
    public JsonNode getOrdersReport(String apiKey, LocalDate dateFrom) {
        try {
            String dateFromStr = dateFrom.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String url = STATISTICS_API + "/api/v1/supplier/orders?dateFrom=" + dateFromStr;

            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return objectMapper.readTree(response.getBody());
            
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP ошибка при получении заказов: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            System.err.println("Ошибка при получении заказов: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получение реализаций (продаж)
     */
    public JsonNode getSalesData(String apiKey, LocalDate dateFrom) {
        try {
            String dateFromStr = dateFrom.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String url = STATISTICS_API + "/api/v1/supplier/sales?dateFrom=" + dateFromStr;

            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return objectMapper.readTree(response.getBody());
            
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP ошибка при получении продаж: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            System.err.println("Ошибка при получении продаж: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получение данных рекламных кампаний (заглушка для тестового контура)
     * В тестовом контуре реальных данных нет, возвращаем пустой массив
     */
    public JsonNode getAdvertCampaignsData(String apiKey, LocalDate dateFrom) {
        try {
            // В тестовом контуре реальных данных о РК нет
            // Возвращаем пустой массив для корректной работы фронтенда
            return objectMapper.readTree("[]");
            
        } catch (Exception e) {
            System.err.println("Ошибка при получении данных рекламных кампаний: " + e.getMessage());
            return null;
        }
    }

    /**
     * Синхронизация данных аналитики за период
     */
    public void syncAnalyticsData(User user, LocalDate startDate, LocalDate endDate) {
        if (user.getWildberriesApiKey() == null || user.getWildberriesApiKey().trim().isEmpty()) {
            throw new RuntimeException("Wildberries API ключ не найден");
        }

        try {
            System.out.println("Начинаем синхронизацию данных для пользователя: " + user.getEmail());
            
            // Проверяем валидность API ключа
            if (!validateApiKey(user.getWildberriesApiKey())) {
                throw new RuntimeException("Неверный API ключ Wildberries");
            }

            // Получаем отчет по продажам
            JsonNode salesReport = getSalesReport(user.getWildberriesApiKey(), startDate, endDate);
            if (salesReport != null) {
                processSalesReport(user, salesReport, startDate, endDate);
                System.out.println("Отчет по продажам обработан");
            }

            // Получаем данные остатков
            JsonNode stocksReport = getStocksReport(user.getWildberriesApiKey(), startDate);
            if (stocksReport != null) {
                processStocksReport(user, stocksReport);
                System.out.println("Отчет по остаткам обработан");
            }

            // Получаем заказы
            JsonNode ordersReport = getOrdersReport(user.getWildberriesApiKey(), startDate);
            if (ordersReport != null) {
                processOrdersReport(user, ordersReport, startDate, endDate);
                System.out.println("Отчет по заказам обработан");
            }

            System.out.println("Синхронизация завершена успешно");

        } catch (Exception e) {
            System.err.println("Ошибка синхронизации данных: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ошибка синхронизации данных: " + e.getMessage());
        }
    }

    /**
     * Создание заголовков для запроса с API ключом
     * Использует правильный формат для Wildberries API
     */
    private HttpHeaders createHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        
        // Wildberries API использует заголовок Authorization с токеном напрямую (без Bearer)
        headers.set("Authorization", apiKey);
        
        return headers;
    }

    /**
     * Обработка отчета по продажам
     */
    private void processSalesReport(User user, JsonNode salesReport, LocalDate startDate, LocalDate endDate) {
        try {
            if (salesReport.isArray()) {
                for (JsonNode item : salesReport) {
                    AnalyticsData analyticsData = new AnalyticsData();
                    analyticsData.setUser(user);
                    analyticsData.setPeriodStart(startDate);
                    analyticsData.setPeriodEnd(endDate);
                    analyticsData.setPeriodType("SALES_REPORT");
                    
                    if (item.has("quantity")) {
                        analyticsData.setSoldQuantity(item.get("quantity").asInt());
                    }
                    if (item.has("totalPrice")) {
                        analyticsData.setSalesAmount(BigDecimal.valueOf(item.get("totalPrice").asDouble()));
                    }
                    if (item.has("price")) {
                        analyticsData.setPrice(BigDecimal.valueOf(item.get("price").asDouble()));
                    }

                    analyticsData.setCreatedAt(LocalDateTime.now());
                    analyticsDataRepository.save(analyticsData);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки отчета продаж: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Обработка отчета по остаткам
     */
    private void processStocksReport(User user, JsonNode stocksReport) {
        try {
            if (stocksReport.isArray()) {
                for (JsonNode item : stocksReport) {
                    Long nmId = item.has("nmId") ? item.get("nmId").asLong() : null;
                    if (nmId == null) continue;

                    Optional<Product> existingProduct = productRepository.findByUserAndNmId(user, nmId);
                    
                    Product product;
                    if (existingProduct.isPresent()) {
                        product = existingProduct.get();
                    } else {
                        product = new Product();
                        product.setUser(user);
                        product.setNmId(nmId);
                    }
                    
                    if (item.has("subject")) {
                        product.setCategory(item.get("subject").asText());
                    }
                    if (item.has("brand")) {
                        product.setBrand(item.get("brand").asText());
                    }
                    if (item.has("vendorCode")) {
                        product.setVendorCode(item.get("vendorCode").asText());
                    }
                    if (item.has("quantity")) {
                        product.setStock(item.get("quantity").asInt());
                    }

                    product.setUpdatedAt(LocalDateTime.now());
                    productRepository.save(product);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки отчета остатков: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Обработка отчета по заказам
     */
    private void processOrdersReport(User user, JsonNode ordersReport, LocalDate startDate, LocalDate endDate) {
        try {
            if (ordersReport.isArray()) {
                int totalOrders = 0;
                BigDecimal totalOrdersAmount = BigDecimal.ZERO;
                
                for (JsonNode item : ordersReport) {
                    totalOrders++;
                    if (item.has("totalPrice")) {
                        totalOrdersAmount = totalOrdersAmount.add(BigDecimal.valueOf(item.get("totalPrice").asDouble()));
                    }
                }

                // Создаем сводную запись по заказам
                if (totalOrders > 0) {
                    AnalyticsData analyticsData = new AnalyticsData();
                    analyticsData.setUser(user);
                    analyticsData.setPeriodStart(startDate);
                    analyticsData.setPeriodEnd(endDate);
                    analyticsData.setPeriodType("ORDERS_SUMMARY");
                    analyticsData.setOrdersCount(totalOrders);
                    analyticsData.setSalesAmount(totalOrdersAmount);
                    analyticsData.setCreatedAt(LocalDateTime.now());
                    
                    analyticsDataRepository.save(analyticsData);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки отчета заказов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Геттеры и сеттеры
    public AnalyticsDataRepository getAnalyticsDataRepository() {
        return analyticsDataRepository;
    }

    public void setAnalyticsDataRepository(AnalyticsDataRepository analyticsDataRepository) {
        this.analyticsDataRepository = analyticsDataRepository;
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
} 
 
 