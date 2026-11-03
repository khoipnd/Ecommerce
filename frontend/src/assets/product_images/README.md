# Ảnh sản phẩm

Chép ảnh vào thư mục này và đặt tên theo ID sản phẩm. Xem ID tại trang admin **Products** hoặc trong URL `/products/<id>`.

- Ảnh chính: `<id>.<ext>` — ví dụ `5.jpg`.
- Ảnh phụ của gallery: `<id>-<n>.<ext>` với `n` từ 2 trở lên — ví dụ `5-2.jpg`, `5-3.webp`.
- Hỗ trợ `jpg`, `jpeg`, `png`, `webp`, `avif` (không phân biệt chữ hoa/chữ thường).

Nên dùng ảnh vuông từ **600 × 600 px** trở lên và dung lượng dưới **300 KB**. Sau khi thêm ảnh, tải lại trang; Vite sẽ tự nhận file mới.

## Thứ tự ưu tiên

1. URL ảnh do API cung cấp (`imageUrl` hoặc ảnh đầu tiên trong `images`).
2. Ảnh riêng theo ID trong thư mục này, ví dụ `5.jpg`.
3. Ảnh stock local theo tên category và tên sản phẩm trong `src/assets/stock/`.
4. `PRODUCT_PLACEHOLDER` nếu vẫn không có ảnh.

Muốn thay ảnh stock: sửa nguồn trong `src/data/stockImages.js`, chạy `npm run fetch:stock -- --force`, rồi xem `/dev/stock-images` khi chạy `npm run dev`. Nên kiểm tra cả nhóm ảnh và bảng sản phẩm ở cuối trang. Trong production, nhóm thiếu ảnh local sẽ dùng `PRODUCT_PLACEHOLDER`; ứng dụng không tải Unsplash làm fallback.

Hãy commit ảnh trong `src/assets/stock/` cùng mã nguồn. Docker image và CI chỉ có ảnh đã được đưa vào repo; lệnh build không tải ảnh từ mạng.
