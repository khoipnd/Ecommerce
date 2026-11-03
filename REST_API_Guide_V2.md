# REST_API_Guide_V2 — Kết nối frontend với backend mới

Tài liệu đầy đủ cho mã nguồn sau khi thay backend ngày 01/10/2026. Dùng tài liệu này thay hướng dẫn lần đầu `REST_API_Guide.md`. Thư mục thực tế: `frontend` và `backend`.

## 1. Kết quả đối chiếu

| Phần | Kết quả và xử lý |
| --- | --- |
| Sản phẩm, danh mục, lọc/phân trang | Đường dẫn `/api/v1/products`, `/api/v1/categories`, `/api/v1/products/filter` và DTO không thay đổi. Giữ service/hook/proxy đã nối lần trước. |
| Quyền ghi sản phẩm/danh mục | Bổ sung JWT vào Axios. Backend hiện yêu cầu ADMIN cho POST/PUT/DELETE; GET vẫn công khai. |
| Đăng nhập | Backend mới có `/auth/login`, `/auth/me`. Thay đăng nhập giả ở cả trang khách hàng và admin bằng API thật. |
| Đăng ký | Backend được cung cấp chưa có API đăng ký. Đã bổ sung `POST /api/v1/auth/register` và nối trang `/register`. |
| Đơn hàng khách hàng | Nối tạo đơn, danh sách của tôi, chi tiết và hủy đơn. Không lưu đơn mới vào localStorage. |
| Quản lý đơn hàng | Nối danh sách/lọc trạng thái, chi tiết và cập nhật trạng thái bằng API admin. |
| Dashboard | Đếm sản phẩm/danh mục như trước; thay thống kê đơn hàng demo bằng API admin. Tổng giá trị đơn hoàn thành lấy từ các đơn `COMPLETED`. |
| Xác thực backend | Backend có phát JWT nhưng chưa đưa JWT vào Spring Security. Đã bổ sung filter để `OrderController` nhận đúng `Authentication`, chặn quyền admin ở server. |
| Thanh toán trực tuyến | Backend vẫn chưa có cổng thanh toán/xác nhận giao dịch. Đặt hàng thật hoạt động theo luồng thanh toán khi nhận hàng; bỏ QR/tài khoản chuyển khoản mẫu. |
| Giỏ hàng | Backend không có API giỏ hàng; giỏ vẫn lưu trên trình duyệt. Checkout gửi danh sách sản phẩm/số lượng sang API đơn hàng. |

Các lệnh gọi API mới và điểm nối chính có comment tiếng Việt ngay cạnh code.

## 2. Chuẩn bị database và chạy dự án

### SQL Server

Backend đang cấu hình `spring.jpa.hibernate.ddl-auto=none`, vì vậy **phải có schema trước khi chạy**. Các bảng cần có: `role`, `account`, `category`, `product`, `orders`, `order_item`.

Đã thêm `Database/REST_API_V2_Migration.sql`: tạo database `Ecommerce` nếu thiếu, tạo từng bảng nếu thiếu, không DROP và không xóa bản ghi cũ. Chạy script này trong SQL Server Management Studio khi database của bạn còn thiếu bảng. Script không tự sửa cấu trúc bảng đã tồn tại; nếu schema cũ khác, cần đối chiếu định nghĩa bảng. Script đã được tạo, chưa thực thi trên SQL Server của bạn.

**Không dùng nguyên `Database/DB.sql` để nâng cấp database đang có dữ liệu**, vì phần đầu script đó chứa `DROP DATABASE Ecommerce`.

Kiểm tra URL, tài khoản và mật khẩu SQL Server trong `backend/src/main/resources/application.properties`. URL hiện trỏ tới `localhost:1433`, database `Ecommerce`. Không đổi `ddl-auto` thành `create`/`create-drop` trên dữ liệu cần giữ.

### Backend — terminal thứ nhất

Từ thư mục gốc:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend mặc định cổng 8080. Cần Java 17 trở lên và môi trường chạy Maven Wrapper. Khi khởi động, `DataInitializer` tạo các role và tài khoản admin nếu chưa tồn tại:

- Username: `admin`
- Email: `admin@gmail.com`
- Mật khẩu khởi tạo: `123456`

Initializer không thay mật khẩu tài khoản đã tồn tại. Nếu database cũ có `admin`, hãy dùng mật khẩu của tài khoản đó. Mật khẩu trong bảng phải là BCrypt, không phải chuỗi mật khẩu thô. Tài khoản khách mới có thể tạo ngay tại `/register`.

Backend hỗ trợ cấu hình `APP_JWT_SECRET` (ít nhất 32 ký tự), `APP_JWT_EXPIRATION_MINUTES` (mặc định 120), `APP_CORS_ALLOWED_ORIGINS` (các origin ngăn cách bằng dấu phẩy). Đổi secret/mật khẩu admin mặc định khi triển khai thật. Không đặt secret JWT trong biến frontend `VITE_*`.

### Frontend — terminal thứ hai

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

Mở `http://localhost:5173`. Cấu hình `frontend/.env.development`:

```dotenv
VITE_API_URL=
VITE_USE_MOCK=false
```

Để đổi backend sang cổng khác, tạo `frontend/.env.development.local`:

```dotenv
VITE_API_URL=
VITE_USE_MOCK=false
API_PROXY_TARGET=http://localhost:8081
```

Khởi động lại Vite khi đổi biến môi trường. Vite dùng cổng cố định 5173 cho dev, 4173 cho preview; nếu cổng bận, báo lỗi để tránh tự nhảy sang origin chưa được backend cho phép.

### Proxy / CORS / production

```text
Trang → hook/service → axiosClient (+ Authorization: Bearer JWT)
      → /api/v1/... trên frontend → proxy Vite → Spring Boot → SQL Server
```

- `VITE_API_URL` trống: gọi `/api` cùng nguồn. Proxy giữ nguyên tiền tố `/api/v1`, chuyển đến cổng 8080 hoặc `API_PROXY_TARGET`.
- `VITE_API_URL` nếu dùng URL trực tiếp chỉ chứa origin, ví dụ `http://localhost:8080`; không thêm `/api/v1` lần nữa.
- Backend cho phép origin localhost/127.0.0.1 ở cổng 5173 và 4173, gồm phương thức `PATCH` cho đơn hàng. Origin khác cấu hình bằng `APP_CORS_ALLOWED_ORIGINS`.
- `npm.cmd run build` tạo `dist`; `npm.cmd run preview` vẫn có proxy Vite.
- Khi host `dist` ở production, cấu hình reverse proxy `/api` của hosting tới backend, hoặc build với `VITE_API_URL` đúng và cấu hình CORS. Proxy dev không nằm trong bundle `dist`.

## 3. Bảng API → trang → cách nối

Đường dẫn API trong bảng đều có tiền tố **`/api/v1`**.

### Xác thực và phiên

| Trang / thành phần | API | Cách dùng |
| --- | --- | --- |
| `/login` — `pages/login/Login.jsx` | `POST /auth/login` | Gửi `{identifier, password}` qua `authService.login`; lưu phiên từ response, quay lại trang yêu cầu đăng nhập nếu có. UI hiện nhập email; API cũng chấp nhận username. |
| `/admin/login` — `pages/admin/AdminLoginPage.jsx` | `POST /auth/login` | `authService.loginAdmin` chỉ lưu phiên khi response có `role=ADMIN`. Không dùng tài khoản hardcode phía FE nữa. |
| `/register` — `pages/login/Register.jsx` | `POST /auth/register` | Gửi `{email,password}`; chỉ báo thành công khi API trả 201 rồi chuyển đến đăng nhập. Tài khoản luôn là USER. |
| `routes/ProtectedRoute.jsx` và `ProtectedAdminRoute.jsx` | `GET /auth/me` | Xác minh phiên từ backend trước khi mở checkout, đơn hàng, trang xác nhận và toàn bộ admin. |
| `components/layout/Header.jsx`, `AdminLayout.jsx` | Không có API logout | Logout xóa phiên JWT ở trình duyệt; hook `useAuth` cập nhật giao diện. Backend chưa có endpoint thu hồi JWT. |
| Thẻ sản phẩm / trang chi tiết | Không gọi API đăng nhập riêng | Kiểm tra phiên JWT thay `userEmail` demo; mua ngay chuyển sang checkout có route bảo vệ, giữ sản phẩm qua đăng nhập. |

### Sản phẩm và danh mục — giữ các kết nối đúng từ lần trước

| Trang / route | API | Cách dùng |
| --- | --- | --- |
| `/` — `HomePage.jsx` | `GET /products` | `useProducts` → `getProducts`; dữ liệu sản phẩm thật. Banner/khuyến mãi vẫn là nội dung giao diện. |
| `/products` — `ProductListPage.jsx` | `GET /products/filter`, `GET /categories` | `useFilteredProducts` gửi bộ lọc và trang; `useCategories` tạo nút danh mục từ ID thật. |
| `/products/:id` — `ProductDetailPage.jsx` | `GET /products/{id}` | `useProductDetail` nạp dữ liệu và hiển thị lỗi khi không tồn tại. |
| `/admin/products` — `AdminProductPage.jsx` | `GET /products`, `DELETE /products/{id}` | Nạp danh sách, xóa sau xác nhận; Axios tự gắn JWT ADMIN. |
| `/admin/products/create` — `ProductFormPage.jsx` | `GET /categories`, `POST /products` | Nạp danh mục; gửi payload đã chuẩn hóa. |
| `/admin/products/:id/edit` — `ProductFormPage.jsx` | `GET /products/{id}`, `GET /categories`, `PUT /products/{id}` | Nạp biểu mẫu và lưu chỉnh sửa. |
| `/admin/categories` — `AdminCategoryPage.jsx` | `GET /categories`, `DELETE /categories/{id}` | Nạp, xóa, tải lại; lỗi khóa ngoại 409 được hiển thị. |
| `/admin/categories/create` — `CategoryFormPage.jsx` | `POST /categories` | Gửi tên danh mục. |
| `/admin/categories/:id/edit` — `CategoryFormPage.jsx` | `GET /categories/{id}`, `PUT /categories/{id}` | Nạp tên và lưu tên mới. |

### Đơn hàng và dashboard — kết nối mới

| Trang / route | API | Cách dùng |
| --- | --- | --- |
| `/checkout` — `CheckoutPage.jsx` | `POST /orders` | `createOrder` gửi người nhận, địa chỉ, ghi chú và `{productId,quantity}`. Backend tính tiền/trừ kho. Chỉ xóa giỏ sau thành công; mua ngay giữ giỏ cũ. |
| `/payment-success?orderId=...` — `PaymentSuccess.jsx` | `GET /orders/{id}` | Đọc lại đơn thật để xác nhận ID, tổng tiền và trạng thái; tải lại URL vẫn hoạt động. Không coi đây là xác nhận chuyển khoản. |
| `/orders`, `/tracking` — `OrdersPage.jsx` | `GET /orders/my` | Chỉ lấy đơn thuộc người đang đăng nhập. `/tracking` hiện cùng màn hình lịch sử đơn, không có API vận chuyển riêng. |
| Nút View Details của khách | `GET /orders/{id}` | Lấy chi tiết/trạng thái mới nhất trước khi mở rộng đơn. |
| Nút Cancel Order của khách | `PATCH /orders/{id}/cancel` | Chỉ hiện cho PENDING; không tự sửa kho trong frontend. |
| `/admin/orders` — `AdminOrderPage.jsx` | `GET /admin/orders?status=...` | Tải tất cả hoặc lọc theo enum. Tìm kiếm ID/tên/số điện thoại và phân trang trên danh sách đã trả về. |
| Nút View của admin | `GET /admin/orders/{id}` | Lấy chi tiết mới nhất cho modal. |
| Chọn trạng thái / hủy đơn của admin | `PATCH /admin/orders/{id}/status` | Gửi `{status}`; chỉ cho chọn các bước chuyển hợp lệ. Dùng response cập nhật giao diện, lỗi giữ dữ liệu trước đó. |
| `/admin` — `AdminDashboardPage.jsx` | `GET /products`, `GET /categories`, `GET /admin/orders` | Gọi đồng thời; đếm đơn chưa hủy, đơn hủy và tổng giá trị đơn COMPLETED. Đây là giá trị đơn, không phải đối soát giao dịch ngân hàng. |

## 4. Payload và response cần biết

### Đăng ký, đăng nhập, phiên

```json
// POST /api/v1/auth/register
{ "email": "customer@example.com", "password": "test123456" }

// POST /api/v1/auth/login
{ "identifier": "customer@example.com", "password": "test123456" }
```

Đăng ký: email hợp lệ, tối đa 100 ký tự; mật khẩu 6–72 ký tự, được BCrypt mã hóa. Username tài khoản đăng ký mới bằng email đã chuẩn hóa chữ thường. Email trùng trả 409. API không nhận quyền ADMIN từ client.

Login trả `token`, `tokenType=Bearer`, `expiresInSeconds`, `id`, `username`, `fullName`, `email`, `role`. `authStorage.js` lưu phiên trong **sessionStorage**, khóa `rest_auth_session`, kèm `expiresAt`; không lưu mật khẩu. Các khóa giả cũ `admin_auth`/`userEmail` không được dùng làm bằng chứng đăng nhập.

`axiosClient.js` tự gắn `Authorization: Bearer <token>` cho request; login/register không gắn token cũ. Response 401 của phiên hiện tại xóa phiên và route bảo vệ chuyển về đăng nhập. 403 giữ phiên nhưng báo thiếu quyền. Lỗi mạng không tự đăng xuất. Token không có cơ chế refresh; cần đăng nhập lại sau khi hết hạn. Logout phía FE chưa thu hồi token tại server.

### Sản phẩm / danh mục

```json
// POST /api/v1/products hoặc PUT /api/v1/products/{id}
{
  "name": "Điện thoại mẫu",
  "description": "Mô tả",
  "price": 12000000,
  "stockQuantity": 10,
  "category": { "id": 1 }
}

// POST /api/v1/categories hoặc PUT /api/v1/categories/{id}
{ "name": "Điện thoại" }
```

ID danh mục phải tồn tại. Giá > 0, stockQuantity là số nguyên không âm. Service chỉ gửi trường backend hỗ trợ; ảnh/màu/rating/thông số không được lưu trong entity Product. Ô nhập màu bị vô hiệu hóa khi dùng API thật. Tên danh mục không rỗng, tối đa 100 ký tự, không trùng. Tạo trả 201; xóa trả 204. Sản phẩm đã thuộc đơn hàng hoặc danh mục còn sản phẩm có thể không xóa được vì khóa ngoại.

### Lọc/phân trang

```text
GET /api/v1/products/filter?categoryId=1&keyword=phone&minPrice=10000000&maxPrice=25000000&page=0&size=8&sortBy=price&direction=asc
```

- Backend tìm keyword trong tên sản phẩm. Response: `{content,page,size,totalElements,totalPages,first,last}`.
- Trang FE bắt đầu từ 1, gửi `page=currentPage-1`; 8 sản phẩm/trang; không cắt trang lần nữa trên `content`.
- Sắp xếp UI theo `id` hoặc `price`; không còn tùy chọn rating vì backend không hỗ trợ.
- Hook đợi 250 ms và hủy request cũ khi bộ lọc thay đổi.
- Nút danh mục dùng ID thật. Liên kết cũ dạng `category=phone|laptop|tablet|watch|audio|headphone|accessories` ánh xạ theo tên danh mục; không có danh mục khớp thì hiển thị trống.
- Backend còn hỗ trợ `inStock`, sort theo `name` hoặc `stockQuantity`, nhưng UI chưa có nút tương ứng.

### Tạo đơn

```json
{
  "receiverName": "Nguyễn Văn A",
  "receiverPhone": "0901234567",
  "shippingAddress": "Địa chỉ nhận hàng",
  "note": "Gọi trước khi giao",
  "items": [ { "productId": 1, "quantity": 2 } ]
}
```

Không gửi username, giá hoặc tổng tiền làm dữ liệu quyết định: backend lấy username từ JWT, đọc giá trong database, gộp các dòng trùng productId, kiểm tra tồn kho và trừ kho trong transaction. Lỗi bất kỳ sản phẩm nào rollback toàn bộ đơn. Tổng trên checkout là ước tính từ giỏ; trang xác nhận dùng tổng trả từ server.

Response đơn có `id`, `username`, `receiverName`, `receiverPhone`, `shippingAddress`, `note`, `status`, `totalAmount`, `createdAt`, `items`. Mỗi item có `productId`, `productName`, `quantity`, `unitPrice`, `lineTotal`.

`orderService.toOrderView` ánh xạ sang UI cũ: `totalAmount→total`, `createdAt→date`, thông tin nhận hàng→`customer`, item→`product`. Giữ nguyên trạng thái enum viết hoa. Không giả định ID đơn là chuỗi `ORDER...`; admin tìm kiếm chuyển ID số sang chuỗi.

### Chuyển trạng thái

| Hiện tại | Admin được chuyển đến | Khách được hủy? |
| --- | --- | --- |
| PENDING | CONFIRMED, CANCELLED | Có |
| CONFIRMED | SHIPPED, CANCELLED | Không |
| SHIPPED | COMPLETED | Không |
| COMPLETED | Không | Không |
| CANCELLED | Không | Không |

Hủy đơn thành công do backend hoàn kho. Khách đọc/hủy đơn không thuộc mình nhận 404; API admin yêu cầu ADMIN. UI chỉ đưa ra bước chuyển được hỗ trợ; backend vẫn kiểm tra lại.

## 5. Các file sửa/bổ sung chính

- FE chung: `src/api/axiosClient.js`, `services/authStorage.js`, `services/authService.js`, `services/orderService.js`, `hooks/useAuth.js`.
- FE route: `routes/ProtectedRoute.jsx`, `ProtectedAdminRoute.jsx`, `AppRoutes.jsx`.
- FE trang: Login, Register, CheckoutPage, PaymentSuccess, OrdersPage, AdminOrderPage, AdminDashboardPage; Header/ProductCard/ProductDetailPage dùng phiên mới. Các trang catalog tiếp tục dùng API cũ đúng hợp đồng.
- BE xác thực: `security/JwtAuthenticationFilter.java`, `config/SecurityConfig.java`, `controller/AuthController.java`, `service/AuthService.java`, `dto/RegisterRequest.java`, `repository/AccountRepository.java`.
- BE đơn hàng: `entity/Order.java` khởi tạo ngày đặt ở `@PrePersist` để API trả được createdAt ngay, kể cả database không có DEFAULT.
- Test: `api/AuthOrderApiTest.java`, cập nhật `AbstractApiTest` dọn đơn trước khi dọn sản phẩm.
- Database: `Database/REST_API_V2_Migration.sql`, chưa chạy vào dữ liệu thật.

## 6. Kiểm thử

Đã kiểm tra:

1. Build frontend thành công.
2. Lint các file tích hợp xác thực/đơn hàng mới đạt. Lint toàn repository còn 16 lỗi và 1 cảnh báo ở code có sẵn, như import thừa, state trong effect và `translate.js`; không coi build là đã đạt toàn bộ lint.
3. Backend: **41 test đạt, không lỗi**, gồm 37 test catalog cũ và 4 test mới chứa nhiều tình huống JWT/đơn hàng: đăng ký/login/me, không cho tự cấp ADMIN, 401/403, chủ sở hữu đơn, hoàn kho, chuyển trạng thái, PATCH CORS, rollback khi thiếu hàng.
4. Kiểm thử service FE → proxy Vite → Spring Boot với H2 riêng: đăng ký/đăng nhập user/admin, tự gắn JWT, catalog/filter, tạo/list/chi tiết/hủy đơn, stock, lọc và chuyển trạng thái admin, logout. Đạt.

Các test dùng H2 trong bộ nhớ, không sửa SQL Server của bạn. Chưa kiểm tra trực quan bằng trình duyệt và chưa xác minh schema/kết nối SQL Server thật. Cần chạy backend với database của bạn theo mục 2.

Lệnh chạy lại:

```powershell
# Từ thư mục backend
.\mvnw.cmd test

# Từ thư mục frontend
npm.cmd run build
npm.cmd run lint
```

### Kiểm tra thủ công đề nghị

1. Đăng nhập admin, thêm danh mục và sản phẩm có tồn kho.
2. Đăng ký tài khoản khách; thử mật khẩu sai rồi đăng nhập đúng.
3. Mở sản phẩm, lọc, thêm giỏ hoặc mua ngay; nhập thông tin và đặt hàng.
4. Kiểm tra mã đơn/tổng server tại trang xác nhận, tải lại trang, xem đơn ở `/orders`.
5. Hủy đơn PENDING; kiểm tra tồn kho được hoàn. Không có nút hủy với đơn đã xác nhận/giao.
6. Tạo đơn khác, đăng nhập admin, mở chi tiết và chuyển CONFIRMED → SHIPPED → COMPLETED. Dashboard chỉ cộng giá trị đơn hoàn thành.
7. Đăng nhập bằng tài khoản khách khác: không thấy đơn của người trước. API admin bị chặn cho USER.
8. DevTools → Network: kiểm tra endpoint, Authorization và status HTTP; không chia sẻ token trong ảnh/log.

## 7. Giới hạn còn lại và cách xử lý lỗi

- Chưa có API đồng bộ giỏ hàng đa thiết bị, quên/đổi mật khẩu, refresh/revoke token, cổng thanh toán, webhook ngân hàng hay tracking vận chuyển. Không có thông báo thanh toán online thành công giả.
- Giỏ hàng vẫn lưu trong localStorage của trình duyệt. Màu lựa chọn/ảnh/thông số và giá gạch ngang là phần UI, không phải thuộc tính đơn hàng backend hỗ trợ.
- `VITE_USE_MOCK=true` chỉ phục vụ demo catalog. Xác thực/đơn hàng dùng API thật; checkout chặn đặt đơn từ catalog mock. Dùng `false` để kiểm tra tích hợp đầy đủ.
- Mất kết nối/timeout: kiểm tra backend, SQL Server, `API_PROXY_TARGET`. Không tự chuyển sang dữ liệu giả khi lỗi.
- 401: đăng nhập lại; phiên demo cũ không dùng được. 403: kiểm tra tài khoản ADMIN và Bearer JWT.
- 400 khi đặt đơn: xem lỗi địa chỉ/số lượng/hết kho; không sửa stock thủ công ở FE. 400 khi đổi trạng thái: đọc lại đơn vì trạng thái có thể đã được cập nhật.
- 404 đơn hàng: sai ID hoặc không thuộc tài khoản hiện tại. 409: email/danh mục trùng hoặc vi phạm khóa ngoại.
- Lỗi bảng không tồn tại: cấu hình hiện là `ddl-auto=none`; đối chiếu schema và dùng script bổ sung bảng, không chạy script DROP database cũ.
- CORS: origin phải nằm trong `APP_CORS_ALLOWED_ORIGINS`; dùng proxy mặc định và đúng cổng 5173/4173.
