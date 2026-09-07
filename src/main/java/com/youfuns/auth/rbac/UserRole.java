package com.youfuns.auth.rbac;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public record UserRole<P extends Enum<P> & Permission>(String name, Set<P> permissions) {
    public UserRole(String name, Set<P> permissions) {
        this.name = name;
        this.permissions = Collections.unmodifiableSet(permissions);
    }

    public boolean hasPermission(P permission) {
        return hasPermission(permissions, permission);
    }

    @SuppressWarnings("unchecked")
    private boolean hasPermission(Collection<P> permissions, P permission) {
        if (permissions.contains(permission)) return true;
        for (P perm : permissions) {
            if (perm.implications().contains(permission)) return true;
            if (hasPermission((Collection<P>) perm.implications(), perm)) return true;
        }
        return false;
    }
}
