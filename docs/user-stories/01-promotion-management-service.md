# User Stories - Promotion Management Service

## Epic: Quản lý Chương trình Khuyến mãi

### 1. Quản lý Promotion (CRUD)

#### US-PM-001: Tạo chương trình khuyến mãi mới
**Là** Promotion Manager
**Tôi muốn** tạo một chương trình khuyến mãi mới
**Để** tôi có thể cung cấp ưu đãi cho khách hàng

**Tiêu chí chấp nhận:**
- Có thể nhập tên, mô tả promotion
- Có thể chọn loại promotion (giảm theo %, giảm số tiền cố định, mua X tặng Y, miễn phí vận chuyển)
- Có thể đặt thời gian bắt đầu và kết thúc
- Có thể đặt trạng thái ban đầu là DRAFT
- Hệ thống validate dữ liệu đầu vào
- Lưu thành công vào database MySQL
- Cache promotion mới vào Redis

#### US-PM-002: Xem danh sách chương trình khuyến mãi
**Là** Promotion Manager
**Tôi muốn** xem danh sách tất cả chương trình khuyến mãi
**Để** tôi có thể quản lý và theo dõi các promotion

**Tiêu chí chấp nhận:**
- Hiển thị danh sách với phân trang
- Có thể filter theo status (DRAFT, ACTIVE, EXPIRED, DISABLED)
- Có thể filter theo thời gian
- Có thể search theo tên promotion
- Có thể sort theo ngày tạo, ngày bắt đầu, ngày kết thúc

#### US-PM-003: Xem chi tiết chương trình khuyến mãi
**Là** Promotion Manager
**Tôi muốn** xem chi tiết một chương trình khuyến mãi
**Để** tôi có thể kiểm tra thông tin và rules của promotion

**Tiêu chí chấp nhận:**
- Hiển thị đầy đủ thông tin promotion
- Hiển thị danh sách rules áp dụng
- Hiển thị điều kiện áp dụng (products, customers, channels)
- Hiển thị thống kê sử dụng cơ bản

#### US-PM-004: Cập nhật chương trình khuyến mãi
**Là** Promotion Manager
**Tôi muốn** cập nhật thông tin chương trình khuyến mãi
**Để** tôi có thể điều chỉnh promotion theo nhu cầu

**Tiêu chí chấp nhận:**
- Chỉ được sửa promotion ở trạng thái DRAFT hoặc ACTIVE
- Không được sửa promotion đã EXPIRED
- Cập nhật cache Redis sau khi sửa
- Ghi log audit trail

#### US-PM-005: Xóa chương trình khuyến mãi
**Là** Promotion Manager
**Tôi muốn** xóa chương trình khuyến mãi
**Để** tôi có thể loại bỏ promotion không cần thiết

**Tiêu chí chấp nhận:**
- Chỉ được xóa promotion ở trạng thái DRAFT
- Soft delete (đánh dấu deleted, không xóa vật lý)
- Xóa khỏi cache Redis
- Ghi log audit trail

---

### 2. Quản lý Lifecycle

#### US-PM-006: Kích hoạt chương trình khuyến mãi
**Là** Promotion Manager
**Tôi muốn** kích hoạt chương trình khuyến mãi
**Để** promotion có thể được áp dụng cho khách hàng

**Tiêu chí chấp nhận:**
- Chuyển trạng thái từ DRAFT sang ACTIVE
- Validate đầy đủ rules và điều kiện trước khi activate
- Sync promotion sang Promotion Engine Service
- Cache vào Redis
- Gửi event thông báo activation

#### US-PM-007: Vô hiệu hóa chương trình khuyến mãi
**Là** Promotion Manager
**Tôi muốn** vô hiệu hóa chương trình khuyến mãi đang chạy
**Để** tôi có thể dừng promotion khi cần

**Tiêu chí chấp nhận:**
- Chuyển trạng thái từ ACTIVE sang DISABLED
- Xóa khỏi cache Redis
- Notify Promotion Engine Service
- Ghi lý do disable

#### US-PM-008: Tự động hết hạn promotion
**Là** System
**Tôi muốn** tự động chuyển promotion sang EXPIRED khi hết thời gian
**Để** promotion không còn được áp dụng sau thời hạn

**Tiêu chí chấp nhận:**
- Scheduler job chạy định kỳ kiểm tra
- Chuyển trạng thái sang EXPIRED
- Xóa khỏi cache Redis
- Gửi notification cho Promotion Manager

---

### 3. Quản lý Promotion Rules

#### US-PM-009: Tạo rule giảm giá theo phần trăm
**Là** Promotion Manager
**Tôi muốn** tạo rule giảm giá theo phần trăm
**Để** khách hàng được giảm một tỷ lệ % trên giá sản phẩm

**Tiêu chí chấp nhận:**
- Nhập tỷ lệ giảm (1-100%)
- Có thể set giá trị giảm tối đa (cap)
- Có thể áp dụng cho specific products hoặc categories
- Validate không vượt quá 100%

#### US-PM-010: Tạo rule giảm giá cố định
**Là** Promotion Manager
**Tôi muốn** tạo rule giảm giá số tiền cố định
**Để** khách hàng được giảm một số tiền cụ thể

**Tiêu chí chấp nhận:**
- Nhập số tiền giảm
- Có thể set điều kiện đơn hàng tối thiểu
- Support multiple currencies
- Validate số tiền giảm không âm

#### US-PM-011: Tạo rule mua X tặng Y
**Là** Promotion Manager
**Tôi muốn** tạo rule mua X sản phẩm tặng Y sản phẩm
**Để** khách hàng được tặng sản phẩm khi mua đủ số lượng

**Tiêu chí chấp nhận:**
- Chọn sản phẩm X (sản phẩm cần mua)
- Nhập số lượng X cần mua
- Chọn sản phẩm Y (sản phẩm được tặng)
- Nhập số lượng Y được tặng
- Có thể set giới hạn số lần áp dụng

#### US-PM-012: Tạo rule miễn phí vận chuyển
**Là** Promotion Manager
**Tôi muốn** tạo rule miễn phí vận chuyển
**Để** khách hàng không phải trả phí ship

**Tiêu chí chấp nhận:**
- Có thể set điều kiện đơn hàng tối thiểu
- Có thể giới hạn theo khu vực giao hàng
- Có thể giới hạn theo phương thức vận chuyển

---

### 4. Quản lý Điều kiện Áp dụng

#### US-PM-013: Set điều kiện theo sản phẩm
**Là** Promotion Manager
**Tôi muốn** set điều kiện promotion áp dụng cho sản phẩm cụ thể
**Để** promotion chỉ áp dụng cho các sản phẩm được chọn

**Tiêu chí chấp nhận:**
- Chọn specific SKUs
- Chọn theo category
- Chọn theo brand
- Có thể exclude sản phẩm
- Lấy thông tin product từ Product Service

#### US-PM-014: Set điều kiện theo khách hàng
**Là** Promotion Manager
**Tôi muốn** set điều kiện promotion áp dụng cho nhóm khách hàng
**Để** promotion chỉ áp dụng cho khách hàng đủ điều kiện

**Tiêu chí chấp nhận:**
- Chọn customer segment (VIP, New, Loyal)
- Chọn customer tier
- Có thể set first-time purchase only
- Lấy thông tin customer từ Customer Service

#### US-PM-015: Set điều kiện theo đơn hàng
**Là** Promotion Manager
**Tôi muốn** set điều kiện theo giá trị đơn hàng
**Để** promotion chỉ áp dụng khi đơn hàng đạt điều kiện

**Tiêu chí chấp nhận:**
- Set giá trị đơn hàng tối thiểu
- Set số lượng sản phẩm tối thiểu
- Set giới hạn số lần sử dụng per customer
- Set giới hạn tổng số lần sử dụng

#### US-PM-016: Set điều kiện theo channel
**Là** Promotion Manager
**Tôi muốn** set điều kiện promotion theo kênh bán hàng
**Để** promotion chỉ áp dụng trên các kênh được chọn

**Tiêu chí chấp nhận:**
- Chọn channels (Web, Mobile App, POS, Marketplace)
- Có thể chọn multiple channels
- Có thể exclude channels

---

### 5. Quản lý Stacking Rules

#### US-PM-017: Cấu hình stacking rules
**Là** Promotion Manager
**Tôi muốn** cấu hình cách các promotion được cộng dồn
**Để** hệ thống biết cách xử lý khi có nhiều promotion áp dụng

**Tiêu chí chấp nhận:**
- Set promotion có thể stack với promotion khác không
- Set priority khi có conflict
- Set exclusive groups (promotions trong cùng group không stack)
- Set maximum discount cap khi stack

---

### 6. Phân quyền và Audit

#### US-PM-018: Phân quyền quản lý promotion theo region
**Là** Admin
**Tôi muốn** phân quyền user quản lý promotion theo region
**Để** mỗi user chỉ quản lý promotion trong phạm vi của mình

**Tiêu chí chấp nhận:**
- Assign user vào specific regions
- User chỉ thấy và quản lý promotion của region mình
- Super admin có thể quản lý tất cả regions
- Integrate với Spring Security

#### US-PM-019: Xem audit log
**Là** Admin
**Tôi muốn** xem lịch sử thay đổi của promotion
**Để** tôi có thể track ai đã làm gì với promotion

**Tiêu chí chấp nhận:**
- Log tất cả actions (create, update, delete, activate, disable)
- Log user thực hiện action
- Log timestamp
- Log giá trị trước và sau khi thay đổi
- Có thể filter và search audit log

