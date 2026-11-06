package com.lims.util;

/**
 * System Constants for Roles, Statuses, and Session Keys.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // Session Attribute Keys
    public static final String SESSION_USER = "LOGIN_USER";
    public static final String SESSION_USER_ROLES = "USER_ROLES";

    // System Roles
    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_MANAGER = "Manager";
    public static final String ROLE_WAREHOUSE = "Warehouse Staff";
    public static final String ROLE_SALES = "Sales Staff";
    public static final String ROLE_PURCHASING = "Purchasing Staff";

    // Order & Request Status
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_APPROVED = "Approved";
    public static final String STATUS_REJECTED = "Rejected";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_CANCELLED = "Cancelled";

    // Inventory & Hardware Status
    public static final String INVENTORY_IN_STOCK = "In Stock";
    public static final String INVENTORY_EXPORTED = "Exported";
    public static final String INVENTORY_DAMAGED = "Damaged";
    public static final String INVENTORY_MISSING = "Missing";
    public static final String INVENTORY_RESERVED = "Reserved";

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 10;
}
