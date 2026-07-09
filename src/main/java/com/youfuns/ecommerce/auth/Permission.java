package com.youfuns.ecommerce.auth;

public enum Permission {
    // SELF = only able to be done on your account

    CREATE_USER(false), // Register SELF as user

    VIEW_SELF_USER(true),
    DELETE_SELF_USER(true), // Delete SELF account
    UPDATE_SELF_USER(true), // Update SELF account

    VIEW_ANY_VENDORS(false), // View ANY vendor's profile (public)
    VIEW_ANY_PRODUCTS(false), // View ANY product (public)
    REVIEW_ANY_PRODUCTS(false), // Leave review on ANY product (public)
    PURCHASE_ANY_PRODUCT(false), // Purchase ANY product (public)

    BECOME_VENDOR(true), // Register SELF account to be vendor
    VIEW_SELF_ROLES(true), // View the roles SELF account has

    CREATE_PRODUCT(true), // Create a product under SELF vendor account
    MANAGE_SELF_PRODUCTS(true), // Update SELF products

    MANAGE_SELF_LISTS(true), // Manage SELF's wishlist and cart
    VIEW_SELF_ORDERS(true), // View SELF's placed orders
    VIEW_CUSTOMER_ORDERS(true), // View all the orders for SELF's products as vendor
    UPDATE_ORDER_STATUS(true), // Update the status of orders for SELF's products as vendor
    CANCEL_SELF_ORDERS(true), // Cancel orders placed by SELF
    CANCEL_CUSTOMER_ORDERS(true), // Cancel orders for SELF's products as vendor

    // Admin
    DISABLE_ANY_USER(false), // Suspend or unsuspend ANY user's account
    APPROVE_VENDORS(false), // Approve ANY vendor applications
    DISABLE_ANY_VENDOR(false), // Revoke ANY vendor's application
    VIEW_ANY_USERS(false), // View ANY user's profile
    MANAGE_ANY_USERS(false), // Manage ANY user's profile and delete them
    MANAGE_ANY_VENDORS(false), // Manage ANY vendor's profile
    MANAGE_ANY_ADMINS(false), // Manage ANY admin
    MANAGE_ANY_ROLES(false), // Manage ANY user's role and add new admins
    MANAGE_ANY_PRODUCTS(false), // Manage ANY product
    MANAGE_ANY_LISTS (false), // Manage ANY wishlist or cart
    MANAGE_ANY_ORDERS(false), // Manage ANY order
    VIEW_LOGS(false); // View ANY system log

    private final boolean hasSpecificUser;

    Permission(boolean hasSpecificUser) {
        this.hasSpecificUser = hasSpecificUser;
    }

    public boolean needsSpecificUser() {
        return hasSpecificUser;
    }
}
