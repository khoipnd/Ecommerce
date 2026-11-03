# auth-service

Microservice **xác thực và người dùng** của dự án Ecommerce (phần việc của thành viên BE 1).
Project Spring Boot **riêng**, database **riêng**, không phụ thuộc thư mục `backend/`.

- Đăng ký, đăng nhập, cấp **JWT** (HS256)
- Bảng `users`, `roles`; mật khẩu băm **BCrypt**
- Phân quyền `ADMIN` / `USER` bằng Spring Security
- Cổng mặc định **8081**, Swagger UI: http://localhost:8081/swagger-ui.html

## Công nghệ

Java 17 (chạy được trên JDK 26), Spring Boot 4.1.1, Spring Security, Spring Data JPA, MySQL (mặc định) hoặc SQL Server, springdoc-openapi, JUnit 5 + MockMvc + H2 (test).

## Cấu trúc

```
auth-service/
├── pom.xml, mvnw, mvnw.cmd
├── Dockerfile, docker-compose.yml, .dockerignore
├── database/                  # script tạo bảng (MySQL, SQL Server) - không bắt buộc
└── src/
    ├── main/java/com/fpt/authservice/
    │   ├── AuthServiceApplication.java
    │   ├── config/        SecurityConfig, PasswordConfig, DataInitializer, OpenApiConfig
    │   ├── controller/    AuthController (/auth), UserController (/users)
    │   ├── dto/           RegisterRequest, LoginRequest, LoginResponse, UserResponse
    │   ├── entity/        User, Role
    │   ├── exception/     GlobalExceptionHandler
    │   ├── repository/    UserRepository, RoleRepository
    │   ├── security/      JwtService, JwtAuthenticationFilter, AuthenticatedUser
    │   └── service/       AuthService, UserService
    ├── main/resources/    application.properties, application-sqlserver.properties
    └── test/              JwtServiceTest, PasswordHashingTest, AuthApiTest (+ H2 config)
```

## Chạy trong IntelliJ (MySQL)

1. Cài MySQL 8 và cấu hình user/password bằng `DB_USERNAME`, `DB_PASSWORD`.
2. Mở thư mục `auth-service/` như một Maven project: chuột phải `pom.xml` → **Add as Maven Project**.
3. `File > Project Structure`: SDK = JDK 26, Language level = 17.
4. Chạy `AuthServiceApplication`. Database `auth_db` và hai bảng `users`, `roles` được **tự tạo** (`createDatabaseIfNotExist=true`, `DDL_AUTO=update`). Muốn tự tạo bằng script: chạy `database/auth_db_mysql.sql` rồi đặt `DDL_AUTO=none`.
5. Lần chạy đầu tự tạo role `ADMIN`, `USER` và tài khoản **admin / 123456** (đổi bằng biến `ADMIN_PASSWORD`).

SQL Server: đặt Active profiles = `sqlserver` (xem `application-sqlserver.properties`, tạo trước database bằng `database/auth_db_sqlserver.sql`).

## Biến môi trường

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `PORT` | 8081 | Cổng chạy |
| `DB_USERNAME`, `DB_PASSWORD` | root / 12345 | Tài khoản database |
| `SPRING_DATASOURCE_URL` | jdbc:mysql://localhost:3306/auth_db... | Ghi đè URL (dùng trong Docker) |
| `DDL_AUTO` | update | `update` tự tạo bảng, `none` dùng script |
| `JWT_SECRET` | chuỗi dev | **Bắt buộc đổi khi triển khai thật**, tối thiểu 32 ký tự |
| `JWT_EXPIRATION_MINUTES` | 120 | Thời hạn token |
| `ADMIN_PASSWORD` | 123456 | Mật khẩu admin mẫu |

## API

| Method | URL | Quyền | Mô tả |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Công khai | Tạo tài khoản USER |
| POST | `/api/v1/auth/login` | Công khai | Đăng nhập, nhận JWT |
| GET | `/api/v1/auth/me` | Đã đăng nhập | Thông tin tài khoản hiện tại |
| GET | `/api/v1/users` | ADMIN | Danh sách người dùng |

Mã lỗi: `400` dữ liệu sai · `401` chưa đăng nhập/sai mật khẩu/token sai hoặc hết hạn · `403` đủ đăng nhập nhưng thiếu quyền · `409` email đã đăng ký.

```bash
# Đăng ký (role luôn là USER, kể cả khi gửi role=ADMIN)
curl -X POST http://localhost:8081/api/v1/auth/register -H "Content-Type: application/json" \
  -d '{"email":"a@example.com","password":"123456","fullName":"Nguyen Van A"}'

# Đăng nhập bằng email hoặc username
curl -X POST http://localhost:8081/api/v1/auth/login -H "Content-Type: application/json" \
  -d '{"identifier":"admin","password":"<ADMIN_PASSWORD>"}'

# Dùng token
curl http://localhost:8081/api/v1/auth/me -H "Authorization: Bearer <token>"
curl http://localhost:8081/api/v1/users   -H "Authorization: Bearer <token-admin>"
```

Response đăng nhập: `token`, `tokenType` (Bearer), `expiresInSeconds`, `id`, `username`, `fullName`, `email`, `role`.
Lỗi validation trả `{"tênTrường": "thông báo"}`; các lỗi khác trả `{"error": "..."}`.

## Thiết kế bảo mật

- **BCrypt** (cost 10, salt ngẫu nhiên trong hash): bảng `users.password` chỉ chứa hash 60 ký tự.
- **JWT HS256**, payload gồm `sub` (id), `role`, `iss`, `iat`, `exp`. Claim `sub`, `role`, `exp` giống backend hiện tại. Kiểm tra chữ ký bằng so sánh hằng thời gian, từ chối `alg=none`, sai issuer, hết hạn.
- **Stateless**: không session, không CSRF; filter đọc `Authorization: Bearer`, role lấy từ token.
- Đăng nhập sai luôn trả cùng một thông báo và luôn chạy BCrypt (kể cả khi không có user) để không lộ tài khoản nào tồn tại.
- Không nhận `role` từ client khi đăng ký; mật khẩu giới hạn 72 byte vì BCrypt chỉ dùng 72 byte đầu.

### Giới hạn hiện tại (nên nói rõ khi báo cáo)

- Chưa có refresh token, logout phía server (thu hồi token), quên/đổi mật khẩu, khóa tài khoản khi đăng nhập sai nhiều lần.
- Role nằm trong token nên đổi role của user chỉ có hiệu lực sau khi token hết hạn.
- Backend monolith hiện tại vẫn có `/api/v1/auth` riêng và bảng `account`; chưa chuyển sang dùng auth-service. Các service khác chưa xác thực token của auth-service, việc này sẽ làm cùng API Gateway.
- Khóa HS256 là khóa đối xứng: service nào xác thực token cũng giữ cùng secret. Khi tách nhiều service có thể cân nhắc RS256.

## Test

```bash
./mvnw clean test        # Windows: mvnw.cmd clean test
./mvnw clean package     # build target/auth-service.jar
```

Test dùng H2 in-memory (`src/test/resources/application.properties`), không cần bật MySQL.

| File | Loại | Nội dung |
|---|---|---|
| `JwtServiceTest` | Unit | Sinh token, claims, hết hạn (Clock giả), giả mạo payload/chữ ký, alg=none, sai secret/issuer, token rác, cấu hình sai |
| `PasswordHashingTest` | Unit | Định dạng BCrypt, salt, matches đúng/sai, cost, tiếng Việt, hash rác |
| `AuthApiTest` | Integration | Đăng ký, đăng nhập, `/me`, 401/403/409/400, ADMIN và USER, token giả/hết hạn, mật khẩu lưu dạng hash |

## Docker

```bash
docker build -t auth-service .
docker compose up -d --build        # auth-service (8081) + MySQL riêng (cổng máy 3307)
```

`docker-compose.yml` trong thư mục này chỉ để chạy riêng auth-service. File compose chung cho cả hệ thống do thành viên BE 3 phụ trách.
