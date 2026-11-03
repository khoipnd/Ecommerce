
-- =====================================================================
-- Ecommerce_Auth.sql : database riêng của auth-service (SQL Server)
-- Khớp với entity Role / User trong auth-service (Hibernate ddl-auto=update).
-- Chạy file này TRƯỚC khi start service, hoặc chỉ cần tạo database rồi để
-- Hibernate tự tạo bảng (khi đó bỏ qua phần CREATE TABLE).
-- =====================================================================

IF DB_ID(N'Ecommerce_Auth') IS NULL
    CREATE DATABASE Ecommerce_Auth;
GO

USE Ecommerce_Auth;
GO

-- ---------- Bảng roles: ADMIN, USER ----------
IF OBJECT_ID(N'dbo.roles', N'U') IS NULL
BEGIN
CREATE TABLE dbo.roles (
                           id   BIGINT IDENTITY(1,1) NOT NULL,
                           name VARCHAR(50)          NOT NULL,
                           CONSTRAINT PK_roles PRIMARY KEY (id),
                           CONSTRAINT UQ_roles_name UNIQUE (name)
);
END
GO

-- ---------- Bảng users: password chỉ lưu hash BCrypt ----------
IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
CREATE TABLE dbo.users (
                           id         BIGINT IDENTITY(1,1) NOT NULL,
                           username   VARCHAR(100)         NOT NULL,
                           email      VARCHAR(100)         NOT NULL,
                           password   VARCHAR(255)         NOT NULL,   -- BCrypt hash (60 ký tự)
                           full_name  NVARCHAR(100)        NULL,       -- NVARCHAR để lưu được tiếng Việt
                           role_id    BIGINT               NOT NULL,
                           created_at DATETIME2(6)         NOT NULL CONSTRAINT DF_users_created_at DEFAULT SYSDATETIME(),
                           CONSTRAINT PK_users PRIMARY KEY (id),
                           CONSTRAINT UQ_users_username UNIQUE (username),
                           CONSTRAINT UQ_users_email UNIQUE (email),
                           CONSTRAINT FK_users_role FOREIGN KEY (role_id) REFERENCES dbo.roles (id)
);
END
GO

-- ---------- Dữ liệu mẫu: 2 role ----------
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE name = 'ADMIN') INSERT INTO dbo.roles (name) VALUES ('ADMIN');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE name = 'USER')  INSERT INTO dbo.roles (name) VALUES ('USER');
GO

-- ---------- Tài khoản admin ----------
-- KHÔNG insert sẵn ở đây vì password phải là hash BCrypt.
-- Khi auth-service khởi động lần đầu, DataInitializer tự tạo:
--     username: admin | email: admin@gmail.com | mật khẩu: giá trị ADMIN_PASSWORD (mặc định 123456)
-- Muốn insert tay thì dán hash BCrypt thật vào <BCRYPT_HASH>:
--
-- INSERT INTO dbo.users (username, email, password, full_name, role_id)
-- SELECT 'admin', 'admin@gmail.com', '<BCRYPT_HASH>', N'Administrator', id
-- FROM dbo.roles WHERE name = 'ADMIN';
GO
-- =====================================================================
-- Auth_db.sql : database riêng của auth-service (SQL Server)
-- Khớp với entity Role / User trong auth-service (Hibernate ddl-auto=update).
-- Chạy file này TRƯỚC khi start service, hoặc chỉ cần tạo database rồi để
-- Hibernate tự tạo bảng (khi đó bỏ qua phần CREATE TABLE).
-- =====================================================================

IF DB_ID(N'Ecommerce_Auth') IS NULL
    CREATE DATABASE Ecommerce_Auth;
GO

USE Ecommerce_Auth;
GO

-- ---------- Bảng roles: ADMIN, USER ----------
IF OBJECT_ID(N'dbo.roles', N'U') IS NULL
BEGIN
CREATE TABLE dbo.roles (
                           id   BIGINT IDENTITY(1,1) NOT NULL,
                           name VARCHAR(50)          NOT NULL,
                           CONSTRAINT PK_roles PRIMARY KEY (id),
                           CONSTRAINT UQ_roles_name UNIQUE (name)
);
END
GO

-- ---------- Bảng users: password chỉ lưu hash BCrypt ----------
IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
CREATE TABLE dbo.users (
                           id         BIGINT IDENTITY(1,1) NOT NULL,
                           username   VARCHAR(100)         NOT NULL,
                           email      VARCHAR(100)         NOT NULL,
                           password   VARCHAR(255)         NOT NULL,   -- BCrypt hash (60 ký tự)
                           full_name  NVARCHAR(100)        NULL,       -- NVARCHAR để lưu được tiếng Việt
                           role_id    BIGINT               NOT NULL,
                           created_at DATETIME2(6)         NOT NULL CONSTRAINT DF_users_created_at DEFAULT SYSDATETIME(),
                           CONSTRAINT PK_users PRIMARY KEY (id),
                           CONSTRAINT UQ_users_username UNIQUE (username),
                           CONSTRAINT UQ_users_email UNIQUE (email),
                           CONSTRAINT FK_users_role FOREIGN KEY (role_id) REFERENCES dbo.roles (id)
);
END
GO

-- ---------- Dữ liệu mẫu: 2 role ----------
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE name = 'ADMIN') INSERT INTO dbo.roles (name) VALUES ('ADMIN');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE name = 'USER')  INSERT INTO dbo.roles (name) VALUES ('USER');
GO

-- ---------- Tài khoản admin ----------
-- KHÔNG insert sẵn ở đây vì password phải là hash BCrypt.
-- Khi auth-service khởi động lần đầu, DataInitializer tự tạo:
--     username: admin | email: admin@gmail.com | mật khẩu: giá trị ADMIN_PASSWORD (mặc định 123456)
-- Muốn insert tay thì dán hash BCrypt thật vào <BCRYPT_HASH>:
--
-- INSERT INTO dbo.users (username, email, password, full_name, role_id)
-- SELECT 'admin', 'admin@gmail.com', '<BCRYPT_HASH>', N'Administrator', id
-- FROM dbo.roles WHERE name = 'ADMIN';
GO

