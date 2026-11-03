/* Chạy trong SSMS. Có thể chạy lại nhiều lần, không xóa dữ liệu. */
USE master;
GO
IF DB_ID('Ecommerce_Order') IS NULL CREATE DATABASE Ecommerce_Order;
GO
USE Ecommerce_Order;
GO

IF OBJECT_ID('dbo.orders', 'U') IS NULL
CREATE TABLE orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    account_id BIGINT NOT NULL,               -- id từ auth, không có khóa ngoại
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
    product_id BIGINT NOT NULL,               -- id từ product, không có khóa ngoại
    product_name NVARCHAR(255) NOT NULL,      -- lưu lại tên tại thời điểm đặt
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    UNIQUE (order_id, product_id)
);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_orders_account')
CREATE INDEX ix_orders_account ON orders(account_id);
GO
