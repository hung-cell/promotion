# Hướng Dẫn Kịch Bản Test (E2E) - Promotion Service

Dưới đây là 2 kịch bản (Scenario) nghiệp vụ kinh điển mang tính hệ thống. Các request đã được thiết kế chuẩn xác dữ liệu đầu vào. Bạn cứ lần lượt copy từng `curl` vào Postman và chạy theo đúng thứ tự (Top-down) nhé.

---

## 🎯 Kịch Bản 1: Siêu Sale Khách VIP (Giảm 20% đơn từ 500k)
**Business Flow:** Marketing tung mã giảm giá 20% (tối đa 50k) chỉ dành cho khách mác VIP, và chỉ áp dụng cho đơn hàng >= 500,000đ. Đơn < 500k sẽ bị chặn.

### 1. (Admin) Tạo Promotion
```bash
curl --location 'http://localhost:8080/api/v1/promotions' \
--header 'Content-Type: application/json' \
--data '{
    "name": "[SCENARIO 1] Siêu Sale Khách VIP",
    "description": "Giảm 20% tối đa 50k cho đơn từ 500k",
    "type": "PERCENTAGE",
    "discountValue": 20.0,
    "maxDiscountAmount": 50000.0,
    "minOrderValue": 500000.0,
    "startDate": "2026-01-01T00:00:00",
    "endDate": "2026-12-31T23:59:59",
    "usageLimit": 1000,
    "usageLimitPerCustomer": 1
}'
```
*(Giả sử ID trả về là `1`, mình sẽ dùng ID=1 cho phần dưới)*

### 2. (Admin) Thêm Quy Tắc Tính Giảm Giá (BẮT BUỘC)
```bash
curl --location 'http://localhost:8080/api/v1/promotions/1/rules' \
--header 'Content-Type: application/json' \
--data '{
    "type": "PERCENTAGE_DISCOUNT",
    "discountPercent": 20.0,
    "maxDiscountAmount": 50000.0,
    "minOrderValue": 500000.0
}'
```

### 3. (Admin) Thêm Điều Kiện (Chỉ VIP mới được dùng)
```bash
curl --location 'http://localhost:8080/api/v1/promotions/1/conditions' \
--header 'Content-Type: application/json' \
--data '{
    "type": "CUSTOMER_SEGMENT",
    "customerTiers": ["VIP"]
}'
```

### 4. (Admin) Kích hoạt Khuyến Mãi
```bash
curl --location --request POST 'http://localhost:8080/api/v1/promotions/1/activate'
```

### 5. (Admin) Tạo mã giảm giá (Coupon Code)
```bash
curl --location 'http://localhost:8080/api/v1/coupons' \
--header 'Content-Type: application/json' \
--data '{
    "promotionId": 1,
    "code": "VIP_SALE_20",
    "maxUses": 100,
    "expiryDate": "2026-12-31T23:59:59"
}'
```

### 6. (Client) Khách Hàng kiểm tra mã lúc ở Giỏ Hàng
Khách hàng "USER_VIP_01" mua đơn hàng 600K -> **HỢP LỆ (200 OK)**
```bash
curl --location 'http://localhost:8080/api/v1/coupons/validate' \
--header 'Content-Type: application/json' \
--data '{
    "code": "VIP_SALE_20",
    "customerId": "USER_VIP_01",
    "orderAmount": 600000.0,
    "cartItemSkus": ["PROD_VIP"]
}'
```
*(Ghi chú: Nếu bạn thử đổi `orderAmount` thành 300,000đ, API sẽ báo Validation Exception vì không đủ điều kiện hoá đơn 500k)*

### 7. (Client) Khách bấm Thanh toán -> Hệ thống Giữ chỗ (Reserve) mã
```bash
curl --location 'http://localhost:8080/api/v1/coupons/reserve' \
--header 'Content-Type: application/json' \
--data '{
    "code": "VIP_SALE_20",
    "customerId": "USER_VIP_01",
    "orderId": "ORDER_VIP_999"
}'
```
*(Chú ý: Báo thành công, hãy Copy cất cái `reservationId` ở response lại nhé)*

### 8. (Client) Thanh toán thành công -> Hệ thống xác nhận dùng mã (Redeem)
Webhook trả tiền xong, gạch mã đi vĩnh viễn. Thay thế chuỗi `<MA_RESERVATION_O_BUOC_7>` bằng ID bạn giữ lại khi nãy.
```bash
curl --location 'http://localhost:8080/api/v1/coupons/redeem' \
--header 'Content-Type: application/json' \
--data '{
    "reservationId": "<MA_RESERVATION_O_BUOC_7>",
    "orderId": "ORDER_VIP_999"
}'
```

---

## 🎯 Kịch Bản 2: Voucher Freeship Đại Trà (Không kén khách)
**Business Flow:** Marketing ra mắt một mã Freeship giảm trọn gói 30k tiền ship, áp dụng cho TẤT CẢ mọi người. Mọi đơn từ 0đ cũng áp dụng được.

### 1. Tạo Promotion
```bash
curl --location 'http://localhost:8080/api/v1/promotions' \
--header 'Content-Type: application/json' \
--data '{
    "name": "[SCENARIO 2] Freeship Mọi Nhà",
    "description": "Giảm 30K phí ship cho mọi đơn hàng",
    "type": "FIXED_AMOUNT",
    "discountValue": 30000.0,
    "maxDiscountAmount": 30000.0,
    "minOrderValue": 0.0,
    "startDate": "2026-01-01T00:00:00",
    "endDate": "2026-12-31T23:59:59",
    "usageLimit": 50000,
    "usageLimitPerCustomer": 3
}'
```
*(Giả sử hệ thống sẽ trả về ID tiếp theo là `2`)*

### 2. Thêm Điều kiện Quy tắc (Quy tắc giảm Fix/Freeship) (BẮT BUỘC)
```bash
curl --location 'http://localhost:8080/api/v1/promotions/2/rules' \
--header 'Content-Type: application/json' \
--data '{
    "type": "FIXED_DISCOUNT",
    "discountAmount": 30000.0,
    "minOrderValue": 0.0
}'
```

*(Vì kịch bản này là ĐẠI TRÀ, nên ta **KHÔNG CẦN TẠO ĐIỀU KIỆN Condition VIP**, để mặc định là mọi khách đều dùng. Bỏ qua luôn sang bước Active)*

### 3. Kích hoạt Khuyến Mãi (Active)
```bash
curl --location --request POST 'http://localhost:8080/api/v1/promotions/2/activate'
```

### 4. Tạo mã Coupon
```bash
curl --location 'http://localhost:8080/api/v1/coupons' \
--header 'Content-Type: application/json' \
--data '{
    "promotionId": 2,
    "code": "FREESHIP_30K",
    "maxUses": 50000,
    "expiryDate": "2026-12-31T23:59:59"
}'
```

### 5. Khách Hàng Kiểm Tra Mã (Dù mua đơn 10k vẫn chạy mượt)
```bash
curl --location 'http://localhost:8080/api/v1/coupons/validate' \
--header 'Content-Type: application/json' \
--data '{
    "code": "FREESHIP_30K",
    "customerId": "USER_THONGTHUONG_22",
    "orderAmount": 10000.0,
    "cartItemSkus": ["PROD_TAT"]
}'
```

### 6. Khách Hàng Đặt Chỗ (Reserve)
```bash
curl --location 'http://localhost:8080/api/v1/coupons/reserve' \
--header 'Content-Type: application/json' \
--data '{
    "code": "FREESHIP_30K",
    "customerId": "USER_THONGTHUONG_22",
    "orderId": "ORDER_NORMAL_852"
}'
```

### 7. Khách HỦY THANH TOÁN DO HẾT TIỀN (Giải phóng mã - Release)
Thay vì thanh toán thành công, khách chuyển ra ngoài màn ngân hàng rồi tắt mạng. Order Service sau khi check không thấy thanh toán, sẽ gọi nhả mã ra để lấy lại 1 lượt dùng cho khách đó. Thay ID vừa lấy được ở trên vào.
```bash
curl --location --request POST 'http://localhost:8080/api/v1/coupons/release/<ID_RESERVATION_O_BUOC_6>'
```

---

## �️ Luồng Bảo Trì (Quản Lý Admin)

### Bước A: Xem lịch sử thao tác của Admin với Khuyến mãi ID = 1 (Audit Log)
```bash
curl --location 'http://localhost:8080/api/v1/promotions/1/audit-logs'
```

### Bước B: Khẩn Cấp Tắt Hẳn Khuyến Mãi Freeship (Stop Campaign)
Marketing phát hiện lỗi ngân sách, cần tắt gấp cái Khuyến mãi ID = 2. Sau bước này, mọi code `FREESHIP_30K` gọi validate đều báo lỗi.
```bash
curl --location --request POST 'http://localhost:8080/api/v1/promotions/2/disable' \
--header 'Content-Type: application/json' \
--data '{
    "reason": "Hết Budget Marketing do chạy quảng cáo sai đối tượng"
}'
```
