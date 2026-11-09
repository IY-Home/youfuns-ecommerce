package com.youfuns.auth.login;

import com.youfuns.auth.rbac.DefaultPermissions;
import com.youfuns.auth.rbac.Permission;

import java.util.function.Supplier;

public record AuthContext<T extends Enum<T> & Permission>(
        DefaultPermissions<T> permissions,
        Supplier<UserLoginConfig> defaultLoginConfig,
        Supplier<HashingService> hasherConfig
) {}