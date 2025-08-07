# 🚀 WILBERIS ANALYTICS BACKEND

> Полная система аналитики для продавцов Wildberries на Spring Boot

## ⚡ БЫСТРЫЙ СТАРТ

```bash
# Запуск сервера
cd BackWilberisProject
mvn spring-boot:run

# Проверка работы
curl http://localhost:8080/api/auth/register
```

## 📁 ДОКУМЕНТАЦИЯ

| Файл | Описание |
|------|----------|
| 📖 [`API_DOCUMENTATION.md`](./API_DOCUMENTATION.md) | **Полная API документация** - все эндпоинты, примеры запросов/ответов |
| 🏗️ [`BACKEND_OVERVIEW.md`](./BACKEND_OVERVIEW.md) | **Архитектура системы** - структура проекта, безопасность, БД |
| 🗄️ [`database_setup.sql`](./database_setup.sql) | **SQL схема** - создание таблиц и индексов |
| ⚙️ [`application.yml`](./src/main/resources/application.yml) | **Конфигурация** - настройки БД, Telegram, логирования |

## 🎯 ОСНОВНЫЕ ВОЗМОЖНОСТИ

✅ **Аутентификация** - JWT токены + Telegram верификация  
✅ **Профиль пользователя** - управление данными и API ключами  
✅ **Подписки** - планы доступа и платежи  
✅ **Аналитика** - финансы, юнит-экономика, ABC-анализ  
✅ **Безопасность** - Spring Security + CORS  
✅ **Интеграции** - Wildberries API + Telegram Bot  

## 🏛️ АРХИТЕКТУРА

```
Frontend (React) → Security Layer → Controllers → Services → Repository → Database
                                      ↓
                               External APIs (Telegram, Wildberries)
```

## 📋 ОСНОВНЫЕ ЭНДПОИНТЫ

### 🔐 Аутентификация (`/api/auth`)
- `POST /register` - Регистрация пользователя
- `POST /login` - Вход в систему  
- `POST /verify` - Верификация через Telegram
- `GET /user-info` - Информация о пользователе
- `PUT /api-key` - Установка Wildberries API ключа

### 💰 Подписки (`/api/subscription`)
- `GET /plans` - Доступные планы
- `GET /info` - Текущая подписка
- `POST /create` - Создание подписки
- `DELETE /cancel` - Отмена подписки

### 📊 Аналитика (`/api/analytics`)
- `GET /financial` - Финансовый отчет
- `GET /unit-economics` - Юнит-экономика  
- `GET /advertising` - Рекламные кампании
- `GET /abc-analysis` - ABC-анализ товаров

### 🌍 Публичные (`/api/public`)
- `POST /subscription/free` - Бесплатная подписка
- `GET /subscription/debug/{email}` - Отладка подписок

## 🗄️ БАЗА ДАННЫХ

### Таблицы
- **users** - пользователи системы
- **subscriptions** - подписки пользователей

### Индексы
- `idx_users_email` - поиск по email
- `idx_subscriptions_user_id` - подписки пользователя
- `idx_subscriptions_status` - активные подписки

## 🔧 НАСТРОЙКА

### 1. База данных (PostgreSQL)
```sql
CREATE DATABASE wilberis_analytics;
CREATE USER wilberis WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE wilberis_analytics TO wilberis;
```

### 2. Конфигурация `application.yml`
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wilberis_analytics
    username: wilberis
    password: password

telegram:
  bot:
    token: "YOUR_TELEGRAM_BOT_TOKEN"
    username: "YourBotUsername"
```

### 3. Запуск
```bash
mvn clean install
mvn spring-boot:run
```

## 🔐 БЕЗОПАСНОСТЬ

### JWT Токены
- Срок действия: 24 часа
- Автоматическое обновление на фронтенде
- Толерантность к сдвигу времени: 5 минут

### CORS
Разрешенные origins:
- `http://localhost:3000`
- `http://localhost:5173`  
- `http://127.0.0.1:5173`
- `http://127.0.0.1:3000`

## 🔌 ИНТЕГРАЦИИ

### Telegram Bot
- Верификация пользователей через Telegram
- Отправка кодов подтверждения
- Уведомления о важных событиях

### Wildberries API
- Получение аналитических данных продавцов
- Финансовые отчеты и статистика продаж
- ABC-анализ товаров

## 📊 ДАННЫЕ

### Demo данные
Система предоставляет демо-данные для всех аналитических отчетов:
- Финансовые отчеты с еженедельной разбивкой
- Юнит-экономика по товарам
- Рекламные кампании и их эффективность  
- ABC-анализ с классификацией товаров

### Реальные данные
При подключении Wildberries API ключа система получает:
- Актуальные продажи и заказы
- Финансовые показатели
- Данные по рекламным кампаниям
- Детальную аналитику по товарам

## 🚨 ТИПИЧНЫЕ ПРОБЛЕМЫ

### JWT Token expired
**Решение:** Проверить системное время, токен обновляется автоматически

### CORS ошибки  
**Решение:** Добавить origin в SecurityConfig.allowedOrigins

### Database connection failed
**Решение:** Проверить настройки БД в application.yml

### Telegram bot not responding
**Решение:** Проверить токен бота в конфигурации

## 📈 ПРОИЗВОДИТЕЛЬНОСТЬ

### Оптимизация
- HikariCP connection pool
- Индексы для часто запрашиваемых полей
- Кэширование статичных данных
- Rate limiting для внешних API

### Мониторинг
```bash
# Статус приложения
curl http://localhost:8080/actuator/health

# Логи в реальном времени  
tail -f logs/application.log
```

## 🐳 DOCKER

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

---

## 📞 ПОДДЕРЖКА

**Telegram:** @SellersWilberis_bot  
**Email:** support@wilberis-analytics.com  
**Документация:** См. файлы в этой папке

**📅 Последнее обновление:** 27.01.2025  
**🏷️ Версия:** 1.0.0