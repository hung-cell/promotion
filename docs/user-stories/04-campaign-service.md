# User Stories - Campaign Service

## Epic: Quản lý Chiến dịch Marketing

### 1. Quản lý Campaign (CRUD)

#### US-CA-001: Tạo campaign mới
**Là** Marketing Manager
**Tôi muốn** tạo một chiến dịch marketing mới
**Để** tổ chức các promotions theo chủ đề

**Tiêu chí chấp nhận:**
- Nhập tên, mô tả campaign
- Chọn loại campaign (Flash Sale, Seasonal, Holiday, Clearance)
- Set thời gian bắt đầu và kết thúc
- Set trạng thái ban đầu DRAFT
- Upload banner/images cho campaign
- Lưu vào MySQL

#### US-CA-002: Xem danh sách campaigns
**Là** Marketing Manager
**Tôi muốn** xem danh sách tất cả campaigns
**Để** quản lý và theo dõi các chiến dịch

**Tiêu chí chấp nhận:**
- Phân trang
- Filter theo status (DRAFT, SCHEDULED, ACTIVE, ENDED)
- Filter theo loại campaign
- Filter theo thời gian
- Search theo tên

#### US-CA-003: Cập nhật campaign
**Là** Marketing Manager
**Tôi muốn** cập nhật thông tin campaign
**Để** điều chỉnh chiến dịch theo nhu cầu

**Tiêu chí chấp nhận:**
- Sửa được campaign DRAFT và SCHEDULED
- Giới hạn sửa đổi khi campaign đang ACTIVE
- Ghi audit log
- Notify stakeholders khi có thay đổi

#### US-CA-004: Xóa campaign
**Là** Marketing Manager
**Tôi muốn** xóa campaign
**Để** loại bỏ campaign không cần thiết

**Tiêu chí chấp nhận:**
- Chỉ xóa được campaign DRAFT
- Soft delete
- Không xóa được campaign có promotions đang active

---

### 2. Quản lý Promotions trong Campaign

#### US-CA-005: Thêm promotion vào campaign
**Là** Marketing Manager
**Tôi muốn** thêm promotions vào campaign
**Để** nhóm các promotions liên quan

**Tiêu chí chấp nhận:**
- Chọn từ danh sách promotions available
- Một promotion có thể thuộc nhiều campaigns
- Validate thời gian promotion nằm trong campaign period
- Sync với Promotion Management Service

#### US-CA-006: Xóa promotion khỏi campaign
**Là** Marketing Manager
**Tôi muốn** xóa promotion khỏi campaign
**Để** điều chỉnh danh sách promotions

**Tiêu chí chấp nhận:**
- Remove association, không xóa promotion
- Update campaign statistics
- Audit log

#### US-CA-007: Xem promotions trong campaign
**Là** Marketing Manager
**Tôi muốn** xem danh sách promotions trong campaign
**Để** review và quản lý

**Tiêu chí chấp nhận:**
- Hiển thị tất cả promotions linked
- Show status của từng promotion
- Show performance metrics cơ bản

---

### 3. Scheduling

#### US-CA-008: Schedule campaign
**Là** Marketing Manager
**Tôi muốn** schedule campaign chạy tự động
**Để** campaign tự động activate đúng thời điểm

**Tiêu chí chấp nhận:**
- Set start time chính xác (date + time)
- Set end time
- Chuyển status sang SCHEDULED
- Scheduler job activate campaign đúng giờ
- Notify trước khi campaign start

#### US-CA-009: Tự động kết thúc campaign
**Là** System
**Tôi muốn** tự động kết thúc campaign khi hết thời gian
**Để** campaign không chạy quá thời hạn

**Tiêu chí chấp nhận:**
- Scheduler job check và end campaigns
- Chuyển status sang ENDED
- Deactivate linked promotions (optional)
- Gửi summary report

#### US-CA-010: Extend campaign
**Là** Marketing Manager
**Tôi muốn** gia hạn campaign đang chạy
**Để** kéo dài thời gian khuyến mãi

**Tiêu chí chấp nhận:**
- Sửa end time của campaign ACTIVE
- Extend linked promotions tương ứng
- Notify stakeholders
- Audit log

---

### 4. Budget Management

#### US-CA-011: Set budget cho campaign
**Là** Marketing Manager
**Tôi muốn** set ngân sách cho campaign
**Để** kiểm soát chi phí khuyến mãi

**Tiêu chí chấp nhận:**
- Set total budget amount
- Set budget per promotion (optional)
- Set budget per day (optional)
- Support multiple currencies

#### US-CA-012: Track budget usage
**Là** Marketing Manager
**Tôi muốn** theo dõi sử dụng ngân sách
**Để** biết còn bao nhiêu budget

**Tiêu chí chấp nhận:**
- Real-time budget consumed
- Budget remaining
- Projected spend based on current rate
- Alert khi gần hết budget

#### US-CA-013: Auto-pause khi hết budget
**Là** System
**Tôi muốn** tự động pause campaign khi hết budget
**Để** không vượt quá ngân sách

**Tiêu chí chấp nhận:**
- Monitor budget usage real-time
- Pause campaign khi đạt threshold (e.g., 95%)
- Notify Marketing Manager
- Option để manually resume với additional budget

---

### 5. Flash Sale

#### US-CA-014: Tạo Flash Sale campaign
**Là** Marketing Manager
**Tôi muốn** tạo Flash Sale với thời gian ngắn
**Để** tạo urgency cho khách hàng

**Tiêu chí chấp nhận:**
- Set duration ngắn (1-24 hours)
- Set quantity limit per product
- Countdown timer support
- High priority trong promotion engine

#### US-CA-015: Manage Flash Sale inventory
**Là** Marketing Manager
**Tôi muốn** set số lượng sản phẩm cho Flash Sale
**Để** kiểm soát stock bán ra

**Tiêu chí chấp nhận:**
- Set quantity per SKU
- Real-time quantity tracking
- Auto-end khi hết stock
- Integrate với Inventory Service

#### US-CA-016: Flash Sale queue management
**Là** System
**Tôi muốn** quản lý queue khi Flash Sale traffic cao
**Để** đảm bảo fair access

**Tiêu chí chấp nhận:**
- Virtual queue khi traffic spike
- Fair ordering
- Timeout handling
- Prevent bot/abuse

---

### 6. A/B Testing

#### US-CA-017: Tạo A/B test cho promotions
**Là** Marketing Manager
**Tôi muốn** A/B test các promotions khác nhau
**Để** tìm ra promotion hiệu quả nhất

**Tiêu chí chấp nhận:**
- Tạo variants (A, B, C...)
- Set traffic split percentage
- Random assignment cho customers
- Track metrics per variant

#### US-CA-018: Xem kết quả A/B test
**Là** Marketing Manager
**Tôi muốn** xem kết quả A/B test
**Để** quyết định promotion nào tốt hơn

**Tiêu chí chấp nhận:**
- Conversion rate per variant
- Revenue per variant
- Statistical significance
- Recommendation

#### US-CA-019: Apply winning variant
**Là** Marketing Manager
**Tôi muốn** apply variant thắng cho tất cả traffic
**Để** maximize hiệu quả

**Tiêu chí chấp nhận:**
- One-click apply winner
- Gradual rollout option
- Keep tracking metrics
- Audit log

---

### 7. Integration

#### US-CA-020: Sync với Promotion Management
**Là** System
**Tôi muốn** sync campaigns với Promotion Management
**Để** promotions được activate/deactivate theo campaign

**Tiêu chí chấp nhận:**
- Event-driven sync (Kafka)
- Activate promotions khi campaign start
- Deactivate khi campaign end
- Handle conflicts

#### US-CA-021: API cho Marketing Portal
**Là** Marketing Portal
**Tôi muốn** gọi Campaign Service APIs
**Để** quản lý campaigns

**Tiêu chí chấp nhận:**
- Full CRUD APIs
- Scheduling APIs
- Budget APIs
- A/B testing APIs
- Authentication với JWT

#### US-CA-022: API cho Storefront
**Là** Storefront
**Tôi muốn** lấy thông tin campaigns đang active
**Để** hiển thị cho customers

**Tiêu chí chấp nhận:**
- GET /api/v1/campaigns/active
- Include banners, promotions
- Filter by channel
- Cache response

