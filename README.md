# 🛒 Ecommerce Web Application (Full-Stack & Microservices)

E-commerce project developed using Java, Spring Boot, React, and Microsoft SQL Server.

---

## 🏗️ 1. Docker Compose cũ (monolith)

File `docker-compose.yml` ở root chạy backend monolith cũ trên cổng 8080. Không dùng lệnh này khi kiểm tra frontend qua API Gateway; hãy chạy các microservice và `api-gateway` riêng, đồng thời đặt `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_PASSWORD` và `INTERNAL_API_KEY` trong môi trường.

Hệ thống được đóng gói và cấu hình hoàn chỉnh bằng **Docker Compose**, cho phép khởi chạy cả hệ thống (Database SQL Server 2022, Backend, Frontend) chỉ với duy nhất 1 câu lệnh.

### 📋 Yêu cầu tiên quyết (Docker):
* Đã cài đặt **Docker Desktop** (trên Windows / macOS) hoặc **Docker Engine & Docker Compose** (trên Linux).
* Docker Desktop đang ở trạng thái **Running**.

### 🚀 Lệnh khởi chạy Docker:
```bash
docker compose up --build -d
```
*(Đối với Docker Compose phiên bản cũ, dùng `docker-compose up -d`)*

### 🌐 Các cổng & Đường dẫn dịch vụ Docker:

| Thành phần | Cổng Host | Đường dẫn truy cập (URL) | Ghi chú |
| :--- | :---: | :--- | :--- |
| **Frontend (Giao diện Web)** | `5173` / `80` | [http://localhost:5173](http://localhost:5173) | Giao diện người dùng mua sắm |
| **Backend API & Swagger UI** | `8080` | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) | Tài liệu & Test REST API |
| **SQL Server 2022** | `1433` | `localhost,1433` | Database: `Ecommerce` (User: `sa`, password từ `MSSQL_SA_PASSWORD`) |

#### 🔑 Tài khoản Quản trị viên (Admin) mặc định:
* **Username / Email:** `admin` hoặc `admin@gmail.com`
* **Password:** Giá trị `ADMIN_PASSWORD` do người chạy cấu hình.

---

## 💻 2. Chạy thủ công trên máy (Local Development)

### 📋 Requirements:
- **JDK:** Java 17 (hoặc JDK 21 / JDK 26)
- **Build Tool:** Maven
- **Database:** Microsoft SQL Server

### 🗄️ Database Setup:
Tạo cơ sở dữ liệu `Ecommerce` trong SQL Server:
```sql
CREATE DATABASE Ecommerce;
```

### ⚙️ application.properties:
⚠️ **LƯU Ý:** Trước khi chạy Backend trên máy thật, vui lòng cập nhật thông tin kết nối Database trong:
`backend/src/main/resources/application.properties`

---

## 📡 3. Microservice Ports & API Base URLs

| Service | Port | Base URL |
| :--- | :---: | :--- |
| **Auth Service** | `8081` | `http://localhost:8081` |
| **Category Service** | `8082` | `http://localhost:8082` |
| **Order Service** | `8083` | `http://localhost:8083` |
| **Product Service** | `8084` | `http://localhost:8084` |

---

## 🛠️ 4. Các lệnh Docker quản trị thường dùng

* **Kiểm tra trạng thái container:**
  ```bash
  docker compose ps
  ```
* **Xem log thời gian thực:**
  ```bash
  docker compose logs -f
  ```
* **Tắt hệ thống:**
  ```bash
  docker compose down
  ```
* **Tắt và xóa sạch toàn bộ dữ liệu database để làm mới từ đầu:**
  ```bash
  docker compose down -v
  ```

## 🗄️ 5. Quy tắc đặt tên Database

Database của các Microservice được đặt theo quy tắc:

```text
Ecommerce_(Function)
VD: Ecommerce_Auth
