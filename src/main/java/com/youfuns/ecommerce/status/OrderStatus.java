package com.youfuns.ecommerce.status;

import java.util.UUID;

public class OrderStatus extends Status<OrderStatus.Status> {
    public enum Status {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    }

    public OrderStatus(UUID orderId, Status status) {
        super(orderId, status);
    }
}
