package com.youfuns.auth.rbac;

import java.util.Set;

public interface Permission {
    String name();
    boolean needsSpecificUser();
    Set<? extends Permission> implications(); // can be empty
}