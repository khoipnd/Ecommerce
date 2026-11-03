-- SQL Server: bổ sung các bảng còn thiếu, không DROP và không xóa dữ liệu cũ.
-- Script không sửa cấu trúc bảng đã tồn tại; đối chiếu schema nếu bảng cũ khác định nghĩa.
IF DB_ID(N'Ecommerce') IS NULL
    EXEC(N'CREATE DATABASE [Ecommerce]');
GO
USE [Ecommerce];
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID(N'dbo.role', N'U') IS NULL
CREATE TABLE dbo.role (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

IF OBJECT_ID(N'dbo.account', N'U') IS NULL
CREATE TABLE dbo.account (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL REFERENCES dbo.role(id),
    full_name NVARCHAR(100), email VARCHAR(100), phone VARCHAR(20)
);

IF OBJECT_ID(N'dbo.category', N'U') IS NULL
CREATE TABLE dbo.category (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE
);

IF OBJECT_ID(N'dbo.product', N'U') IS NULL
CREATE TABLE dbo.product (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(2000),
    price DECIMAL(12,2) NOT NULL CHECK (price > 0),
    stock_quantity INT NOT NULL CHECK (stock_quantity >= 0),
    category_id BIGINT NOT NULL REFERENCES dbo.category(id)
);

IF OBJECT_ID(N'dbo.orders', N'U') IS NULL
CREATE TABLE dbo.orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES dbo.account(id),
    receiver_name NVARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    shipping_address NVARCHAR(500) NOT NULL,
    note NVARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(12,2) NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME()
);

IF OBJECT_ID(N'dbo.order_item', N'U') IS NULL
CREATE TABLE dbo.order_item (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES dbo.orders(id),
    product_id BIGINT NOT NULL REFERENCES dbo.product(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(12,2) NOT NULL,
    UNIQUE (order_id, product_id)
);

COMMIT TRANSACTION;
GO
-- Backend DataInitializer tạo role và admin khi thiếu; không chèn mật khẩu thô bằng SQL.
