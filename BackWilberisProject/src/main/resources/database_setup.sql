-- PostgreSQL Database Setup Script for Wilberis Analytics
-- Note: Database should be created manually or via application.yml configuration

-- Create tables if they don't exist

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    is_verified BOOLEAN DEFAULT FALSE,
    verification_code VARCHAR(10),
    verification_expires_at TIMESTAMP,
    telegram_chat_id BIGINT,
    wb_api_key VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Subscriptions table with enum support
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_type VARCHAR(50) NOT NULL DEFAULT 'PLAN_FREE',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP NOT NULL,
    price DECIMAL(10,2) DEFAULT 0.00,
    auto_renew BOOLEAN DEFAULT FALSE,
    payment_method VARCHAR(100),
    payment_transaction_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_end_date ON subscriptions(end_date);

-- Insert sample data if tables are empty
INSERT INTO users (email, password, first_name, last_name, is_verified) 
SELECT '123321@mail.ru', '$2a$10$example.hash.here', 'Test', 'User', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = '123321@mail.ru');

INSERT INTO users (email, password, first_name, last_name, is_verified) 
SELECT '123123123@mail.ru', '$2a$10$example.hash.here', 'Test2', 'User2', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = '123123123@mail.ru'); 