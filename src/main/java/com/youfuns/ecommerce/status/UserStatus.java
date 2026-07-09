package com.youfuns.ecommerce.status;

import java.util.UUID;

public class UserStatus extends Status<UserStatus.Status> {
    public enum Status {
        ACTIVE, SUSPENDED
    }

    public UserStatus(UUID parentId, Status status) {
        super(parentId, status);
    }
}
