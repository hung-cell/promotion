# User Stories - Promotion Analytics Service

## Epic: Tracking, Reporting và Dashboard

### 1. Event Tracking

#### US-PA-001: Track promotion views
**Là** System
**Tôi muốn** track khi promotion được hiển thị
**Để** đo lường impression

**Tiêu chí chấp nhận:**
- Receive events từ Storefront/App
- Track promotion ID, customer ID, channel, timestamp
- Store trong time-series database hoặc Kafka
- Aggregate counts per promotion

#### US-PA-002: Track promotion clicks
**Là** System
**Tôi muốn** track khi customer click vào promotion
**Để** đo lường engagement

**Tiêu chí chấp nhận:**
- Track click events
- Track source (banner, listing, cart)
- Calculate CTR (Click-through rate)
- Store raw events cho analysis

#### US-PA-003: Track promotion applications
**Là** System
**Tôi muốn** track khi promotion được áp dụng
**Để** đo lường conversion

**Tiêu chí chấp nhận:**
- Receive events từ Promotion Engine
- Track order ID, promotion ID, discount amount
- Track customer info
- Real-time aggregation

#### US-PA-004: Track coupon redemptions
**Là** System
**Tôi muốn** track khi coupon được sử dụng
**Để** đo lường coupon performance

**Tiêu chí chấp nhận:**
- Receive events từ Coupon Service
- Track coupon code, customer, order
- Track discount value
- Aggregate by coupon batch/campaign

---

### 2. Real-time Dashboard

#### US-PA-005: Dashboard tổng quan promotions
**Là** Marketing Manager
**Tôi muốn** xem dashboard tổng quan
**Để** monitor performance real-time

**Tiêu chí chấp nhận:**
- Total active promotions
- Total discount given today
- Top performing promotions
- Conversion rate overview
- Auto-refresh mỗi 30 seconds

#### US-PA-006: Dashboard chi tiết promotion
**Là** Marketing Manager
**Tôi muốn** xem dashboard chi tiết một promotion
**Để** analyze performance cụ thể

**Tiêu chí chấp nhận:**
- Impressions, clicks, conversions
- Conversion funnel
- Revenue generated
- Discount given
- Usage over time chart

#### US-PA-007: Dashboard campaigns
**Là** Marketing Manager
**Tôi muốn** xem dashboard campaigns
**Để** monitor campaign performance

**Tiêu chí chấp nhận:**
- Campaign status overview
- Budget utilization
- Promotions performance trong campaign
- Comparison với previous campaigns

#### US-PA-008: Dashboard coupons
**Là** Marketing Manager
**Tôi muốn** xem dashboard coupons
**Để** monitor coupon usage

**Tiêu chí chấp nhận:**
- Coupons distributed vs redeemed
- Redemption rate
- Top used coupons
- Fraud alerts

---

### 3. Reports

#### US-PA-009: Promotion performance report
**Là** Marketing Manager
**Tôi muốn** generate báo cáo hiệu quả promotion
**Để** đánh giá và báo cáo leadership

**Tiêu chí chấp nhận:**
- Select date range
- Select promotions
- Metrics: impressions, clicks, conversions, revenue, discount
- Export PDF/Excel
- Schedule automated reports

#### US-PA-010: ROI report
**Là** Marketing Manager
**Tôi muốn** xem báo cáo ROI của promotions
**Để** đánh giá hiệu quả đầu tư

**Tiêu chí chấp nhận:**
- Revenue generated từ promotion
- Cost (discount given)
- ROI calculation
- Comparison across promotions
- Trend over time

#### US-PA-011: Customer behavior report
**Là** Marketing Analyst
**Tôi muốn** xem báo cáo hành vi khách hàng với promotions
**Để** hiểu customer response

**Tiêu chí chấp nhận:**
- Customer segments response rate
- Repeat purchase rate với promotion
- Average order value với/không promotion
- Customer acquisition cost

#### US-PA-012: Coupon usage report
**Là** Marketing Manager
**Tôi muốn** xem báo cáo sử dụng coupon
**Để** đánh giá coupon campaigns

**Tiêu chí chấp nhận:**
- Distribution vs redemption
- Redemption by channel
- Redemption by customer segment
- Unused/expired coupons

---

### 4. Alerts và Notifications

#### US-PA-013: Alert khi promotion performance thấp
**Là** Marketing Manager
**Tôi muốn** nhận alert khi promotion không hiệu quả
**Để** có thể điều chỉnh kịp thời

**Tiêu chí chấp nhận:**
- Set threshold cho conversion rate
- Alert qua email/Slack
- Include promotion details và metrics
- Suggestion để improve

#### US-PA-014: Alert khi budget gần hết
**Là** Marketing Manager
**Tôi muốn** nhận alert khi campaign budget gần hết
**Để** quyết định có thêm budget không

**Tiêu chí chấp nhận:**
- Alert ở 80%, 90%, 95% budget
- Include current spend và projected
- Link để add budget
- Alert qua multiple channels

#### US-PA-015: Alert fraud detection
**Là** Fraud Team
**Tôi muốn** nhận alert khi phát hiện fraud
**Để** xử lý kịp thời

**Tiêu chí chấp nhận:**
- Real-time fraud detection
- Alert với details (customer, pattern)
- Priority levels
- Action buttons (block, investigate)

---

### 5. Data Export và Integration

#### US-PA-016: Export data cho BI tools
**Là** Data Analyst
**Tôi muốn** export data sang BI tools
**Để** phân tích sâu hơn

**Tiêu chí chấp nhận:**
- API để query raw data
- Support date range filters
- Export formats: JSON, CSV
- Pagination cho large datasets

#### US-PA-017: Integration với Data Warehouse
**Là** System
**Tôi muốn** sync data sang Data Warehouse
**Để** centralized analytics

**Tiêu chí chấp nhận:**
- Scheduled ETL jobs
- Incremental sync
- Data transformation
- Error handling và retry

#### US-PA-018: Webhook cho external systems
**Là** External System
**Tôi muốn** nhận events qua webhook
**Để** integrate với third-party analytics

**Tiêu chí chấp nhận:**
- Configure webhook endpoints
- Select event types
- Retry logic
- Authentication

---

### 6. Historical Analysis

#### US-PA-019: Trend analysis
**Là** Marketing Analyst
**Tôi muốn** xem trend của promotions over time
**Để** identify patterns

**Tiêu chí chấp nhận:**
- Compare periods (MoM, YoY)
- Seasonal patterns
- Growth trends
- Anomaly detection

#### US-PA-020: Promotion comparison
**Là** Marketing Manager
**Tôi muốn** so sánh hiệu quả các promotions
**Để** learn best practices

**Tiêu chí chấp nhận:**
- Side-by-side comparison
- Multiple metrics
- Statistical significance
- Recommendations

---

### 7. APIs

#### US-PA-021: API cho Dashboard
**Là** Dashboard Frontend
**Tôi muốn** gọi APIs để lấy data
**Để** render charts và metrics

**Tiêu chí chấp nhận:**
- GET /api/v1/analytics/overview
- GET /api/v1/analytics/promotions/{id}
- GET /api/v1/analytics/campaigns/{id}
- GET /api/v1/analytics/coupons
- Support date range, aggregation params

#### US-PA-022: API cho Reports
**Là** Report Service
**Tôi muốn** gọi APIs để generate reports
**Để** tạo báo cáo tự động

**Tiêu chí chấp nhận:**
- POST /api/v1/reports/generate
- GET /api/v1/reports/{id}/status
- GET /api/v1/reports/{id}/download
- Async processing cho large reports

