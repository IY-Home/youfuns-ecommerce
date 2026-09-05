package com.youfuns.auth;

import java.util.List;

public interface Permission {
    String name();
    boolean needsSpecificUser();
    List<? extends Permission> implications(); // can be empty
}