package org.example.promotion.common;

/**
 * Application-wide constants to avoid magic values scattered across the
 * codebase.
 */
public final class AppConstants {

    private AppConstants() {
        // utility class — no instantiation
    }

    // =================== Pagination ===================

    /** Default maximum page size for list APIs to prevent abuse. */
    public static final int MAX_PAGE_SIZE = 100;

    /** Default page size when not specified. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    // =================== Coupon ===================

    /**
     * Default TTL (minutes) for a coupon reservation before it is automatically
     * released.
     */
    public static final int RESERVATION_TTL_MINUTES = 15;

    /** Default max uses for a newly created coupon if not specified. */
    public static final int DEFAULT_COUPON_MAX_USES = 1;

    // =================== Audit ===================

    /**
     * Fallback actor name used in audit logs when no authenticated user is present.
     */
    public static final String SYSTEM_ACTOR = "system";
}
