# Hướng dẫn chạy và Luồng Business - Promotion Service

## 1. Hướng Dẫn Chạy Source Code (Local Development)

Project này được dựng bằng Java 17, Spring Boot 3 và MySQL. Đã có sẵn file `docker-compose.yml` để bạn dễ dàng dựng database.

### Bước 1: Khởi động Database
Bạn cần phải chạy MySQL lên trước bằng Docker. Mở terminal tại thư mục project và chạy lệnh:
```bash
docker-compose up -d
```
*Lệnh này sẽ tải image MySQL 8, chạy ở port `3306` và tự động tạo database tên là `promotion_db`.*

### Bước 2: Chạy Ứng Dụng Spring Boot
Chạy ứng dụng bằng Maven với profile `dev`:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
*(Nếu bạn dùng IntelliJ IDEA hoặc Eclipse, bạn cũng có thể mở class `PromotionApplication.java` và ấn Run trực tiếp, hãy nhớ set Active Profile là `dev`)*

### Bước 3: Truy cập API Documentation (Swagger)
Sau khi ứng dụng chạy thành công trên port `8080`, bạn có thể vào đường dẫn sau trên trình duyệt để xem toàn bộ danh sách API và test thử:
- 👉 **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 2. Luồng Chạy Business (Business Flow)

Promotion Service quản lý các chiến dịch khuyến mãi (Promotion), các mã giảm giá cụ thể (Coupon), các quy tắc áp dụng (Rule, Condition) và cấu hình cộng dồn mã (Stacking). Dưới đây là 3 luồng hoạt động chính:

### A. Luồng Tạo và Cấu Hình Khuyến Mãi (Admin Flow)
Dành cho người quản trị hệ thống để setup một chiến dịch khuyến mãi mới:
1. **Tạo Promotion:** Gọi API `POST /api/v1/promotions` (Trạng thái khởi tạo sẽ là `DRAFT`).
2. **Thêm Điều Kiện (Conditions):** Gọi API thêm `Condition` (ví dụ: *Giá trị đơn hàng tối thiểu là 200k*).
3. **Thêm Quy Tắc Giảm Giá (Rules):** Gọi API thêm `Rule` (ví dụ: *Giảm giá 10%* hoặc *Giảm thẳng 50k*).
4. **Cấu Hình Cộng Dồn (Stacking Config - Tùy chọn):** Thiết lập xem mã này có được dùng chung với mã của chương trình khác không.
5. **Kích Hoạt (Activate):** Gọi API `POST /api/v1/promotions/{id}/activate` để chuyển promotion sang trạng thái `ACTIVE`.
6. **Tạo Coupon:** Sau khi Promotion đã Active, admin sinh ra các mã Coupon thực tế thuộc khuyến mãi đó để phát hành cho User.

### B. Luồng Sử Dụng Mã Giảm Giá của User (Checkout Flow)
Khi người dùng áp mã ở màn hình Thanh toán (Checkout):
1. **Kiểm tra/Xác thực (Validate):** Hệ thống gọi API Validate Coupon để kiểm tra xem mã có hợp lệ không (còn lượt không, có quá hạn không, đơn hàng có thoả mãn điều kiện/condition của mã không).
2. **Giữ Mã (Reserve/Lock):** Người dùng bấm "Đặt hàng". Hệ thống lập tức gọi API Reserve Coupon để "giữ chỗ" mã giảm giá này. Điều này tránh việc 2 người cùng áp 1 mã nhưng mã đó chỉ còn đúng 1 lượt dùng (Optimistic Locking sẽ đảm bảo không bị vượt quá số lượng).
3. **Sử Dụng (Redeem):**
   - Nếu User *thanh toán thành công*, gọi API Redeem để chính thức ghi nhận mã đã được sử dụng.
   - Nếu User *hủy thao tác hoặc thanh toán lỗi*, mã khóa này sẽ tự động được hệ thống ngầm (Background Job) giải phóng (release) sau 15 phút, trả lại lượt dùng cho người khác.

### C. Luồng Xử Lý Ngầm (Background Jobs)
Hệ thống có các tác vụ định kỳ tự chạy (Scheduler) để dọn dẹp data:
- Tự động thay đổi trạng thái các Promotion thành `EXPIRED` (Hết hạn) nếu thời gian hiện tại đã vượt qua `endDate`.
- Tự động **Release** (mở khóa) các Coupon Reservation (phiếu giữ mã) nếu đã giữ quá 15 phút mà không thấy gọi lệnh Redeem.
