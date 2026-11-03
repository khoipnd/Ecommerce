IF DB_ID('Ecommerce_Product') IS NOT NULL
BEGIN
    ALTER DATABASE Ecommerce_Products
    SET SINGLE_USER WITH ROLLBACK IMMEDIATE;

    DROP DATABASE Ecommerce_Products;
END
GO

-- Tạo database Product
CREATE DATABASE Ecommerce_Products;
GO

-- Sử dụng database Product
USE Ecommerce_Products;
GO

-- Tạo bảng Product
CREATE TABLE product (
                         id BIGINT IDENTITY(1,1) PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         description VARCHAR(500),
                         price DECIMAL(18,2) NOT NULL,
                         stock_quantity INT NOT NULL,
                         category_id BIGINT NOT NULL
);
GO