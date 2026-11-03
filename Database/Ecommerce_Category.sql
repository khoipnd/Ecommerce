USE master;
GO

IF DB_ID('Ecommerce_Category') IS NULL
    CREATE DATABASE Ecommerce_Category;
GO

USE Ecommerce_Category;
GO

IF OBJECT_ID('dbo.category', 'U') IS NULL
CREATE TABLE category (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE
);
GO
