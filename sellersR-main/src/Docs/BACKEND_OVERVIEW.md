# 🏗️ WILBERIS ANALYTICS - АРХИТЕКТУРА BACKEND

## 📋 КРАТКИЙ ОБЗОР

**Wilberis Analytics** - это Spring Boot приложение для аналитики продавцов на маркетплейсе Wildberries. Система предоставляет полный цикл работы с пользователями: регистрация, аутентификация, управление подписками и отображение аналитических данных.

## 🎯 ОСНОВНЫЕ ФУНКЦИИ

- ✅ **Аутентификация** - JWT токены + Telegram верификация
- ✅ **Подписки** - Управление планами и доступом пользователей  
- ✅ **Аналитика** - Финансовые отчеты, юнит-экономика, ABC-анализ
- ✅ **Интеграции** - Wildberries API + Telegram Bot
- ✅ **Безопасность** - Spring Security + CORS

## 🏛️ АРХИТЕКТУРА

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   FRONTEND      │    │    BACKEND      │    │   EXTERNAL      │
│   (React)       │    │  (Spring Boot)  │    │     APIs        │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ • Auth Modal    │───→│ • AuthController│───→│ • Telegram Bot  │
│ • Profile Page  │    │ • UserService   │    │ • Wildberries   │
│ • Analytics     │    │ • JwtFilter     │    │   API           │
│ • Subscriptions │    │ • SecurityConfig│    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                       ┌─────────────────┐
                       │   DATABASE      │
                       │  (PostgreSQL)   │
                       ├─────────────────┤
                       │ • users         │
                       │ • subscriptions │
                       │ • analytics     │
                       └─────────────────┘
```

## 🗂️ СТРУКТУРА ПРОЕКТА

```
src/main/java/org/example/
├── 📁 config/                   # Конфигурации системы
│   ├── SecurityConfig.java      # Spring Security + CORS
│   ├── TelegramBotConfig.java   # Настройки Telegram бота
│   └── WebConfig.java           # Web конфигурация
│
├── 📁 controller/               # REST контроллеры
│   ├── AuthController.java      # Аутентификация
│   ├── SubscriptionController.java # Управление подписками
│   ├── AnalyticsController.java # Аналитические данные
│   └── PublicController.java    # Публичные эндпоинты
│
├── 📁 service/                  # Бизнес-логика
│   ├── AuthService.java         # Логика аутентификации
│   ├── JwtService.java          # Работа с JWT токенами
│   ├── SubscriptionService.java # Логика подписок
│   ├── TelegramBotService.java  # Telegram интеграция
│   └── WildberriesApiService.java # Wildberries интеграция
│
├── 📁 entity/                   # JPA сущности
│   ├── User.java                # Пользователи
│   ├── Subscription.java        # Подписки
│   └── AnalyticsData.java       # Аналитические данные
│
├── 📁 repository/               # Репозитории для БД
│   ├── UserRepository.java
│   ├── SubscriptionRepository.java
│   └── AnalyticsDataRepository.java
│
└── 📁 dto/                      # Data Transfer Objects
    ├── auth/                    # DTO для аутентификации
    └── admin/                   # DTO для админки
```

## 🔐 БЕЗОПАСНОСТЬ

### JWT Аутентификация
```java
// Жизненный цикл токена
1. User Login → JWT Token (24h)
2. Frontend → Authorization: Bearer <token>
3. JwtAuthenticationFilter → Validation
4. SecurityContext → User Access
```

### Публичные эндпоинты
- `/api/auth/register` - Регистрация
- `/api/auth/login` - Вход  
- `/api/auth/verify` - Верификация
- `/api/public/**` - Публичные функции
- `/api/analytics/**` - Демо данные

### Приватные эндпоинты  
- `/api/auth/user-info` - Профиль пользователя
- `/api/auth/api-key` - Управление API ключами
- `/api/subscription/**` - Управление подписками

## 🗄️ БАЗА ДАННЫХ

### Основные таблицы

**users** - Пользователи системы
```sql
id, email, password_hash, first_name, last_name, 
phone_number, telegram_chat_id, wb_api_key, 
is_verified, verification_code, created_at, updated_at
```

**subscriptions** - Подписки пользователей
```sql
id, user_id, plan_type, status, start_date, end_date,
price, auto_renew, payment_method, created_at, updated_at
```

### Индексы для производительности
- `idx_users_email` - Поиск по email
- `idx_users_telegram_chat_id` - Telegram интеграция
- `idx_subscriptions_user_id` - Подписки пользователя
- `idx_subscriptions_status` - Активные подписки

## 🔄 ПОТОК ДАННЫХ

### 1. Регистрация пользователя
```
Frontend → AuthController.register() → AuthService.register() 
    → UserRepository.save() → TelegramBotService.sendCode()
```

### 2. Аутентификация
```
Frontend → AuthController.login() → AuthService.authenticate()
    → JwtService.generateToken() → Response with JWT
```

### 3. Загрузка аналитики
```
Frontend → AnalyticsController.getFinancialData() 
    → WildberriesApiService.fetchData() → Demo/Real Data
```

### 4. Управление подписками
```
Frontend → SubscriptionController.createSubscription()
    → SubscriptionService.createSubscription() → Database
```

## 📡 ВНЕШНИЕ ИНТЕГРАЦИИ

### Telegram Bot
- **Цель:** Верификация пользователей
- **API:** Telegram Bot API
- **Функции:** Отправка кодов, уведомления

### Wildberries API  
- **Цель:** Получение аналитических данных
- **Аутентификация:** API ключ пользователя
- **Данные:** Продажи, финансы, реклама, ABC-анализ

## 🚀 ЗАПУСК СИСТЕМЫ

### Требования
- Java 11+
- Maven 3.6+
- PostgreSQL 12+
- Telegram Bot Token

### Быстрый старт
```bash
# 1. Клонирование
git clone <repository>
cd BackWilberisProject

# 2. Настройка БД
createdb wilberis_analytics

# 3. Конфигурация
cp application-example.yml application.yml
# Настроить DB и Telegram токен

# 4. Запуск
mvn spring-boot:run
```

### Проверка
```bash
curl http://localhost:8080/api/auth/register
```

## 📊 МОНИТОРИНГ

### Основные метрики
- **Регистрации:** Количество новых пользователей
- **Аутентификация:** Успешные/неуспешные входы
- **Подписки:** Активные/истекшие планы
- **API запросы:** Частота обращений к Wildberries

### Логирование
```java
log.info("User registered: {}", user.getEmail());
log.warn("Failed login attempt: {}", request.getEmail());
log.error("Wildberries API error: ", exception);
```

## 🔧 НАСТРОЙКИ PRODUCTION

### Безопасность
- Используйте сложные пароли для БД
- Настройте HTTPS для production
- Ограничьте CORS origins
- Используйте Redis для сессий

### Производительность
- Connection pooling (HikariCP)
- Кэширование часто используемых данных
- Индексы для всех поисковых полей
- Rate limiting для внешних API

### Мониторинг
- Spring Boot Actuator
- Prometheus + Grafana
- ELK Stack для логов
- Health checks для сервисов

---

## 📚 ДОПОЛНИТЕЛЬНАЯ ДОКУМЕНТАЦИЯ

- 📖 **Полная API документация:** `API_DOCUMENTATION.md`
- 🛠️ **Настройка разработки:** `README.md`
- 📋 **SQL схема:** `database_setup.sql`
- ⚙️ **Конфигурация:** `application.yml`

**📅 Последнее обновление:** 27.01.2025  
**👨‍💻 Версия:** 1.0.0