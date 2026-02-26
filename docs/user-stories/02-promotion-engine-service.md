# User Stories - Promotion Engine Service

## Epic: Tính toán và Áp dụng Khuyến mãi Real-time

### 1. Tính toán Promotion

#### US-PE-001: Tính toán promotion cho cart
**Là** Cart Service
**Tôi muốn** gọi API tính toán promotion cho giỏ hàng
**Để** khách hàng thấy được giá sau khuyến mãi

**Tiêu chí chấp nhận:**
- Nhận request với cart items, customer info, channel
- Tính toán tất cả promotions applicable
- Trả về danh sách promotions được áp dụng và số tiền giảm
- Response time < 100ms (đọc từ Redis cache)
- Handle high throughput (1000+ requests/second)

#### US-PE-002: Tính toán promotion cho single product
**Là** Product Listing Service
**Tôi muốn** lấy thông tin promotion cho một sản phẩm
**Để** hiển thị giá khuyến mãi trên listing

**Tiêu chí chấp nhận:**
- Nhận SKU và context (channel, customer segment)
- Trả về best applicable promotion
- Trả về giá sau khuyến mãi
- Support batch request cho multiple SKUs
- Cache kết quả trong Redis

#### US-PE-003: Tính toán stacking promotions
**Là** System
**Tôi muốn** tính toán khi có nhiều promotions áp dụng
**Để** khách hàng được hưởng đúng ưu đãi theo rules

**Tiêu chí chấp nhận:**
- Áp dụng stacking rules từ Promotion Management
- Tính toán theo priority order
- Respect exclusive groups
- Apply maximum discount cap
- Trả về breakdown chi tiết từng promotion

#### US-PE-004: Tính toán promotion mua X tặng Y
**Là** System
**Tôi muốn** tính toán promotion buy X get Y
**Để** tự động thêm sản phẩm tặng vào cart

**Tiêu chí chấp nhận:**
- Detect khi cart đủ điều kiện
- Trả về sản phẩm Y cần thêm vào cart
- Tính toán số lượng Y dựa trên số lượng X
- Check inventory của sản phẩm Y (gọi Inventory Service)

---

### 2. Validate Promotion

#### US-PE-005: Validate điều kiện áp dụng promotion
**Là** Order Service
**Tôi muốn** validate promotion có thể áp dụng cho order
**Để** đảm bảo promotion hợp lệ trước khi place order

**Tiêu chí chấp nhận:**
- Validate thời gian promotion còn hiệu lực
- Validate điều kiện sản phẩm
- Validate điều kiện khách hàng
- Validate điều kiện đơn hàng (min value, min quantity)
- Validate usage limit chưa vượt quá
- Trả về chi tiết lý do nếu invalid

#### US-PE-006: Validate coupon code
**Là** Checkout Service
**Tôi muốn** validate mã coupon khách hàng nhập
**Để** áp dụng coupon nếu hợp lệ

**Tiêu chí chấp nhận:**
- Gọi Coupon Service để validate code
- Check coupon chưa hết hạn
- Check coupon chưa sử dụng hết quota
- Check customer eligible cho coupon
- Trả về promotion details nếu valid

#### US-PE-007: Validate promotion không conflict
**Là** System
**Tôi muốn** validate các promotions được chọn không conflict
**Để** tránh áp dụng sai rules

**Tiêu chí chấp nhận:**
- Check exclusive groups
- Check stacking rules
- Suggest best combination nếu có conflict
- Trả về warning nếu có promotions bị loại

---

### 3. Apply Promotion

#### US-PE-008: Reserve promotion usage
**Là** Order Service
**Tôi muốn** reserve promotion usage khi customer checkout
**Để** đảm bảo quota không bị race condition

**Tiêu chí chấp nhận:**
- Tạo reservation record
- Decrement available quota (atomic operation trong Redis)
- Set TTL cho reservation (auto release nếu order không complete)
- Return reservation ID

#### US-PE-009: Commit promotion usage
**Là** Order Service
**Tôi muốn** commit promotion usage khi order placed thành công
**Để** ghi nhận promotion đã được sử dụng

**Tiêu chí chấp nhận:**
- Convert reservation thành usage record
- Persist vào MySQL
- Update usage statistics
- Gửi event cho Analytics Service

#### US-PE-010: Release promotion reservation
**Là** Order Service
**Tôi muốn** release reservation khi order bị cancel
**Để** quota được trả lại

**Tiêu chí chấp nhận:**
- Increment available quota trong Redis
- Delete reservation record
- Log release action

---

### 4. Sync và Cache

#### US-PE-011: Sync promotion rules từ Management Service
**Là** System
**Tôi muốn** sync promotion rules từ Promotion Management
**Để** Engine luôn có rules mới nhất

**Tiêu chí chấp nhận:**
- Listen events từ Promotion Management (Kafka)
- Update local cache khi có changes
- Full sync định kỳ để đảm bảo consistency
- Handle version conflict

#### US-PE-012: Cache promotion rules trong Redis
**Là** System
**Tôi muốn** cache promotion rules trong Redis
**Để** tính toán nhanh không cần query database

**Tiêu chí chấp nhận:**
- Cache active promotions
- Cache promotion rules và conditions
- Set appropriate TTL
- Invalidate cache khi có updates
- Fallback to database nếu cache miss

#### US-PE-013: Warm up cache khi service start
**Là** System
**Tôi muốn** load tất cả active promotions vào cache khi start
**Để** service ready ngay sau khi deploy

**Tiêu chí chấp nhận:**
- Load từ database khi application startup
- Populate Redis cache
- Health check chỉ pass sau khi cache ready
- Log số lượng promotions loaded

---

### 5. Integration APIs

#### US-PE-014: API cho Order Service
**Là** Order Service
**Tôi muốn** gọi API để calculate và apply promotion cho order
**Để** order có đúng giá sau khuyến mãi

**Tiêu chí chấp nhận:**
- POST /api/v1/promotions/calculate - tính toán promotion
- POST /api/v1/promotions/validate - validate trước khi apply
- POST /api/v1/promotions/reserve - reserve quota
- POST /api/v1/promotions/commit - commit usage
- POST /api/v1/promotions/release - release reservation
- Authenticate với API key hoặc JWT

#### US-PE-015: API cho Cart Service
**Là** Cart Service
**Tôi muốn** gọi API preview promotion cho cart
**Để** hiển thị giá khuyến mãi real-time

**Tiêu chí chấp nhận:**
- POST /api/v1/promotions/preview - preview không reserve
- Support partial cart (chỉ một số items)
- Response bao gồm applicable promotions và savings
- Low latency < 50ms

#### US-PE-016: API cho POS Service
**Là** POS Service
**Tôi muốn** gọi API tính promotion cho offline orders
**Để** POS có thể áp dụng promotion khi offline

**Tiêu chí chấp nhận:**
- Sync promotion rules về POS
- API để download active promotions
- Support offline calculation
- Reconcile khi POS online lại

---

### 6. Performance và Monitoring

#### US-PE-017: Handle high throughput
**Là** System
**Tôi muốn** handle 10,000+ requests/second
**Để** không bị bottleneck trong peak hours

**Tiêu chí chấp nhận:**
- Horizontal scaling với multiple instances
- Connection pooling cho Redis và MySQL
- Async processing where possible
- Circuit breaker cho external service calls

#### US-PE-018: Monitor performance metrics
**Là** DevOps
**Tôi muốn** monitor performance của Promotion Engine
**Để** phát hiện và xử lý issues kịp thời

**Tiêu chí chấp nhận:**
- Track response time percentiles (p50, p95, p99)
- Track throughput (requests/second)
- Track cache hit/miss ratio
- Track error rate
- Alert khi metrics vượt threshold

