-- ====================================================
-- PostgreSQL Schema Migration for Ecommerce
-- Tương thích hoàn toàn với Render Managed PostgreSQL
-- ====================================================

-- 1. Bảng role
CREATE TABLE IF NOT EXISTS role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Bảng account
CREATE TABLE IF NOT EXISTS account (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL REFERENCES role(id),
    full_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20)
);

-- 3. Bảng category
CREATE TABLE IF NOT EXISTS category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 4. Bảng product
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    price DECIMAL(12,2) NOT NULL CHECK (price > 0),
    stock_quantity INT NOT NULL CHECK (stock_quantity >= 0),
    category_id BIGINT NOT NULL REFERENCES category(id)
);

-- 5. Bảng orders
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES account(id),
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    note VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Bảng order_item
CREATE TABLE IF NOT EXISTS order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(12,2) NOT NULL,
    UNIQUE (order_id, product_id)
);

-- Dữ liệu mẫu ban đầu: Roles
INSERT INTO role (name) 
VALUES ('ADMIN'), ('USER')
ON CONFLICT (name) DO NOTHING;

-- Dữ liệu mẫu: Categories
INSERT INTO category (name)
VALUES ('Electronics'), ('Phone'), ('Laptop'), ('Smartwatch'), ('Headphones'), ('Accessories')
ON CONFLICT (name) DO NOTHING;
