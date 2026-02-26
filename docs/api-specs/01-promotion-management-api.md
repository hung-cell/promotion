# API Specification - Promotion Management Service

## Base URL
```
/api/v1/promotions
```

## Authentication
Tất cả API yêu cầu JWT token trong header:
```
Authorization: Bearer <token>
```

---

## US-PM-001: Tạo chương trình khuyến mãi mới

### POST /api/v1/promotions

**Description:** Tạo một chương trình khuyến mãi mới

**Request Headers:**
| Header | Type | Required | Description |
|--------|------|----------|-------------|
| Authorization | String | Yes | Bearer JWT token |
| Content-Type | String | Yes | application/json |

**Request Body:**
```json
{
  "name": "Summer Sale 2024",
  "description": "Giảm giá mùa hè lên đến 50%",
  "type": "PERCENTAGE",
  "discountValue": 20.00,
  "maxDiscountAmount": 500000,
  "minOrderValue": 200000,
  "startDate": "2024-06-01T00:00:00",
  "endDate": "2024-08-31T23:59:59",
  "usageLimit": 1000,
  "usageLimitPerCustomer": 1,
  "status": "DRAFT"
}
```

**Request Body Parameters:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Tên promotion (max 255 chars) |
| description | String | No | Mô tả promotion |
| type | Enum | Yes | PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y, FREE_SHIPPING |
| discountValue | Decimal | Yes | Giá trị giảm (% hoặc số tiền) |
| maxDiscountAmount | Decimal | No | Giảm tối đa (cho type PERCENTAGE) |
| minOrderValue | Decimal | No | Giá trị đơn hàng tối thiểu |
| startDate | DateTime | Yes | Thời gian bắt đầu (ISO 8601) |
| endDate | DateTime | Yes | Thời gian kết thúc (ISO 8601) |
| usageLimit | Integer | No | Giới hạn tổng số lần sử dụng |
| usageLimitPerCustomer | Integer | No | Giới hạn số lần sử dụng per customer |
| status | Enum | No | DRAFT (default), ACTIVE |

**Response Success (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "promo_123456",
    "name": "Summer Sale 2024",
    "description": "Giảm giá mùa hè lên đến 50%",
    "type": "PERCENTAGE",
    "discountValue": 20.00,
    "maxDiscountAmount": 500000,
    "minOrderValue": 200000,
    "startDate": "2024-06-01T00:00:00",
    "endDate": "2024-08-31T23:59:59",
    "usageLimit": 1000,
    "usageLimitPerCustomer": 1,
    "usageCount": 0,
    "status": "DRAFT",
    "createdAt": "2024-05-15T10:30:00",
    "createdBy": "user_001",
    "updatedAt": "2024-05-15T10:30:00"
  },
  "message": "Promotion created successfully"
}
```

**Response Error (400 Bad Request):**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "name",
        "message": "Name is required"
      },
      {
        "field": "discountValue",
        "message": "Discount value must be greater than 0"
      }
    ]
  }
}
```

**Response Error (401 Unauthorized):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired token"
  }
}
```

**Response Error (403 Forbidden):**
```json
{
  "success": false,
  "error": {
    "code": "FORBIDDEN",
    "message": "You don't have permission to create promotion"
  }
}
```

---

## US-PM-002: Xem danh sách chương trình khuyến mãi

### GET /api/v1/promotions

**Description:** Lấy danh sách promotions với phân trang và filter

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | No | 0 | Số trang (bắt đầu từ 0) |
| size | Integer | No | 20 | Số items per page (max 100) |
| sort | String | No | createdAt,desc | Field và direction sort |
| status | Enum | No | - | Filter theo status |
| type | Enum | No | - | Filter theo type |
| startDateFrom | DateTime | No | - | Filter startDate >= |
| startDateTo | DateTime | No | - | Filter startDate <= |
| search | String | No | - | Search theo name |

**Example Request:**
```
GET /api/v1/promotions?page=0&size=10&status=ACTIVE&sort=startDate,desc
```

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "promo_123456",
        "name": "Summer Sale 2024",
        "type": "PERCENTAGE",
        "discountValue": 20.00,
        "startDate": "2024-06-01T00:00:00",
        "endDate": "2024-08-31T23:59:59",
        "status": "ACTIVE",
        "usageCount": 150,
        "usageLimit": 1000
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 50,
      "totalPages": 5,
      "first": true,
      "last": false
    }
  }
}
```

---

## US-PM-003: Xem chi tiết chương trình khuyến mãi

### GET /api/v1/promotions/{id}

**Description:** Lấy chi tiết một promotion

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | String | Yes | Promotion ID |

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "promo_123456",
    "name": "Summer Sale 2024",
    "description": "Giảm giá mùa hè lên đến 50%",
    "type": "PERCENTAGE",
    "discountValue": 20.00,
    "maxDiscountAmount": 500000,
    "minOrderValue": 200000,
    "startDate": "2024-06-01T00:00:00",
    "endDate": "2024-08-31T23:59:59",
    "usageLimit": 1000,
    "usageLimitPerCustomer": 1,
    "usageCount": 150,
    "status": "ACTIVE",
    "rules": [
      {
        "id": "rule_001",
        "type": "PRODUCT_CONDITION",
        "condition": {
          "categoryIds": ["cat_001", "cat_002"],
          "excludeSkus": ["SKU001"]
        }
      }
    ],
    "createdAt": "2024-05-15T10:30:00",
    "createdBy": "user_001",
    "updatedAt": "2024-05-20T14:00:00",
    "updatedBy": "user_002"
  }
}
```

**Response Error (404 Not Found):**
```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "Promotion not found with id: promo_123456"
  }
}
```

---

## US-PM-004: Cập nhật chương trình khuyến mãi

### PUT /api/v1/promotions/{id}

**Description:** Cập nhật thông tin promotion

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | String | Yes | Promotion ID |

**Request Body:**
```json
{
  "name": "Summer Sale 2024 - Extended",
  "description": "Giảm giá mùa hè - Gia hạn thêm",
  "discountValue": 25.00,
  "endDate": "2024-09-30T23:59:59",
  "usageLimit": 2000
}
```

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "promo_123456",
    "name": "Summer Sale 2024 - Extended",
    "discountValue": 25.00,
    "endDate": "2024-09-30T23:59:59",
    "usageLimit": 2000,
    "status": "ACTIVE",
    "updatedAt": "2024-06-15T09:00:00",
    "updatedBy": "user_001"
  },
  "message": "Promotion updated successfully"
}
```

**Response Error (400 Bad Request):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_STATUS",
    "message": "Cannot update promotion with status EXPIRED"
  }
}
```

---

## US-PM-005: Xóa chương trình khuyến mãi

### DELETE /api/v1/promotions/{id}

**Description:** Xóa promotion (soft delete)

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | String | Yes | Promotion ID |

**Response Success (200 OK):**
```json
{
  "success": true,
  "message": "Promotion deleted successfully"
}
```

**Response Error (400 Bad Request):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_STATUS",
    "message": "Can only delete promotion with status DRAFT"
  }
}
```

---

## US-PM-006: Kích hoạt chương trình khuyến mãi

### POST /api/v1/promotions/{id}/activate

**Description:** Kích hoạt promotion (DRAFT -> ACTIVE)

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | String | Yes | Promotion ID |

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "promo_123456",
    "status": "ACTIVE",
    "activatedAt": "2024-06-01T00:00:00",
    "activatedBy": "user_001"
  },
  "message": "Promotion activated successfully"
}
```

**Response Error (400 Bad Request):**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Cannot activate promotion",
    "details": [
      "Promotion must have at least one rule",
      "Start date must be in the future"
    ]
  }
}
```

---

## US-PM-007: Vô hiệu hóa chương trình khuyến mãi

### POST /api/v1/promotions/{id}/disable

**Description:** Vô hiệu hóa promotion (ACTIVE -> DISABLED)

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | String | Yes | Promotion ID |

**Request Body:**
```json
{
  "reason": "Budget exceeded"
}
```

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "promo_123456",
    "status": "DISABLED",
    "disabledAt": "2024-07-15T10:00:00",
    "disabledBy": "user_001",
    "disableReason": "Budget exceeded"
  },
  "message": "Promotion disabled successfully"
}
```

---

## US-PM-009 - US-PM-012: Quản lý Promotion Rules

### POST /api/v1/promotions/{id}/rules

**Description:** Thêm rule cho promotion

**Request Body (Percentage Rule):**
```json
{
  "type": "PERCENTAGE_DISCOUNT",
  "discountPercent": 20,
  "maxDiscountAmount": 500000
}
```

**Request Body (Fixed Amount Rule):**
```json
{
  "type": "FIXED_DISCOUNT",
  "discountAmount": 100000,
  "minOrderValue": 500000
}
```

**Request Body (Buy X Get Y Rule):**
```json
{
  "type": "BUY_X_GET_Y",
  "buyProductId": "prod_001",
  "buyQuantity": 2,
  "getProductId": "prod_002",
  "getQuantity": 1,
  "maxApplications": 3
}
```

**Request Body (Free Shipping Rule):**
```json
{
  "type": "FREE_SHIPPING",
  "minOrderValue": 300000,
  "applicableRegions": ["HN", "HCM"],
  "applicableShippingMethods": ["STANDARD", "EXPRESS"]
}
```

**Response Success (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "rule_001",
    "promotionId": "promo_123456",
    "type": "PERCENTAGE_DISCOUNT",
    "discountPercent": 20,
    "maxDiscountAmount": 500000,
    "createdAt": "2024-05-15T11:00:00"
  },
  "message": "Rule added successfully"
}
```

### GET /api/v1/promotions/{id}/rules

**Description:** Lấy danh sách rules của promotion

### DELETE /api/v1/promotions/{id}/rules/{ruleId}

**Description:** Xóa rule khỏi promotion

---

## US-PM-013 - US-PM-016: Quản lý Điều kiện Áp dụng

### POST /api/v1/promotions/{id}/conditions

**Description:** Thêm điều kiện áp dụng cho promotion

**Request Body (Product Condition):**
```json
{
  "type": "PRODUCT",
  "includeSkus": ["SKU001", "SKU002"],
  "includeCategoryIds": ["cat_001"],
  "includeBrandIds": ["brand_001"],
  "excludeSkus": ["SKU003"]
}
```

**Request Body (Customer Condition):**
```json
{
  "type": "CUSTOMER",
  "customerSegments": ["VIP", "LOYAL"],
  "customerTiers": ["GOLD", "PLATINUM"],
  "firstPurchaseOnly": false
}
```

**Request Body (Order Condition):**
```json
{
  "type": "ORDER",
  "minOrderValue": 500000,
  "minQuantity": 3,
  "maxUsagePerCustomer": 2,
  "maxTotalUsage": 1000
}
```

**Request Body (Channel Condition):**
```json
{
  "type": "CHANNEL",
  "includeChannels": ["WEB", "MOBILE_APP"],
  "excludeChannels": ["POS"]
}
```

**Response Success (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "cond_001",
    "promotionId": "promo_123456",
    "type": "PRODUCT",
    "includeCategoryIds": ["cat_001"],
    "createdAt": "2024-05-15T11:30:00"
  },
  "message": "Condition added successfully"
}
```

### GET /api/v1/promotions/{id}/conditions

**Description:** Lấy danh sách conditions của promotion

### DELETE /api/v1/promotions/{id}/conditions/{conditionId}

**Description:** Xóa condition khỏi promotion

---

## US-PM-017: Cấu hình Stacking Rules

### PUT /api/v1/promotions/{id}/stacking-config

**Description:** Cấu hình stacking rules cho promotion

**Request Body:**
```json
{
  "stackable": true,
  "priority": 10,
  "exclusiveGroupId": "group_summer_sale",
  "maxStackDiscount": 1000000
}
```

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "promotionId": "promo_123456",
    "stackable": true,
    "priority": 10,
    "exclusiveGroupId": "group_summer_sale",
    "maxStackDiscount": 1000000
  },
  "message": "Stacking config updated successfully"
}
```

---

## US-PM-019: Xem Audit Log

### GET /api/v1/promotions/{id}/audit-logs

**Description:** Lấy lịch sử thay đổi của promotion

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | No | 0 | Số trang |
| size | Integer | No | 20 | Số items per page |
| action | Enum | No | - | Filter theo action |
| fromDate | DateTime | No | - | Filter từ ngày |
| toDate | DateTime | No | - | Filter đến ngày |

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "log_001",
        "promotionId": "promo_123456",
        "action": "UPDATE",
        "userId": "user_001",
        "userName": "John Doe",
        "timestamp": "2024-06-15T09:00:00",
        "changes": {
          "discountValue": {
            "oldValue": 20,
            "newValue": 25
          },
          "endDate": {
            "oldValue": "2024-08-31T23:59:59",
            "newValue": "2024-09-30T23:59:59"
          }
        }
      }
    ],
    "pagination": {
      "page": 0,
      "size": 20,
      "totalElements": 15,
      "totalPages": 1
    }
  }
}
```

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| VALIDATION_ERROR | 400 | Dữ liệu không hợp lệ |
| INVALID_STATUS | 400 | Trạng thái không cho phép thao tác |
| UNAUTHORIZED | 401 | Chưa xác thực |
| FORBIDDEN | 403 | Không có quyền |
| NOT_FOUND | 404 | Không tìm thấy resource |
| CONFLICT | 409 | Xung đột dữ liệu |
| INTERNAL_ERROR | 500 | Lỗi server |

---

## Enums

### PromotionType
- `PERCENTAGE` - Giảm theo phần trăm
- `FIXED_AMOUNT` - Giảm số tiền cố định
- `BUY_X_GET_Y` - Mua X tặng Y
- `FREE_SHIPPING` - Miễn phí vận chuyển

### PromotionStatus
- `DRAFT` - Nháp
- `ACTIVE` - Đang hoạt động
- `DISABLED` - Đã vô hiệu
- `EXPIRED` - Đã hết hạn

### Channel
- `WEB` - Website
- `MOBILE_APP` - Ứng dụng di động
- `POS` - Point of Sale
- `MARKETPLACE` - Sàn thương mại điện tử

