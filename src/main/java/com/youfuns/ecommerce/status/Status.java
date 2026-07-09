package com.youfuns.ecommerce.status;

import java.util.UUID;

public abstract class Status<T extends Enum<T>> {
    private final UUID parentId;
    private T status;

    public Status(UUID parentId, T status) {
        if (parentId == null) {
            throw new IllegalArgumentException("parentId cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }
        this.parentId = parentId;
        this.status = status;
    }

    public void updateStatus(T newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus cannot be null");
        }
        if (this.status == newStatus) {
            return; // No-op
        }
        this.status = newStatus;
    }

    public T getStatus() {
        return status;
    }

    public UUID getParentId() {
        return parentId;
    }
}
