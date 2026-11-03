USE master;
GO

IF DB_ID('Ecommerce_Products') IS NULL
    CREATE DATABASE Ecommerce_Products;
GO

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
