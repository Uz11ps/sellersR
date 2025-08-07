-- Создание базы данных
CREATE DATABASE IF NOT EXISTS wilberis_analytics;
USE wilberis_analytics;

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    telegram_chat_id BIGINT,
    verification_code VARCHAR(10),
    is_verified BOOLEAN DEFAULT FALSE,
    verification_expires_at DATETIME,
    wb_api_key VARCHAR(1000),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_verification_code (verification_code),
    INDEX idx_telegram_chat_id (telegram_chat_id)
);

-- 🔥 НОВАЯ ТАБЛИЦА: Подписки пользователей
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_type ENUM('PLAN_30_DAYS', 'PLAN_60_DAYS', 'PLAN_90_DAYS') NOT NULL,
    status ENUM('ACTIVE', 'EXPIRED', 'CANCELLED', 'PENDING') NOT NULL DEFAULT 'PENDING',
    price DECIMAL(10,2) NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    auto_renew BOOLEAN NOT NULL DEFAULT FALSE,
    payment_method VARCHAR(50),
    payment_transaction_id VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_end_date (end_date),
    INDEX idx_auto_renew (auto_renew),
    INDEX idx_payment_transaction_id (payment_transaction_id)
);

-- Таблица продавцов
CREATE TABLE IF NOT EXISTS sellers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    seller_name VARCHAR(255) NOT NULL,
    inn VARCHAR(20),
    wb_api_key VARCHAR(1000),
    wb_seller_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_sync_at DATETIME,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_inn (inn),
    INDEX idx_wb_seller_id (wb_seller_id),
    INDEX idx_is_active (is_active)
);

-- Таблица товаров
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    seller_id BIGINT,
    nm_id BIGINT,
    vendor_code VARCHAR(100),
    brand VARCHAR(200),
    category VARCHAR(200),
    name VARCHAR(500),
    stock INTEGER DEFAULT 0,
    price DECIMAL(10,2),
    discount_price DECIMAL(10,2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES sellers(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_nm_id (nm_id),
    INDEX idx_vendor_code (vendor_code),
    INDEX idx_brand (brand),
    INDEX idx_category (category)
);

-- Таблица аналитических данных
CREATE TABLE IF NOT EXISTS analytics_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    seller_id BIGINT,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    period_type VARCHAR(50) NOT NULL,
    orders_count INTEGER DEFAULT 0,
    sold_quantity INTEGER DEFAULT 0,
    sales_amount DECIMAL(15,2) DEFAULT 0,
    price DECIMAL(10,2) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES sellers(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_period_start (period_start),
    INDEX idx_period_end (period_end),
    INDEX idx_period_type (period_type)
);

-- Таблица юнит-экономики
CREATE TABLE IF NOT EXISTS unit_economics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    wb_article VARCHAR(100) NOT NULL,
    supplier_article VARCHAR(100),
    cost_price DECIMAL(10,2) NOT NULL,
    gross_profit DECIMAL(10,2),
    roi DECIMAL(8,4),
    calculation_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (seller_id) REFERENCES sellers(id) ON DELETE CASCADE,
    INDEX idx_seller_id (seller_id),
    INDEX idx_wb_article (wb_article),
    INDEX idx_supplier_article (supplier_article),
    INDEX idx_calculation_date (calculation_date)
);

-- Таблица еженедельных финансовых отчетов
CREATE TABLE IF NOT EXISTS weekly_financial_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    week_number INTEGER NOT NULL,
    date_period DATE NOT NULL,
    total_orders INTEGER DEFAULT 0,
    total_sales DECIMAL(15,2) DEFAULT 0,
    logistics_cost DECIMAL(15,2) DEFAULT 0,
    net_profit DECIMAL(15,2) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (seller_id) REFERENCES sellers(id) ON DELETE CASCADE,
    INDEX idx_seller_id (seller_id),
    INDEX idx_week_number (week_number),
    INDEX idx_date_period (date_period)
);

-- Таблица учета акций
CREATE TABLE IF NOT EXISTS promotions_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    product_id BIGINT,
    row_number INTEGER NOT NULL,
    wb_article VARCHAR(100),
    supplier_article VARCHAR(100),
    grouping VARCHAR(200),
    abc_analysis VARCHAR(50),
    subgroup_f_preparation_d_sale VARCHAR(100),
    gross_profit DECIMAL(12,2),
    current_price DECIMAL(12,2),
    action VARCHAR(200),
    price_for_promotion_participation DECIMAL(12,2),
    gross_profit_in_promotion DECIMAL(12,2),
    turnover_days DECIMAL(8,2),
    wb_stock_balance DECIMAL(10,0),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (seller_id) REFERENCES sellers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
    INDEX idx_seller_id (seller_id),
    INDEX idx_product_id (product_id),
    INDEX idx_wb_article (wb_article),
    INDEX idx_supplier_article (supplier_article),
    INDEX idx_abc_analysis (abc_analysis),
    INDEX idx_subgroup_f_preparation_d_sale (subgroup_f_preparation_d_sale)
);

-- Таблица рекламных кампаний
CREATE TABLE IF NOT EXISTS advertising_campaigns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    product_id BIGINT,
    row_number INTEGER NOT NULL,
    wb_article VARCHAR(100),
    supplier_article VARCHAR(100),
    grouping VARCHAR(200),
    indicator VARCHAR(100),
    week_1_period VARCHAR(50),
    week_1_value DECIMAL(12,2),
    week_2_period VARCHAR(50),
    week_2_value DECIMAL(12,2),
    week_3_period VARCHAR(50),
    week_3_value DECIMAL(12,2),
    week_4_period VARCHAR(50),
    week_4_value DECIMAL(12,2),
    week_5_period VARCHAR(50),
    week_5_value DECIMAL(12,2),
    calculation DECIMAL(12,2),
    report_period_start DATE NOT NULL,
    report_period_end DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (seller_id) REFERENCES sellers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
    INDEX idx_seller_id (seller_id),
    INDEX idx_product_id (product_id),
    INDEX idx_wb_article (wb_article),
    INDEX idx_supplier_article (supplier_article),
    INDEX idx_grouping (grouping),
    INDEX idx_indicator (indicator),
    INDEX idx_report_period_start (report_period_start),
    INDEX idx_report_period_end (report_period_end)
);

-- Таблица планирования поставок
CREATE TABLE IF NOT EXISTS supply_planning (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    wb_article VARCHAR(100),
    supplier_article VARCHAR(100),
    product_name VARCHAR(500),
    current_stock DECIMAL(10,0),
    orders_per_day DECIMAL(8,2),
    plan_days DECIMAL(8,2),
    seasonality_coefficient DECIMAL(6,4) DEFAULT 1.0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (seller_id) REFERENCES sellers(id) ON DELETE CASCADE,
    INDEX idx_seller_id (seller_id),
    INDEX idx_wb_article (wb_article),
    INDEX idx_supplier_article (supplier_article)
);

-- Вставка тестовых данных
INSERT IGNORE INTO users (email, password, first_name, last_name, is_verified, wb_api_key) 
VALUES 
('test@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Test', 'User', TRUE, 'test_api_key_123'),
('user2@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'User', 'Two', TRUE, 'test_api_key_456');

-- 🔥 ТЕСТОВЫЕ ПОДПИСКИ
-- Вставляем активную подписку для тестового пользователя
INSERT IGNORE INTO subscriptions (user_id, plan_type, status, price, start_date, end_date, auto_renew, payment_method, payment_transaction_id)
SELECT 
    u.id,
    'PLAN_30_DAYS',
    'ACTIVE',
    1499.00,
    NOW(),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    FALSE,
    'TEST_PAYMENT',
    'TEST_TXN_001'
FROM users u 
WHERE u.email = 'test@example.com'
AND NOT EXISTS (SELECT 1 FROM subscriptions s WHERE s.user_id = u.id);

-- Вставляем истекающую подписку для второго пользователя
INSERT IGNORE INTO subscriptions (user_id, plan_type, status, price, start_date, end_date, auto_renew, payment_method, payment_transaction_id)
SELECT 
    u.id,
    'PLAN_60_DAYS',
    'ACTIVE',
    2799.00,
    DATE_SUB(NOW(), INTERVAL 53 DAY),
    DATE_ADD(NOW(), INTERVAL 7 DAY),
    TRUE,
    'TEST_PAYMENT',
    'TEST_TXN_002'
FROM users u 
WHERE u.email = 'user2@example.com'
AND NOT EXISTS (SELECT 1 FROM subscriptions s WHERE s.user_id = u.id);

-- Вставляем тестовых продавцов
INSERT IGNORE INTO sellers (user_id, seller_name, inn, wb_api_key, wb_seller_id, is_active)
SELECT u.id, 'ИП Тестовый', '123456789012', u.wb_api_key, 12345, TRUE
FROM users u WHERE u.email = 'test@example.com';

INSERT IGNORE INTO sellers (user_id, seller_name, inn, wb_api_key, wb_seller_id, is_active)
SELECT u.id, 'ООО Тестовое', '987654321098', u.wb_api_key, 67890, TRUE
FROM users u WHERE u.email = 'user2@example.com';

-- Вставляем тестовые данные юнит-экономики
INSERT IGNORE INTO unit_economics (seller_id, wb_article, supplier_article, cost_price, gross_profit, roi)
SELECT s.id, '12345', 'SUP12345', 500.00, 300.00, 0.60
FROM sellers s 
WHERE s.seller_name = 'ИП Тестовый';

INSERT IGNORE INTO unit_economics (seller_id, wb_article, supplier_article, cost_price, gross_profit, roi)
SELECT s.id, '12346', 'SUP12346', 750.00, 450.00, 0.60
FROM sellers s 
WHERE s.seller_name = 'ИП Тестовый';

-- Вставляем тестовые данные акций
INSERT IGNORE INTO promotions_tracking (seller_id, row_number, wb_article, supplier_article, grouping, abc_analysis, gross_profit, current_price, action, price_for_promotion_participation, gross_profit_in_promotion)
SELECT s.id, 1, '12345', 'SUP12345', 'Группа 1', 'A', 300.00, 1200.00, 'Скидка 20%', 960.00, 240.00
FROM sellers s 
WHERE s.seller_name = 'ИП Тестовый';

-- Вставляем тестовые данные рекламных кампаний
INSERT IGNORE INTO advertising_campaigns (seller_id, row_number, wb_article, supplier_article, grouping, indicator, week_1_period, week_1_value, week_2_value, week_3_value, calculation, report_period_start, report_period_end)
SELECT s.id, 1, '12345', 'SUP12345', 'Группа 1', 'CTR', '01.01-07.01', 5.2, 5.8, 6.1, 5.7, '2024-01-01', '2024-01-31'
FROM sellers s 
WHERE s.seller_name = 'ИП Тестовый';

-- Вставляем тестовые данные планирования поставок
INSERT IGNORE INTO supply_planning (seller_id, wb_article, supplier_article, product_name, current_stock, orders_per_day, plan_days, seasonality_coefficient)
SELECT s.id, '12345', 'SUP12345', 'Тестовый товар 1', 100, 3.5, 30, 1.0
FROM sellers s 
WHERE s.seller_name = 'ИП Тестовый';

-- Вставляем тестовые еженедельные отчеты
INSERT IGNORE INTO weekly_financial_reports (seller_id, week_number, date_period, total_orders, total_sales, logistics_cost, net_profit)
SELECT s.id, 1, '2024-01-01', 50, 25000.00, 2500.00, 15000.00
FROM sellers s 
WHERE s.seller_name = 'ИП Тестовый';

-- Проверяем результат
SELECT 'Пользователи:' as info, COUNT(*) as count FROM users
UNION ALL
SELECT 'Подписки:', COUNT(*) FROM subscriptions
UNION ALL
SELECT 'Продавцы:', COUNT(*) FROM sellers
UNION ALL
SELECT 'Юнит-экономика:', COUNT(*) FROM unit_economics
UNION ALL
SELECT 'Акции:', COUNT(*) FROM promotions_tracking
UNION ALL
SELECT 'Рекламные кампании:', COUNT(*) FROM advertising_campaigns
UNION ALL
SELECT 'Планирование поставок:', COUNT(*) FROM supply_planning; 