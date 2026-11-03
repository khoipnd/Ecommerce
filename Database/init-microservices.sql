-- ====================================================
-- Init all databases for Microservices
-- ====================================================

-- 1. Ecommerce_Auth
IF DB_ID(N'Ecommerce_Auth') IS NULL
    CREATE DATABASE Ecommerce_Auth;
GO

-- 2. Ecommerce_Category
IF DB_ID(N'Ecommerce_Category') IS NULL
    CREATE DATABASE Ecommerce_Category;
GO

-- 3. Ecommerce_Products
IF DB_ID(N'Ecommerce_Products') IS NULL
    CREATE DATABASE Ecommerce_Products;
GO

-- 4. Ecommerce_Order
IF DB_ID(N'Ecommerce_Order') IS NULL
    CREATE DATABASE Ecommerce_Order;
GO

-- Schemas for Category
USE Ecommerce_Category;
GO
IF OBJECT_ID('dbo.category', 'U') IS NULL
CREATE TABLE category (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE
);
GO

-- Schemas for Products
USE Ecommerce_Products;
GO
IF OBJECT_ID('dbo.product', 'U') IS NULL
CREATE TABLE product (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(500),
    price DECIMAL(18,2) NOT NULL,
    stock_quantity INT NOT NULL,
    category_id BIGINT NOT NULL
);
GO

-- Schemas for Orders
USE Ecommerce_Order;
GO
IF OBJECT_ID('dbo.orders', 'U') IS NULL
CREATE TABLE orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    account_id BIGINT NOT NULL,
    receiver_name NVARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    shipping_address NVARCHAR(500) NOT NULL,
    note NVARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(12,2) NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME()
);
GO

IF OBJECT_ID('dbo.order_item', 'U') IS NULL
CREATE TABLE order_item (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name NVARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    UNIQUE (order_id, product_id)
);
GO

