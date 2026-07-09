package com.youfuns.ecommerce.orders;

import com.youfuns.ecommerce.products.Product;
import com.youfuns.ecommerce.status.OrderStatus;
import com.youfuns.paramtypes.Address;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Order {
    private final UUID id;
    private final UUID userId;
    private final List<OrderItem> items;
    private final Address shippingAddress;
    private final Address billingAddress;
    private final BigDecimal totalAmount;
    private final String paymentMethod;
    private final LocalDateTime createdAt;
    private OrderStatus.Status status;
    private LocalDateTime updatedAt;

    public record OrderItem(UUID productId, String productName, int quantity, BigDecimal priceAtTime) {}

    public Order(UUID userId, List<OrderItem> items, Address shippingAddress, Address billingAddress,
                 BigDecimal totalAmount, String paymentMethod) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.items = List.copyOf(items);
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = OrderStatus.Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public List<OrderItem> getItems() { return items; }
    public Address getShippingAddress() { return shippingAddress; }
    public Address getBillingAddress() { return billingAddress; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public OrderStatus.Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void updateStatus(OrderStatus.Status newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
}