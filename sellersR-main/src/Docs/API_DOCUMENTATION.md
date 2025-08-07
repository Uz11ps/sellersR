# 📚 WILBERIS ANALYTICS - ПОЛНАЯ ДОКУМЕНТАЦИЯ BACKEND

## 🏗️ АРХИТЕКТУРА СИСТЕМЫ

### Технологический стек
- **Framework:** Spring Boot 2.7+
- **Database:** PostgreSQL 
- **Security:** Spring Security + JWT
- **ORM:** Hibernate/JPA
- **External APIs:** Wildberries API, Telegram Bot API
- **Build Tool:** Maven

### Структура проекта
```
src/main/java/org/example/
├── config/           # Конфигурации (Security, Telegram, Web)
├── controller/       # REST контроллеры
├── dto/             # Data Transfer Objects
├── entity/          # JPA сущности
├── repository/      # Репозитории для работы с БД
├── service/         # Бизнес-логика
└── WilberisAnalyticsApplication.java # Главный класс
```

## 🌐 Базовая информация

**Базовый URL:** `http://localhost:8080`  
**Версия API:** 1.0  
**Формат данных:** JSON  

### 🔐 Авторизация

Система использует JWT токены для аутентификации:
```
Authorization: Bearer <your_jwt_token>
```

**Жизненный цикл токена:**
- Срок действия: 24 часа
- Автоматическое обновление через Axios interceptors
- Толерантность к сдвигу времени: 5 минут

### 🌍 CORS настройки

Разрешенные origins:
- `http://localhost:3000`
- `http://localhost:3002` 
- `http://localhost:5173`
- `http://127.0.0.1:5173`
- `http://127.0.0.1:3000`

### 🛡️ Безопасность

**Публичные эндпоинты (без токена):**
- `/api/auth/register`
- `/api/auth/login` 
- `/api/auth/verify`
- `/api/public/**`
- `/api/analytics/**` (демо данные)

**Приватные эндпоинты (требуют токен):**
- `/api/auth/user-info`
- `/api/auth/update-profile`
- `/api/auth/change-password`
- `/api/auth/api-key`
- `/api/subscription/**`

## 🗄️ БАЗА ДАННЫХ

### Схема БД (PostgreSQL)

**Таблица users:**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    telegram_chat_id VARCHAR(50),
    wb_api_key VARCHAR(1000),
    is_verified BOOLEAN DEFAULT FALSE,
    verification_code VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Таблица subscriptions:**
```sql
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    plan_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    price DECIMAL(10,2),
    auto_renew BOOLEAN DEFAULT FALSE,
    payment_method VARCHAR(50),
    payment_transaction_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Индексы
```sql
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_telegram_chat_id ON users(telegram_chat_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);
```

## 🔌 ВНЕШНИЕ ИНТЕГРАЦИИ

### Telegram Bot
- **Назначение:** Верификация пользователей через Telegram
- **Конфигурация:** `TelegramBotConfig.java`
- **Токен:** Настраивается в `application.yml`
- **Функции:**
  - Отправка кодов верификации
  - Уведомления о важных событиях

### Wildberries API
- **Назначение:** Получение аналитических данных продавцов
- **Аутентификация:** API ключ пользователя
- **Эндпоинты:**
  - Финансовые отчеты
  - Юнит-экономика
  - Рекламные кампании
  - ABC-анализ

## 🔄 ЖИЗНЕННЫЙ ЦИКЛ ЗАПРОСА

### 1. Аутентификация
```
Client Request → JwtAuthenticationFilter → SecurityConfig → Controller
                     ↓
              JWT Validation & User Context
```

### 2. Обработка данных
```
Controller → Service → Repository → Database
     ↓         ↓          ↓
   DTO ←── Business ←── Entity
           Logic      Mapping
```

### 3. Внешние API
```
Service → WildberriesApiService → External API
   ↓             ↓                     ↓
Response ←── Data Processing ←── Raw Data
```

---

## 🔑 АУТЕНТИФИКАЦИЯ (/api/auth)

### 1. Регистрация пользователя

**POST** `/api/auth/register`

**Описание:** Регистрация нового пользователя с отправкой кода верификации в Telegram

**Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "Иван",
  "lastName": "Петров",
  "phoneNumber": "+79991234567"
}
```

**Ответ при успехе:**
```json
{
  "success": true,
  "message": "Регистрация успешна",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "email": "user@example.com",
    "firstName": "Иван",
    "lastName": "Петров"
  },
  "verificationCode": "123456",
  "telegramBot": "@SellersWilberis_bot"
}
```

**Ответ при ошибке:**
```json
{
  "success": false,
  "message": "Пользователь с таким email уже существует"
}
```

### 2. Авторизация пользователя

**POST** `/api/auth/login`

**Описание:** Авторизация существующего пользователя

**Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Ответ при успехе:**
```json
{
  "success": true,
  "message": "Авторизация успешна",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "email": "user@example.com",
    "firstName": "Иван",
    "lastName": "Петров"
  }
}
```

### 3. Верификация пользователя

**POST** `/api/auth/verify`

**Описание:** Подтверждение email через код из Telegram

**Body:**
```json
{
  "verificationCode": "123456"
}
```

**Ответ при успехе:**
```json
{
  "success": true,
  "message": "Верификация прошла успешно"
}
```

### 4. Получение информации о пользователе

**GET** `/api/auth/user-info`

**Описание:** Получение текущей информации о пользователе

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Ответ при успехе:**
```json
{
  "success": true,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "Иван",
    "lastName": "Петров",
    "phoneNumber": "+79991234567",
    "isVerified": true,
    "hasApiKey": true,
    "hasSubscription": true
  }
}
```

### 5. Обновление профиля

**POST** `/api/auth/update-profile`

**Описание:** Обновление данных профиля пользователя

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Body:**
```json
{
  "firstName": "Новое имя",
  "lastName": "Новая фамилия",
  "phoneNumber": "+79999999999"
}
```

**Ответ при успехе:**
```json
{
  "success": true,
  "message": "Профиль успешно обновлен",
  "user": {
    "firstName": "Новое имя",
    "lastName": "Новая фамилия",
    "phoneNumber": "+79999999999"
  }
}
```

### 6. Изменение пароля

**POST** `/api/auth/change-password`

**Описание:** Изменение пароля пользователя

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Body:**
```json
{
  "currentPassword": "old_password",
  "newPassword": "new_password"
}
```

**Ответ при успехе:**
```json
{
  "success": true,
  "message": "Пароль успешно изменен",
  "token": "new_jwt_token_here"
}
```

### 7. Установка API ключа Wildberries

**POST** `/api/auth/api-key`

**Описание:** Сохранение API ключа Wildberries для пользователя

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Body:**
```json
{
  "email": "user@example.com",
  "apiKey": "your_wildberries_api_key_here"
}
```

**Ответ при успехе:**
```json
{
  "success": true,
  "message": "API ключ успешно сохранен"
}
```

---

## 💳 ПОДПИСКИ (/api/subscription)

### 1. Получение доступных планов

**GET** `/api/subscription/plans`

**Описание:** Получение списка всех доступных планов подписки

**Ответ при успехе:**
```json
{
  "success": true,
  "plans": [
    {
      "planType": "PLAN_FREE",
      "displayName": "Бесплатный тестовый",
      "price": 0.0,
      "days": 7,
      "features": [
        "Базовая аналитика",
        "Тестовый доступ",
        "7 дней"
      ]
    },
    {
      "planType": "PLAN_30_DAYS",
      "displayName": "30 дней",
      "price": 1499.0,
      "days": 30,
      "features": [
        "Финансовая таблица",
        "ABC-анализ",
        "Планирование поставок"
      ]
    }
  ]
}
```

### 2. Информация о текущей подписке

**GET** `/api/subscription/info`

**Описание:** Получение информации о текущей подписке пользователя

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Ответ при успехе:**
```json
{
  "success": true,
  "subscription": {
    "id": 1,
    "planType": "PLAN_30_DAYS",
    "displayName": "30 дней",
    "status": "ACTIVE",
    "startDate": "2025-01-27T10:00:00",
    "endDate": "2025-02-26T10:00:00",
    "price": 1499.0,
    "autoRenew": false,
    "daysLeft": 23
  },
  "hasActiveSubscription": true
}
```

### 3. Создание подписки

**POST** `/api/subscription/create`

**Описание:** Создание новой подписки для пользователя

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Body:**
```json
{
  "planType": "PLAN_30_DAYS",
  "paymentMethod": "card",
  "autoRenew": true
}
```

**Ответ при успехе:**
```json
{
  "success": true,
  "message": "Подписка успешно создана",
  "subscription": {
    "planType": "PLAN_30_DAYS",
    "startDate": "2025-01-27T10:00:00",
    "endDate": "2025-02-26T10:00:00",
    "price": 1499.0
  }
}
```

### 4. Отмена подписки

**POST** `/api/subscription/cancel`

**Описание:** Отмена текущей подписки

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Ответ при успехе:**
```json
{
  "success": true,
  "message": "Подписка успешно отменена"
}
```

### 5. Создание бесплатной подписки

**POST** `/api/subscription/create-trial`

**Описание:** Создание бесплатной тестовой подписки

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Body:**
```json
{
  "email": "user@example.com"
}
```

**Ответ при успехе:**
```json
{
  "success": true,
  "message": "Бесплатная подписка успешно активирована",
  "subscription": {
    "planType": "PLAN_FREE",
    "startDate": "2025-01-27T10:00:00",
    "endDate": "2025-02-03T10:00:00",
    "price": 0.0
  }
}
```

---

## 📊 АНАЛИТИКА (/api/analytics)

### 1. Финансовый отчет

**GET** `/api/analytics/financial`

**Описание:** Получение финансовых данных по неделям

**Query Parameters:**
- `days` (optional): Количество дней для анализа (по умолчанию 30)

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Ответ при успехе:**
```json
{
  "success": true,
  "data": {
    "weeks": [
      {
        "week": "2025-01-20 - 2025-01-26",
        "date": "2025-01-26",
        "buyoutQuantity": 47,
        "salesWb": 52640,
        "toCalculateForGoods": 45230,
        "logistics": 8420,
        "storage": 1280,
        "acceptance": 450,
        "penalty": 0,
        "retentions": 2150,
        "toPay": 33930,
        "tax": 5428,
        "otherExpenses": 1200,
        "costOfGoodsSold": 25480,
        "netProfit": 1822,
        "drr": 6.3
      }
    ],
    "totals": {
      "totalBuyoutQuantity": 132,
      "totalSalesWb": 145640,
      "totalToCalculateForGoods": 125230,
      "totalLogistics": 23320,
      "totalStorage": 3540,
      "totalAcceptance": 1245,
      "totalPenalty": 0,
      "totalRetentions": 5950,
      "totalToPay": 94175,
      "totalTax": 15028,
      "totalOtherExpenses": 3320,
      "totalCostOfGoodsSold": 70580,
      "totalNetProfit": 5247,
      "avgDrr": 7.1
    }
  }
}
```

### 2. Юнит экономика

**GET** `/api/analytics/unit-economics`

**Описание:** Получение данных по юнит экономике товаров

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Ответ при успехе:**
```json
{
  "success": true,
  "data": {
    "totalItems": 2,
    "avgRoi": 2.1,
    "avgGrossProfit": 8.2,
    "items": [
      {
        "nmId": "166658151",
        "vendorCode": "DP02/черный",
        "xyz": "X",
        "costPrice": 850,
        "mpPriceBefore": 2100,
        "priceBeforeSpp": 1785,
        "sppPercent": 22,
        "priceAfterSpp": 1392,
        "mpDiscount": 15,
        "buyout": 85,
        "revenueAfterTax": 938,
        "tax": 179,
        "mpCommissionPercent": 12,
        "mpCommissionRub": 167,
        "logisticsMp": 42,
        "logisticsWithBuyout": 48,
        "deliveryToWb": 45,
        "deliveryFirstLiter": 42,
        "deliveryNextLiter": 18,
        "storageMp": 8,
        "warehouseCoeff": 1.2,
        "totalMp": 275,
        "totalWithIndex": 52,
        "totalToPay": 1117,
        "grossProfit": 1205,
        "grossProfitFinal": 43,
        "grossProfitability": 5.1,
        "roi": 3.2,
        "rom": 2.8,
        "finalMarginality": 3.8,
        "markupFromFinalPrice": 4.6,
        "breakEvenBeforeSpp": 1210,
        "volumeLiters": 3.0,
        "length": 8,
        "width": 15,
        "height": 25
      }
    ]
  }
}
```

### 3. РК таблица (реклама)

**GET** `/api/analytics/advertising`

**Описание:** Получение данных по рекламным кампаниям

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Ответ при успехе:**
```json
{
  "success": true,
  "data": {
    "totalCampaigns": 2,
    "totalSpend": 15420.50,
    "totalRevenue": 85600.00,
    "avgRoas": 5.55,
    "campaigns": [
      {
        "campaignId": "12345",
        "campaignName": "Зимняя коллекция",
        "campaignType": "SEARCH",
        "status": "ACTIVE",
        "dailyBudget": 5000.0,
        "totalSpend": 8920.50,
        "totalRevenue": 52300.00,
        "clicks": 1250,
        "impressions": 8500,
        "ctr": 14.7,
        "cpc": 7.14,
        "cr": 8.5,
        "roas": 5.86,
        "startDate": "2025-01-20",
        "endDate": "2025-01-26",
        "products": [
          {
            "nmId": "166658151",
            "vendorCode": "DP02/черный",
            "spend": 4200.25,
            "revenue": 28900.00,
            "clicks": 680,
            "roas": 6.88
          }
        ]
      }
    ]
  }
}
```

### 4. ABC-анализ товаров

**GET** `/api/analytics/abc-analysis`

**Описание:** Получение ABC-анализа товаров с расчетом классов по выручке

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Ответ при успехе:**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "position": 1,
        "nmId": 166658151,
        "vendorCode": "DP02/черный",
        "subject": "Сумка",
        "ordersCount": 120,
        "avgPrice": 1785.50,
        "revenue": 214260.0,
        "revenuePercentInGroup": 35.2,
        "cumulativePercentInGroup": 35.2,
        "avgValueInGroup": 101833.33,
        "deviationCoeffInGroup": 2.1,
        "classInGroup": "A",
        "revenuePercentTotal": 22.5,
        "cumulativePercentTotal": 22.5,
        "avgValueTotal": 63333.33,
        "deviationCoeffTotal": 3.38,
        "classTotal": "A"
      }
    ],
    "summary": {
      "totalItems": 6,
      "totalRevenue": 656100.0,
      "classA": {
        "count": 3,
        "revenue": 553270.0,
        "percent": 84.3
      },
      "classB": {
        "count": 2,
        "revenue": 90580.0,
        "percent": 13.8
      },
      "classC": {
        "count": 1,
        "revenue": 12250.0,
        "percent": 1.9
      }
    }
  }
}
```

---

## 🔓 ПУБЛИЧНЫЕ ЭНДПОИНТЫ (/api/public)

### 1. Создание бесплатной подписки (публичный)

**POST** `/api/public/subscription/free`

**Описание:** Публичный эндпоинт для создания бесплатной подписки без JWT

**Body:**
```json
{
  "email": "user@example.com"
}
```

**Ответ при успехе:**
```json
{
  "success": true,
  "message": "Бесплатная подписка успешно активирована"
}
```

### 2. Отладочная информация о подписках

**GET** `/api/public/subscription/debug`

**Описание:** Публичный эндпоинт для отладки подписок

**Query Parameters:**
- `email` (required): Email пользователя

**Пример:** `/api/public/subscription/debug?email=user@example.com`

**Ответ при успехе:**
```json
{
  "success": true,
  "debug": {
    "userFound": true,
    "userEmail": "user@example.com",
    "subscriptions": [
      {
        "id": 1,
        "planType": "PLAN_FREE",
        "status": "ACTIVE",
        "startDate": "2025-01-27T10:00:00",
        "endDate": "2025-02-03T10:00:00"
      }
    ],
    "activeSubscriptions": 1
  }
}
```

---

## ⚠️ КОДЫ ОШИБОК

### HTTP Status Codes
- `200 OK` - Успешный запрос
- `400 Bad Request` - Неверный запрос
- `401 Unauthorized` - Требуется авторизация
- `403 Forbidden` - Доступ запрещен
- `404 Not Found` - Ресурс не найден
- `500 Internal Server Error` - Внутренняя ошибка сервера

### Стандартный формат ошибки
```json
{
  "success": false,
  "message": "Описание ошибки"
}
```

### Типичные ошибки

#### Авторизация
```json
{
  "success": false,
  "message": "Требуется авторизация"
}
```

#### Недействительный токен
```json
{
  "success": false,
  "message": "Недействительный токен"
}
```

#### Пользователь не найден
```json
{
  "success": false,
  "message": "Пользователь не найден"
}
```

#### Подписка не найдена
```json
{
  "success": false,
  "message": "У пользователя нет активной подписки"
}
```

---

## 🛠️ ДОПОЛНИТЕЛЬНЫЕ ФУНКЦИИ

### JWT Token Refresh
При получении ошибки 401, клиент должен попытаться обновить токен через повторную авторизацию.

### Rate Limiting
API имеет встроенную защиту от множественных запросов. Рекомендуемый интервал между запросами - 1 секунда.

### Demo Data
Если у пользователя нет API ключа Wildberries или активной подписки, API возвращает демонстрационные данные для всех аналитических эндпоинтов.

### Логирование
Все API запросы логируются в консоль сервера с подробной информацией для отладки.

---

## 💡 ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ

### JavaScript/Axios

```javascript
// Авторизация
const loginResponse = await axios.post('http://localhost:8080/api/auth/login', {
  email: 'user@example.com',
  password: 'password123'
});

const token = loginResponse.data.token;

// Получение финансовых данных
const analyticsResponse = await axios.get('http://localhost:8080/api/analytics/financial', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

console.log(analyticsResponse.data);
```

### cURL

```bash
# Авторизация
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Получение данных аналитики
curl -X GET http://localhost:8080/api/analytics/financial \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📝 CHANGELOG

### v1.0 (Текущая версия)
- ✅ Система аутентификации с JWT
- ✅ Управление подписками
- ✅ Финансовая аналитика
- ✅ Юнит экономика
- ✅ РК таблица (реклама)
- ✅ Интеграция с Wildberries API
- ✅ Telegram Bot для верификации
- ✅ Публичные эндпоинты
- ✅ Demo данные для тестирования

## ⚙️ КОНФИГУРАЦИЯ

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wilberis_analytics
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    generate-ddl: true
    show-sql: false
  
  sql:
    init:
      mode: always
      data-locations: classpath:database_setup.sql
      continue-on-error: true

server:
  port: 8080

telegram:
  bot:
    token: "YOUR_TELEGRAM_BOT_TOKEN"
    username: "YourBotUsername"

logging:
  level:
    org.example: DEBUG
    org.springframework.security: DEBUG
```

### SecurityConfig.java - основные настройки
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    // CORS конфигурация
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000", "http://localhost:5173",
            "http://127.0.0.1:5173", "http://127.0.0.1:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        return source;
    }
    
    // Публичные эндпоинты
    .requestMatchers("/api/public/**").permitAll()
    .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/verify").permitAll()
    .requestMatchers("/api/analytics/**").permitAll()
}
```

## 🚀 РАЗВЕРТЫВАНИЕ

### Требования системы
- **Java:** 11+
- **Maven:** 3.6+
- **PostgreSQL:** 12+
- **RAM:** минимум 512MB
- **Disk:** 100MB

### Шаги запуска

1. **Клонирование репозитория:**
```bash
git clone <repository-url>
cd BackWilberisProject
```

2. **Настройка базы данных:**
```sql
CREATE DATABASE wilberis_analytics;
CREATE USER wilberis WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE wilberis_analytics TO wilberis;
```

3. **Настройка конфигурации:**
```bash
cp src/main/resources/application-example.yml src/main/resources/application.yml
# Отредактируйте application.yml с вашими настройками
```

4. **Сборка и запуск:**
```bash
mvn clean install
mvn spring-boot:run
```

5. **Проверка:**
```bash
curl http://localhost:8080/api/auth/register
```

### Docker развертывание
```dockerfile
FROM openjdk:11-jre-slim
COPY target/wilberis-analytics-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
docker build -t wilberis-analytics .
docker run -p 8080:8080 wilberis-analytics
```

## 🐛 ОТЛАДКА И ЛОГИРОВАНИЕ

### Основные логи
```java
// В контроллерах
log.info("Processing request: {}", request);
log.error("Error occurred: ", exception);

// В сервисах  
log.debug("User authenticated: {}", user.getEmail());
log.warn("Invalid API key provided");
```

### Типичные проблемы

**1. JWT Token expired**
```
Решение: Проверить системное время, настроить clock skew
```

**2. CORS ошибки**
```
Решение: Добавить origin в SecurityConfig.allowedOrigins
```

**3. Database connection failed**
```
Решение: Проверить настройки в application.yml
```

**4. Telegram bot not responding**
```
Решение: Проверить токен бота в конфигурации
```

### Мониторинг
```bash
# Проверка статуса приложения
curl http://localhost:8080/actuator/health

# Логи в реальном времени
tail -f logs/application.log

# Мониторинг базы данных
SELECT * FROM pg_stat_activity WHERE datname = 'wilberis_analytics';
```

## 📊 ПРОИЗВОДИТЕЛЬНОСТЬ

### Оптимизация запросов
- Используйте индексы для часто запрашиваемых полей
- Ограничивайте размер результата с помощью Pageable
- Кэшируйте статичные данные

### Рекомендации
- **Пул соединений:** HikariCP (по умолчанию в Spring Boot)
- **Кэширование:** Redis для сессий и часто используемых данных
- **Rate limiting:** Ограничение запросов к внешним API

---

**📞 Техническая поддержка:** Telegram @SellersWilberis_bot  
**🌐 Базовый URL:** http://localhost:8080  
**📅 Последнее обновление:** 27.01.2025

## 📋 CHANGELOG

### v1.0.0 (27.01.2025)
- ✅ Полная система аутентификации
- ✅ Управление подписками
- ✅ Интеграция с Wildberries API
- ✅ ABC-анализ и аналитика
- ✅ Telegram Bot верификация
- ✅ Публичные эндпоинты
- ✅ Demo данные для тестирования
- ✅ Полная документация API 