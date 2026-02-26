# User Stories - Coupon Service

## Epic: Quản lý Mã Giảm Giá và Voucher

### 1. Quản lý Coupon (CRUD)

#### US-CP-001: Tạo coupon đơn lẻ
**Là** Promotion Manager
**Tôi muốn** tạo một mã coupon cụ thể
**Để** có thể phát cho khách hàng sử dụng

**Tiêu chí chấp nhận:**
- Nhập mã coupon (hoặc auto-generate)
- Link với promotion đã tạo
- Set số lần sử dụng tối đa
- Set thời hạn sử dụng
- Validate mã không trùng lặp
- Lưu vào MySQL

#### US-CP-002: Generate coupon hàng loạt
**Là** Promotion Manager
**Tôi muốn** generate nhiều mã coupon cùng lúc
**Để** có thể phát cho chiến dịch marketing lớn

**Tiêu chí chấp nhận:**
- Nhập số lượng cần generate (lên đến 1 triệu codes)
- Chọn format mã (prefix, length, character set)
- Async processing cho bulk generation
- Progress tracking
- Export danh sách codes ra CSV
- Đảm bảo uniqueness của tất cả codes

#### US-CP-003: Xem danh sách coupon
**Là** Promotion Manager
**Tôi muốn** xem danh sách tất cả coupon
**Để** quản lý và theo dõi coupon

**Tiêu chí chấp nhận:**
- Phân trang với large dataset
- Filter theo status (ACTIVE, USED, EXPIRED, DISABLED)
- Filter theo promotion
- Filter theo thời gian tạo
- Search theo mã coupon

#### US-CP-004: Vô hiệu hóa coupon
**Là** Promotion Manager
**Tôi muốn** vô hiệu hóa coupon
**Để** coupon không thể sử dụng được nữa

**Tiêu chí chấp nhận:**
- Disable single coupon
- Bulk disable nhiều coupons
- Ghi lý do disable
- Update cache Redis
- Audit log

---

### 2. Validate Coupon

#### US-CP-005: Validate mã coupon
**Là** Promotion Engine Service
**Tôi muốn** validate mã coupon khách hàng nhập
**Để** xác định coupon có hợp lệ không

**Tiêu chí chấp nhận:**
- Check coupon tồn tại
- Check coupon chưa hết hạn
- Check coupon chưa bị disable
- Check còn quota sử dụng
- Response time < 50ms (cache trong Redis)

#### US-CP-006: Validate coupon cho customer cụ thể
**Là** Promotion Engine Service
**Tôi muốn** validate coupon có áp dụng được cho customer
**Để** đảm bảo customer eligible

**Tiêu chí chấp nhận:**
- Check customer chưa sử dụng coupon (nếu single-use per customer)
- Check customer thuộc segment được phép
- Check customer tier đủ điều kiện
- Gọi Customer Service để lấy thông tin

#### US-CP-007: Check coupon usage limit
**Là** System
**Tôi muốn** check số lần sử dụng coupon
**Để** không vượt quá giới hạn

**Tiêu chí chấp nhận:**
- Track global usage count
- Track per-customer usage count
- Atomic increment trong Redis
- Persist to MySQL định kỳ

---

### 3. Redeem Coupon

#### US-CP-008: Reserve coupon
**Là** Promotion Engine Service
**Tôi muốn** reserve coupon khi customer checkout
**Để** đảm bảo coupon không bị dùng bởi người khác

**Tiêu chí chấp nhận:**
- Atomic decrement quota trong Redis
- Tạo reservation record
- Set TTL cho reservation (5-15 phút)
- Return reservation ID

#### US-CP-009: Redeem coupon
**Là** Promotion Engine Service
**Tôi muốn** redeem coupon khi order thành công
**Để** ghi nhận coupon đã được sử dụng

**Tiêu chí chấp nhận:**
- Convert reservation thành redemption
- Update coupon status nếu single-use
- Persist redemption record vào MySQL
- Link với order ID
- Gửi event cho Analytics

#### US-CP-010: Release coupon reservation
**Là** Promotion Engine Service
**Tôi muốn** release reservation khi checkout timeout/cancel
**Để** coupon có thể được sử dụng lại

**Tiêu chí chấp nhận:**
- Increment quota trong Redis
- Delete reservation record
- Auto-release khi TTL expire
- Log release action

---

### 4. Phân phối Coupon

#### US-CP-011: Assign coupon cho customer
**Là** Marketing Service
**Tôi muốn** assign coupon cho customer cụ thể
**Để** customer có coupon trong account

**Tiêu chí chấp nhận:**
- API để assign coupon cho customer ID
- Bulk assign cho list customers
- Gửi notification cho customer
- Track assigned coupons per customer

#### US-CP-012: Claim coupon từ campaign
**Là** Customer
**Tôi muốn** claim coupon từ campaign
**Để** tôi có coupon để sử dụng

**Tiêu chí chấp nhận:**
- API để customer claim coupon
- Check campaign còn coupon available
- Check customer chưa claim
- Assign coupon cho customer
- Return coupon details

#### US-CP-013: Lấy danh sách coupon của customer
**Là** Customer App
**Tôi muốn** lấy danh sách coupon của customer
**Để** hiển thị trong My Coupons

**Tiêu chí chấp nhận:**
- API lấy coupons by customer ID
- Filter theo status (available, used, expired)
- Include promotion details
- Sort theo expiry date

---

### 5. Coupon Analytics

#### US-CP-014: Track coupon distribution
**Là** Promotion Manager
**Tôi muốn** xem thống kê phân phối coupon
**Để** biết được coupon đã được phát như thế nào

**Tiêu chí chấp nhận:**
- Số coupon đã generate
- Số coupon đã assign
- Số coupon đã claim
- Distribution by channel

#### US-CP-015: Track coupon redemption
**Là** Promotion Manager
**Tôi muốn** xem thống kê sử dụng coupon
**Để** đánh giá hiệu quả coupon

**Tiêu chí chấp nhận:**
- Số coupon đã redeem
- Redemption rate
- Revenue từ orders có coupon
- Average discount per redemption

---

### 6. Fraud Detection

#### US-CP-016: Detect coupon abuse
**Là** System
**Tôi muốn** detect hành vi lạm dụng coupon
**Để** ngăn chặn fraud

**Tiêu chí chấp nhận:**
- Detect multiple redemptions từ same IP
- Detect unusual redemption patterns
- Detect coupon sharing
- Alert khi phát hiện suspicious activity
- Auto-disable coupon nếu cần

#### US-CP-017: Blacklist customers
**Là** Fraud Team
**Tôi muốn** blacklist customers lạm dụng coupon
**Để** họ không thể sử dụng coupon nữa

**Tiêu chí chấp nhận:**
- Add customer to blacklist
- Check blacklist khi validate coupon
- Temporary vs permanent blacklist
- Audit log cho blacklist actions

---

### 7. Integration

#### US-CP-018: API cho Promotion Engine
**Là** Promotion Engine Service
**Tôi muốn** gọi Coupon Service APIs
**Để** validate và redeem coupons

**Tiêu chí chấp nhận:**
- POST /api/v1/coupons/validate
- POST /api/v1/coupons/reserve
- POST /api/v1/coupons/redeem
- POST /api/v1/coupons/release
- Authentication với service account

#### US-CP-019: API cho Marketing Service
**Là** Marketing Service
**Tôi muốn** gọi Coupon Service APIs
**Để** phân phối coupons cho campaigns

**Tiêu chí chấp nhận:**
- POST /api/v1/coupons/generate
- POST /api/v1/coupons/assign
- GET /api/v1/coupons/available
- Webhook khi coupon được redeem

