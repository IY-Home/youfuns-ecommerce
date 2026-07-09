package com.youfuns.ecommerce.auth;

import java.util.EnumSet;
import java.util.Set;

public enum UserRole {
    GUEST(Set.of(
            Permission.VIEW_ANY_VENDORS,
            Permission.VIEW_ANY_PRODUCTS,
            Permission.CREATE_USER
    )),
    CUSTOMER(Set.of(
            Permission.VIEW_SELF_USER,
            Permission.DELETE_SELF_USER,
            Permission.UPDATE_SELF_USER,
            Permission.VIEW_ANY_VENDORS,
            Permission.VIEW_ANY_PRODUCTS,
            Permission.REVIEW_ANY_PRODUCTS,
            Permission.PURCHASE_ANY_PRODUCT,
            Permission.BECOME_VENDOR,
            Permission.VIEW_SELF_ROLES,
            Permission.VIEW_SELF_ORDERS,
            Permission.CANCEL_SELF_ORDERS
    )),

    VENDOR(Set.of(
            Permission.VIEW_SELF_USER,
            Permission.DELETE_SELF_USER,
            Permission.UPDATE_SELF_USER,
            Permission.VIEW_SELF_ROLES,
            Permission.CREATE_PRODUCT,
            Permission.MANAGE_SELF_PRODUCTS,
            Permission.VIEW_CUSTOMER_ORDERS,
            Permission.UPDATE_ORDER_STATUS,
            Permission.CANCEL_CUSTOMER_ORDERS
    )),

    ADMIN(Set.of(
            Permission.VIEW_SELF_USER,
            Permission.CREATE_USER,
            Permission.DELETE_SELF_USER,
            Permission.UPDATE_SELF_USER,
            Permission.DISABLE_ANY_USER,
            Permission.APPROVE_VENDORS,
            Permission.DISABLE_ANY_VENDOR,
            Permission.VIEW_ANY_PRODUCTS,
            Permission.VIEW_ANY_VENDORS,
            Permission.MANAGE_ANY_PRODUCTS,
            Permission.MANAGE_ANY_ORDERS,
            Permission.VIEW_LOGS
            // Permission.VIEW_ANY_USERS, Permission.MANAGE_ANY_USERS, Permission.MANAGE_ANY_VENDORS, Permission.MANAGE_ANY_ADMINS, and Permission.MANAGE_ANY_ROLES are intentionally left out to avoid admins abusing their role
    )),

    MANAGER(EnumSet.allOf(Permission.class));  // Manager has ALL permissions

    private final Set<Permission> permissions;

    UserRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}

