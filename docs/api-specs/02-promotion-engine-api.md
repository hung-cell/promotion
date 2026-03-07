# API Specification - Promotion Engine Service

## Base URL
```
/api/v1/engine
```

## Authentication
Tất cả API yêu cầu Service-to-Service authentication:
```
X-API-Key: <service_api_key>
Authorization: Bearer <jwt_token> (optional, nếu có context user)
```

---

## 1. Promotion Calculation APIs

### POST /api/v1/engine/calculate

**Description:** Tính toán promotions cho giỏ hàng hoặc đơn hàng

**Request Headers:**
| Header | Type | Required | Description |
|--------|------|----------|-------------|
| X-API-Key | String | Yes | Service API Key |
| Authorization | String | No | Bearer JWT token (if has user context) |
| Content-Type | String | Yes | application/json |

**Request Body:**
```json
{
  "cartId": "cart_12345",
  "customerId": "cust_001",
  "channel": "WEB",
  "items": [
    {
      "sku": "SKU001",
      "productId": "prod_001",
      "productName": "Áo Thun Nam",
      "categoryId": "cat_001",
      "brandId": "brand_001",
      "quantity": 2,
      "unitPrice": 250000,
      "subtotal": 500000
    },
    {
      "sku": "SKU002",
      "productId": "prod_002",
      "productName": "Quần Jean",
      "categoryId": "cat_002",
      "brandId": "brand_001",
      "quantity": 1,
      "unitPrice": 450000,
      "subtotal": 450000
    }
  ],
  "subtotal": 950000,
  "shippingFee": 30000,
  "customerSegment": "VIP",
  "customerTier": "GOLD",
  "appliedCouponCodes": ["SUMMER2024"],
  "shippingRegion": "HCM",
  "shippingMethod": "STANDARD"
}
```

**Request Body Parameters:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| cartId | String | No | Cart ID (for tracking) |
| customerId | String | Yes | Customer ID |
| channel | Enum | Yes | WEB, MOBILE_APP, POS, MARKETPLACE |
| items | Array | Yes | Danh sách items trong cart |
| items[].sku | String | Yes | SKU sản phẩm |
| items[].productId | String | Yes | Product ID |
| items[].productName | String | No | Tên sản phẩm |
| items[].categoryId | String | No | Category ID |
| items[].brandId | String | No | Brand ID |
| items[].quantity | Integer | Yes | Số lượng |
| items[].unitPrice | Decimal | Yes | Đơn giá |
| items[].subtotal | Decimal | Yes | Thành tiền (quantity × unitPrice) |
| subtotal | Decimal | Yes | Tổng tiền hàng |
| shippingFee | Decimal | No | Phí vận chuyển |
| customerSegment | String | No | Phân khúc khách hàng |
| customerTier | String | No | Tier khách hàng |
| appliedCouponCodes | Array | No | Danh sách mã coupon áp dụng |
| shippingRegion | String | No | Khu vực giao hàng |
| shippingMethod | String | No | Phương thức giao hàng |

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "calculationId": "calc_789456",
    "timestamp": "2024-06-15T10:30:00",
    "applicablePromotions": [
      {
        "promotionId": "promo_123",
        "promotionName": "Summer Sale 2024",
        "type": "PERCENTAGE",
        "discountAmount": 95000,
        "appliedToItems": [
          {
            "sku": "SKU001",
            "originalPrice": 500000,
            "discountAmount": 50000,
            "finalPrice": 450000
          },
          {
            "sku": "SKU002",
            "originalPrice": 450000,
            "discountAmount": 45000,
            "finalPrice": 405000
          }
        ],
        "priority": 10,
        "couponCode": null
      },
      {
        "promotionId": "promo_456",
        "promotionName": "Free Shipping HCM",
        "type": "FREE_SHIPPING",
        "discountAmount": 30000,
        "appliedToItems": [],
        "priority": 5,
        "couponCode": null
      }
    ],
    "summary": {
      "originalSubtotal": 950000,
      "totalDiscount": 125000,
      "finalSubtotal": 825000,
      "originalShippingFee": 30000,
      "shippingDiscount": 30000,
      "finalShippingFee": 0,
      "totalAmount": 825000,
      "savedAmount": 155000,
      "savedPercentage": 15.82
    },
    "giftItems": [],
    "messages": [
      "Bạn đã tiết kiệm được 155,000đ với các ưu đãi!"
    ]
  }
}
```

**Response Error (400 Bad Request):**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request data",
    "details": [
      {
        "field": "items",
        "message": "Items cannot be empty"
      }
    ]
  }
}
```

---

### POST /api/v1/engine/preview

**Description:** Preview promotions cho cart (không reserve quota, chỉ xem trước)

**Request Body:** Giống `/calculate`

**Response:** Giống `/calculate` nhưng không tạo reservation

**Use case:** Cart Service gọi API này để hiển thị promotion real-time khi user đang shopping

---

### POST /api/v1/engine/calculate-product

**Description:** Tính toán promotion cho một hoặc nhiều sản phẩm (dùng cho Product Listing)

**Request Body:**
```json
{
  "products": [
    {
      "sku": "SKU001",
      "productId": "prod_001",
      "categoryId": "cat_001",
      "brandId": "brand_001",
      "unitPrice": 250000
    },
    {
      "sku": "SKU002",
      "productId": "prod_002",
      "categoryId": "cat_002",
      "brandId": "brand_001",
      "unitPrice": 450000
    }
  ],
  "channel": "WEB",
  "customerId": "cust_001",
  "customerSegment": "VIP"
}
```

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "sku": "SKU001",
      "productId": "prod_001",
      "originalPrice": 250000,
      "hasPromotion": true,
      "bestPromotion": {
        "promotionId": "promo_123",
        "promotionName": "Summer Sale",
        "discountAmount": 25000,
        "discountPercent": 10,
        "finalPrice": 225000
      },
      "badges": ["SALE", "HOT_DEAL"]
    },
    {
      "sku": "SKU002",
      "productId": "prod_002",
      "originalPrice": 450000,
      "hasPromotion": false,
      "bestPromotion": null,
      "badges": []
    }
  ]
}
```

---

## 2. Validation APIs

### POST /api/v1/engine/validate

**Description:** Validate promotions có thể áp dụng cho order hay không

**Request Body:**
```json
{
  "orderId": "order_12345",
  "customerId": "cust_001",
  "channel": "WEB",
  "items": [
    {
      "sku": "SKU001",
      "productId": "prod_001",
      "categoryId": "cat_001",
      "quantity": 2,
      "unitPrice": 250000,
      "subtotal": 500000
    }
  ],
  "subtotal": 500000,
  "promotionIds": ["promo_123", "promo_456"],
  "couponCodes": ["SUMMER2024"]
}
```

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "valid": true,
    "validPromotions": [
      {
        "promotionId": "promo_123",
        "valid": true,
        "reason": null
      },
      {
        "promotionId": "promo_456",
        "valid": true,
        "reason": null
      }
    ],
    "validCoupons": [
      {
        "couponCode": "SUMMER2024",
        "valid": true,
        "promotionId": "promo_789",
        "reason": null
      }
    ],
    "conflicts": [],
    "warnings": []
  }
}
```

**Response when has invalid promotions:**
```json
{
  "success": true,
  "data": {
    "valid": false,
    "validPromotions": [
      {
        "promotionId": "promo_123",
        "valid": false,
        "reason": "PROMOTION_EXPIRED",
        "message": "Chương trình khuyến mãi đã hết hạn"
      },
      {
        "promotionId": "promo_456",
        "valid": false,
        "reason": "USAGE_LIMIT_EXCEEDED",
        "message": "Đã hết lượt sử dụng"
      }
    ],
    "validCoupons": [],
    "conflicts": [
      {
        "type": "EXCLUSIVE_GROUP",
        "message": "promo_123 và promo_456 không thể dùng chung",
        "conflictPromotionIds": ["promo_123", "promo_456"]
      }
    ],
    "warnings": [
      "Đơn hàng chưa đạt giá trị tối thiểu 500,000đ"
    ]
  }
}
```

**Validation Reasons:**
- `PROMOTION_EXPIRED` - Promotion đã hết hạn
- `PROMOTION_NOT_STARTED` - Promotion chưa bắt đầu
- `PROMOTION_INACTIVE` - Promotion không active
- `USAGE_LIMIT_EXCEEDED` - Đã hết lượt sử dụng
- `CUSTOMER_USAGE_LIMIT_EXCEEDED` - Khách hàng đã sử dụng hết lượt
- `MIN_ORDER_VALUE_NOT_MET` - Chưa đạt giá trị đơn hàng tối thiểu
- `PRODUCT_NOT_APPLICABLE` - Sản phẩm không áp dụng
- `CUSTOMER_NOT_ELIGIBLE` - Khách hàng không đủ điều kiện
- `CHANNEL_NOT_APPLICABLE` - Kênh không áp dụng
- `EXCLUSIVE_CONFLICT` - Xung đột với promotion khác

---

### POST /api/v1/engine/validate-coupon

**Description:** Validate mã coupon

**Request Body:**
```json
{
  "couponCode": "SUMMER2024",
  "customerId": "cust_001",
  "channel": "WEB"
}
```

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "valid": true,
    "coupon": {
      "code": "SUMMER2024",
      "promotionId": "promo_123",
      "promotionName": "Summer Sale 2024",
      "discountType": "PERCENTAGE",
      "discountValue": 20,
      "maxDiscountAmount": 500000,
      "minOrderValue": 200000,
      "expiryDate": "2024-08-31T23:59:59",
      "usageRemaining": 850
    }
  }
}
```

**Response when invalid:**
```json
{
  "success": true,
  "data": {
    "valid": false,
    "reason": "COUPON_NOT_FOUND",
    "message": "Mã giảm giá không tồn tại hoặc đã hết hạn"
  }
}
```

---

## 3. Reservation APIs

### POST /api/v1/engine/reserve

**Description:** Reserve promotion quota khi customer checkout

**Request Body:**
```json
{
  "orderId": "order_12345",
  "customerId": "cust_001",
  "promotionIds": ["promo_123", "promo_456"],
  "couponCodes": ["SUMMER2024"],
  "calculationId": "calc_789456",
  "ttlSeconds": 600
}
```

**Request Parameters:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| orderId | String | Yes | Order ID để tracking |
| customerId | String | Yes | Customer ID |
| promotionIds | Array | Yes | Danh sách promotion IDs cần reserve |
| couponCodes | Array | No | Danh sách coupon codes |
| calculationId | String | No | Calculation ID từ API /calculate trước đó |
| ttlSeconds | Integer | No | Time to live (default 600s = 10 phút) |

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "reservationId": "rsv_567890",
    "orderId": "order_12345",
    "reservedPromotions": [
      {
        "promotionId": "promo_123",
        "reserved": true,
        "reservedAt": "2024-06-15T10:30:00"
      },
      {
        "promotionId": "promo_456",
        "reserved": true,
        "reservedAt": "2024-06-15T10:30:00"
      }
    ],
    "expiresAt": "2024-06-15T10:40:00",
    "ttlSeconds": 600
  },
  "message": "Promotion quota reserved successfully"
}
```

**Response Error (409 Conflict):**
```json
{
  "success": false,
  "error": {
    "code": "QUOTA_EXCEEDED",
    "message": "Promotion usage quota exceeded",
    "details": [
      {
        "promotionId": "promo_123",
        "message": "Chương trình khuyến mãi đã hết lượt sử dụng"
      }
    ]
  }
}
```

---

### POST /api/v1/engine/commit

**Description:** Commit promotion usage khi order placed thành công

**Request Body:**
```json
{
  "reservationId": "rsv_567890",
  "orderId": "order_12345",
  "customerId": "cust_001",
  "finalAmount": 825000,
  "discountAmount": 155000
}
```

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "reservationId": "rsv_567890",
    "orderId": "order_12345",
    "committedPromotions": [
      {
        "promotionId": "promo_123",
        "usageRecordId": "usage_111",
        "committedAt": "2024-06-15T10:35:00"
      },
      {
        "promotionId": "promo_456",
        "usageRecordId": "usage_222",
        "committedAt": "2024-06-15T10:35:00"
      }
    ]
  },
  "message": "Promotion usage committed successfully"
}
```

**Response Error (404 Not Found):**
```json
{
  "success": false,
  "error": {
    "code": "RESERVATION_NOT_FOUND",
    "message": "Reservation not found or expired"
  }
}
```

---

### POST /api/v1/engine/release

**Description:** Release reservation khi order bị cancel

**Request Body:**
```json
{
  "reservationId": "rsv_567890",
  "orderId": "order_12345",
  "reason": "Customer cancelled checkout"
}
```

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "reservationId": "rsv_567890",
    "orderId": "order_12345",
    "releasedPromotions": [
      {
        "promotionId": "promo_123",
        "releasedAt": "2024-06-15T10:38:00"
      },
      {
        "promotionId": "promo_456",
        "releasedAt": "2024-06-15T10:38:00"
      }
    ]
  },
  "message": "Reservation released successfully"
}
```

---

## 4. Sync & Cache Management APIs

### POST /api/v1/engine/sync/promotion

**Description:** Sync một promotion từ Promotion Management Service (Internal API)

**Request Body:**
```json
{
  "action": "CREATE",
  "promotion": {
    "id": "promo_123",
    "name": "Summer Sale 2024",
    "type": "PERCENTAGE",
    "status": "ACTIVE",
    "discountValue": 20,
    "maxDiscountAmount": 500000,
    "minOrderValue": 200000,
    "startDate": "2024-06-01T00:00:00",
    "endDate": "2024-08-31T23:59:59",
    "usageLimit": 1000,
    "usageLimitPerCustomer": 1,
    "rules": [...],
    "conditions": [...]
  }
}
```

**Actions:**
- `CREATE` - Tạo mới promotion trong cache
- `UPDATE` - Cập nhật promotion
- `ACTIVATE` - Kích hoạt promotion
- `DISABLE` - Vô hiệu hóa promotion
- `DELETE` - Xóa promotion khỏi cache

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "promotionId": "promo_123",
    "action": "CREATE",
    "cached": true,
    "syncedAt": "2024-06-15T10:00:00"
  },
  "message": "Promotion synced successfully"
}
```

---

### POST /api/v1/engine/cache/invalidate

**Description:** Invalidate cache của một hoặc nhiều promotions

**Request Body:**
```json
{
  "promotionIds": ["promo_123", "promo_456"],
  "invalidateAll": false
}
```

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "invalidatedCount": 2,
    "invalidatedPromotionIds": ["promo_123", "promo_456"]
  },
  "message": "Cache invalidated successfully"
}
```

---

### POST /api/v1/engine/cache/warmup

**Description:** Warm up cache (load tất cả active promotions vào cache)

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "loadedPromotions": 150,
    "loadedRules": 320,
    "loadedConditions": 280,
    "startTime": "2024-06-15T10:00:00",
    "endTime": "2024-06-15T10:00:05",
    "durationMs": 5000
  },
  "message": "Cache warmed up successfully"
}
```

---

## 5. Health & Monitoring APIs

### GET /api/v1/engine/health

**Description:** Health check endpoint

**Response Success (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP",
      "details": {
        "connected": true,
        "ping": "1ms"
      }
    },
    "database": {
      "status": "UP",
      "details": {
        "connected": true
      }
    },
    "cache": {
      "status": "UP",
      "details": {
        "promotionsInCache": 150,
        "cacheHitRate": 95.5
      }
    }
  }
}
```

---

### GET /api/v1/engine/metrics

**Description:** Get performance metrics

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "performance": {
      "avgResponseTime": 45,
      "p50ResponseTime": 35,
      "p95ResponseTime": 80,
      "p99ResponseTime": 120,
      "requestsPerSecond": 850
    },
    "cache": {
      "hitRate": 95.5,
      "missRate": 4.5,
      "totalRequests": 100000,
      "cacheHits": 95500,
      "cacheMisses": 4500
    },
    "promotions": {
      "activePromotions": 150,
      "totalCalculations": 50000,
      "totalReservations": 5000,
      "totalCommits": 4500,
      "totalReleases": 500
    },
    "errors": {
      "errorRate": 0.1,
      "totalErrors": 50,
      "errorsByType": {
        "VALIDATION_ERROR": 30,
        "QUOTA_EXCEEDED": 15,
        "INTERNAL_ERROR": 5
      }
    }
  }
}
```

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| VALIDATION_ERROR | 400 | Dữ liệu không hợp lệ |
| PROMOTION_NOT_FOUND | 404 | Không tìm thấy promotion |
| RESERVATION_NOT_FOUND | 404 | Không tìm thấy reservation |
| QUOTA_EXCEEDED | 409 | Đã hết quota sử dụng |
| CACHE_ERROR | 500 | Lỗi Redis cache |
| INTERNAL_ERROR | 500 | Lỗi hệ thống |
| SERVICE_UNAVAILABLE | 503 | Service tạm thời không khả dụng |

---

## Rate Limiting

- **Calculate/Preview APIs**: 1000 requests/minute per API key
- **Validate APIs**: 500 requests/minute per API key
- **Reservation APIs**: 200 requests/minute per API key
- **Sync APIs**: 100 requests/minute (internal only)

## Performance SLAs

- **Calculate API**: < 100ms (p95), < 150ms (p99)
- **Preview API**: < 50ms (p95), < 80ms (p99)
- **Validate API**: < 80ms (p95), < 120ms (p99)
- **Reserve API**: < 100ms (p95), < 150ms (p99)
- **Commit API**: < 200ms (p95), < 300ms (p99)

## Caching Strategy

- **Active Promotions**: TTL 5 minutes
- **Promotion Rules**: TTL 5 minutes
- **Usage Quotas**: Real-time (Redis counters)
- **Calculation Results**: TTL 30 seconds (optional)
