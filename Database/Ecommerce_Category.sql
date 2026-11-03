 USE master;
GO

IF DB_ID('Ecommerce_Category') IS NOT NULL
BEGIN
    ALTER DATABASE Ecommerce_Category SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE Ecommerce_Category;
END
GO

CREATE DATABASE Ecommerce_Category;
GO

USE Ecommerce_Category;
GO

CREATE TABLE category (
                          id BIGINT IDENTITY(1,1) PRIMARY KEY,
                          name VARCHAR(100) NOT NULL UNIQUE
);
GO