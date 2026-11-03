# REST_API_Guide — Hướng dẫn kết nối frontend và backend

> Tài liệu lịch sử cho backend cũ. Backend đã được thay; hướng dẫn hiện tại đầy đủ nằm trong [REST_API_Guide_V2.md](REST_API_Guide_V2.md).

## 1. Phạm vi đã kết nối

Thư mục thực tế là `frontend` (React/Vite/Axios) và `backend` (Spring Boot/JPA).
Đã bật REST thật cho sản phẩm và danh mục, nối API lọc/phân trang vào trang danh sách, chuẩn hóa payload và thông báo lỗi. Các lệnh nối có chú thích tiếng Việt ngay cạnh dòng code.

Backend hiện chỉ có `ProductController`, `CategoryController`, `ProductFilterController`. Không cần đổi controller vì đường dẫn và cấu trúc dữ liệu đã đáp ứng các trang này. Đăng nhập, giỏ hàng, đơn hàng và thanh toán chưa có API backend; xem mục 6 để tránh nhầm chức năng demo với dữ liệu thật.

## 2. Cách chạy trên Windows

1. Bật SQL Server, tạo database `Ecommerce` và cấu hình kết nối trong `backend/src/main/resources/application.properties` cho máy của bạn. Không dùng cấu hình H2 test làm cấu hình dữ liệu thật.
2. Mở terminal tại thư mục gốc rồi chạy backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

3. Mở terminal thứ hai tại thư mục gốc rồi chạy frontend:

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

4. Mở địa chỉ Vite in ra (thường `http://localhost:5173`). Backend mặc định ở `http://localhost:8080`.

`frontend/.env.development` đã đặt:

```dotenv
VITE_API_URL=
VITE_USE_MOCK=false
```

Có file mẫu `frontend/.env.example`. Khi cần đổi cổng backend, tạo `frontend/.env.development.local`:

```dotenv
VITE_API_URL=
VITE_USE_MOCK=false
API_PROXY_TARGET=http://localhost:8081
```

Khởi động lại Vite sau khi đổi biến môi trường. `VITE_API_URL` là nguồn gốc URL, không thêm `/api/v1` vì service đã có tiền tố này. Biến `VITE_*` được đưa vào bundle trình duyệt, không đặt mật khẩu trong đó.

### Đường đi của một yêu cầu

```text
Trang React → hook/service → axiosClient → /api/v1/...
           → proxy Vite → Spring controller → service → database
```

`frontend/src/api/axiosClient.js` dùng URL cùng nguồn khi `VITE_API_URL` trống, timeout 15 giây và chuyển lỗi backend thành `error.message`.
`frontend/vite.config.js` chuyển tiếp `/api` đến `API_PROXY_TARGET` (mặc định cổng 8080), áp dụng cả `dev` và `preview`; không cắt tiền tố `/api/v1`.
Trình duyệt gọi cùng nguồn nên cấu hình local không cần thêm CORS ở backend.

Khi triển khai thư mục `dist` lên hosting, cần cấu hình reverse proxy `/api` tại hosting tới backend. Proxy Vite không được đóng gói trong `dist`. Nếu đặt `VITE_API_URL` tới một nguồn khác, phải cấu hình CORS tương ứng ở Spring Security/backend; cấu hình đó chưa được bổ sung trong lần nối này.

## 3. API nào nối vào trang nào

Tất cả đường dẫn API dưới đây có tiền tố `/api/v1`.

| Trang / route | API | Cách nối trong mã nguồn |
| --- | --- | --- |
| Trang chủ `/` — `HomePage.jsx` | `GET /products` | `useProducts` → `productService.getProducts`; lấy dữ liệu thật cho các khu sản phẩm, hiển thị lỗi nếu backend không phản hồi. Banner quảng cáo vẫn là nội dung tĩnh. |
| Danh sách `/products` — `ProductListPage.jsx` | `GET /products/filter` | `useFilteredProducts` → `filterProducts`; gửi tìm kiếm, danh mục, khoảng giá, sắp xếp và số trang lên backend. |
| Danh sách `/products` | `GET /categories` | `useCategories` → `getCategories`; tạo nút danh mục từ ID và tên trong database. |
| Chi tiết `/products/:id` — `ProductDetailPage.jsx` | `GET /products/{id}` | `useProductDetail(id)` → `getProductById(id)`; hiển thị chi tiết hoặc thông báo lỗi/không tìm thấy. |
| Dashboard `/admin` — `AdminDashboardPage.jsx` | `GET /products`, `GET /categories` | Gọi hai service bằng `Promise.all`, đếm sản phẩm/danh mục và lấy danh sách sản phẩm gần đây. Thống kê đơn hàng vẫn là demo. |
| Quản lý `/admin/products` — `AdminProductPage.jsx` | `GET /products`, `DELETE /products/{id}` | Nạp danh sách qua `getProducts`; xác nhận xóa rồi gọi `deleteProduct`. Tìm kiếm/phân trang tại trang quản trị vẫn xử lý trên danh sách đã tải. |
| Thêm `/admin/products/create` — `ProductFormPage.jsx` | `GET /categories`, `POST /products` | Nạp danh mục cho select; gửi dữ liệu biểu mẫu bằng `createProduct`. |
| Sửa `/admin/products/:id/edit` — `ProductFormPage.jsx` | `GET /categories`, `GET /products/{id}`, `PUT /products/{id}` | Tải dữ liệu vào biểu mẫu; gửi thay đổi bằng `updateProduct`. |
| Quản lý `/admin/categories` — `AdminCategoryPage.jsx` | `GET /categories`, `DELETE /categories/{id}` | Nạp danh sách; xóa qua `deleteCategory`, sau đó tải lại. Hiển thị lỗi 409 nếu danh mục còn được sản phẩm tham chiếu. |
| Thêm `/admin/categories/create` — `CategoryFormPage.jsx` | `POST /categories` | `createCategory` gửi tên danh mục. |
| Sửa `/admin/categories/:id/edit` — `CategoryFormPage.jsx` | `GET /categories/{id}`, `PUT /categories/{id}` | `getCategoryById` nạp tên; `updateCategory` lưu tên mới. |

Các file service nằm trong `frontend/src/services`; hook nằm trong `frontend/src/hooks`; trang nằm trong `frontend/src/pages/public` hoặc `frontend/src/pages/admin`.

## 4. Hợp đồng dữ liệu

### Sản phẩm: POST / PUT

```json
{
  "name": "Điện thoại mẫu",
  "description": "Mô tả sản phẩm",
  "price": 12000000,
  "stockQuantity": 10,
  "category": { "id": 1 }
}
```

Thay `category.id` bằng ID tồn tại thực tế. Giá phải lớn hơn 0; tồn kho là số nguyên không âm; tên và danh mục bắt buộc. `toProductPayload` chuyển giá/tồn kho sang số và chỉ gửi các trường backend hỗ trợ. Backend trả sản phẩm với `category: {id, name}`.

Backend chưa lưu ảnh, màu sắc, rating hoặc thông số kỹ thuật. Service không gửi những trường này; ô màu sắc bị vô hiệu hóa khi dùng API thật. Ảnh/thông số ở các trang vẫn do tiện ích frontend tạo/chọn, không phải dữ liệu trả từ REST.

### Danh mục: POST / PUT

```json
{ "name": "Điện thoại" }
```

Tên bắt buộc, tối đa 100 ký tự và không trùng. API danh sách trả mảng JSON; API chi tiết trả một object; API tạo trả HTTP 201; API xóa thành công trả HTTP 204 không có body.

### Lọc và phân trang

Ví dụ:

```text
GET /api/v1/products/filter?categoryId=1&keyword=phone&minPrice=10000000&maxPrice=25000000&page=0&size=8&sortBy=price&direction=asc
```

- Trang giao diện bắt đầu từ 1; request gửi `page = currentPage - 1`, `size = 8`.
- Backend tìm theo tên sản phẩm, không tìm trong mô tả hay tên danh mục.
- Giao diện hỗ trợ sắp xếp mặc định theo `id`, hoặc theo `price` tăng/giảm. Đã bỏ tùy chọn rating vì backend chưa có trường này.
- Giá dưới 10 triệu gửi `maxPrice=9999999.99`; trên 25 triệu gửi `minPrice=25000000.01`, theo độ chính xác hai chữ số thập phân của backend.
- Backend còn hỗ trợ `inStock`, `sortBy=name|stockQuantity` nhưng giao diện hiện chưa có bộ điều khiển cho các tùy chọn này.
- Response có `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`. Không phân trang lần nữa trên `content`.
- Hook chờ 250 ms khi bộ lọc đổi và hủy request cũ bằng `AbortController`, tránh kết quả cũ ghi đè kết quả mới.
- Các liên kết cũ `category=phone|laptop|tablet|watch|audio|headphone|accessories` được đối chiếu tên danh mục để lấy ID thật. Liên kết do nút danh mục mới tạo dùng ID thật. Không tìm thấy alias thì hiển thị danh sách trống.

### Xử lý lỗi

Backend trả `{ "error": "..." }` hoặc lỗi validation dạng `{ "price": "...", "name": "..." }`. Interceptor giữ nguyên lỗi Axios/status, đồng thời chuẩn hóa `error.message` để các trang hiển thị được. Không tự chuyển sang mock khi API lỗi.

## 5. Kiểm tra và kết quả

Đã thực hiện:

- `npm.cmd run build`: thành công.
- `backend/.\mvnw.cmd test`: 37 kiểm thử đạt, 0 lỗi, dùng H2 in-memory.
- Kiểm tra HTTP thật: service frontend → proxy Vite cổng 15173 → Spring Boot cổng 18080 → H2 riêng. Đã kiểm tra tạo/đọc/sửa/xóa sản phẩm và danh mục, lọc giá/từ khóa/danh mục, phân trang, lỗi validation 400, không tìm thấy 404 và xóa danh mục đang dùng 409. Đạt.
- Lint toàn frontend còn lỗi ở code hiện có như import thừa, cập nhật state trong effect, export context và script `translate.js`. Không coi build thành công là lint thành công.
- Chưa kiểm thử trực quan trên trình duyệt và chưa xác nhận kết nối với SQL Server thật của máy bạn. Kiểm thử HTTP dùng dữ liệu H2 riêng, không thay đổi dữ liệu SQL Server.

Kiểm tra thủ công sau khi chạy với SQL Server: tạo danh mục ở admin, tạo sản phẩm thuộc danh mục đó, mở trang chi tiết, lọc theo danh mục/giá, sửa giá rồi tải lại trang công khai, xóa sản phẩm rồi xóa danh mục. Dữ liệu phải còn sau khi tải lại và đồng nhất giữa các trang. Trong DevTools → Network kiểm tra request `/api/v1/...` và JSON response.

## 6. Các chức năng chưa có REST backend

| Chức năng | Trạng thái thực tế |
| --- | --- |
| `/login`, `/register`, `/admin/login` | Frontend demo, chưa có API xác thực. Đăng nhập admin cũ dùng `admin@gmail.com` / `123456` trong `authService.js`, không phải tài khoản SQL Server. |
| `/cart` và giỏ hàng bên cạnh | State/localStorage phía trình duyệt; chưa có API giỏ hàng. |
| `/checkout`, `/payment-success` | Backend chưa có API tạo đơn/thanh toán. Chế độ REST thật chặn gửi checkout và hiển thị thông báo, tránh dùng PUT sản phẩm để trừ kho khi chưa có giao dịch tạo đơn. |
| `/orders`, `/tracking`, `/admin/orders` | Đơn hàng demo trong localStorage, không được đồng bộ với backend. |
| Dashboard: đơn hàng/doanh thu | Vẫn đọc đơn hàng demo, chỉ thống kê sản phẩm và danh mục là REST thật. |

`SecurityConfig` hiện cho phép mọi request; màn hình đăng nhập admin chỉ là kiểm tra phía frontend, không bảo vệ API ghi. Muốn vận hành bán hàng thật cần bổ sung xác thực/phân quyền backend và API tạo đơn có giao dịch kiểm tra/trừ kho phía server.

Nếu muốn xem lại demo, đặt `VITE_USE_MOCK=true` và khởi động lại Vite. Sản phẩm/danh mục khi đó đọc/ghi localStorage thay vì SQL Server; đây là chế độ riêng, không phải cơ chế dự phòng tự động. Không cần xóa cache mock để dùng API thật.
