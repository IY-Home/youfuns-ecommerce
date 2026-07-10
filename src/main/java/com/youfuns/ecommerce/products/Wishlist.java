package com.youfuns.ecommerce.products;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.Permission;
import com.youfuns.ecommerce.auth.PermissionChecker;
import com.youfuns.ecommerce.auth.RoleToken;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.paramtypes.UuidFormat;

import java.util.UUID;

public class Wishlist extends ProductList {
    private final UUID userId;

    public Wishlist(UUID userId) {
        super();
        this.userId = userId;
        LoggerManager.quickLog(this, "Created Wishlist for user: " + UuidFormat.shortenUUID(userId));
    }

    @Override
    protected void checkPermission(RoleToken rt) {
        // No need to check as it is already checked at addEntry
        /*
        try {
            PermissionChecker.checkPermissionWithUser(rt, this.userId, Permission.MANAGE_SELF_LISTS);
        } catch (AccessDeniedException e) {
            // Try admin permission as fallback
            PermissionChecker.checkPermission(rt, Permission.MANAGE_ANY_LISTS);
        } */
    }

    public UUID getUserId() {
        return userId;
    }
}