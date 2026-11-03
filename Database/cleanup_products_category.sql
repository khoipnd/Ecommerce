/* =========================================================
   Ecommerce_Products: sửa category_id sai + xoá bảng category không dùng
   Chạy TỪNG PHẦN theo thứ tự, đọc kết quả trước khi chạy phần sau.
   ========================================================= */
USE Ecommerce_Products;
GO

/* ---------- PHẦN 0: KIỂM TRA (chỉ đọc) ---------- */
-- 0.1 Các khoá ngoại đang trỏ vào bảng category (nếu có)
SELECT fk.name AS fk_name,
       OBJECT_NAME(fk.parent_object_id) AS from_table
FROM sys.foreign_keys fk
WHERE fk.referenced_object_id = OBJECT_ID(N'dbo.category');

-- 0.2 Nội dung bảng category cũ
SELECT * FROM dbo.category;

-- 0.3 Sản phẩm theo category_id hiện tại (đối chiếu với tên)
SELECT category_id, COUNT(*) AS so_sp, STRING_AGG(name, ', ') AS san_pham
FROM dbo.product
GROUP BY category_id
ORDER BY category_id;
GO

/* ---------- PHẦN 1: SỬA category_id + XOÁ BẢNG ----------
   ID category mới lấy từ category-service (Ecommerce_Category):
   Electronics=1, Phone=6, Laptop=7, Smartwatch=8, Headphones=9, Accessories=10
   Mapping dưới đây suy ra từ tên sản phẩm. Hãy so với kết quả 0.3 trước khi chạy. */
SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- Sao lưu
SELECT * INTO dbo.product_backup_20261008  FROM dbo.product;
SELECT * INTO dbo.category_backup_20261008 FROM dbo.category;

-- Bỏ mọi khoá ngoại product -> category (nếu có)
DECLARE @sql NVARCHAR(MAX) = N'';
SELECT @sql += N'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + N'.'
             + QUOTENAME(OBJECT_NAME(parent_object_id))
             + N' DROP CONSTRAINT ' + QUOTENAME(name) + N';'
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID(N'dbo.category');
EXEC sp_executesql @sql;

-- Sửa category_id theo ID mới của category-service
UPDATE dbo.product
SET category_id = CASE category_id
    WHEN 10004 THEN 6   -- Phone
    WHEN 10005 THEN 7   -- Laptop
    WHEN 10006 THEN 8   -- Smartwatch
    WHEN 10007 THEN 9   -- Headphones
    WHEN 10008 THEN 10  -- Accessories
    ELSE category_id END
WHERE category_id IN (10004, 10005, 10006, 10007, 10008);

-- Xoá bảng không dùng
DROP TABLE dbo.category;

-- Kiểm tra lại: mọi category_id phải thuộc {1,6,7,8,9,10}
SELECT category_id, COUNT(*) AS so_sp FROM dbo.product GROUP BY category_id;

COMMIT TRANSACTION;   -- Kết quả sai? Đổi dòng này thành ROLLBACK TRANSACTION;
GO

/* ---------- PHẦN 2: CHỐNG NHẢY ID +1000 SAU KHI RESTART ---------- */
ALTER DATABASE SCOPED CONFIGURATION SET IDENTITY_CACHE = OFF;
GO
USE Ecommerce_Category;
ALTER DATABASE SCOPED CONFIGURATION SET IDENTITY_CACHE = OFF;
GO
USE Ecommerce_Order;
ALTER DATABASE SCOPED CONFIGURATION SET IDENTITY_CACHE = OFF;
GO

/* ---------- PHẦN 3 (sau khi đã kiểm tra app chạy ổn): xoá bảng backup ----------
USE Ecommerce_Products;
DROP TABLE dbo.product_backup_20261008;
DROP TABLE dbo.category_backup_20261008;
*/
