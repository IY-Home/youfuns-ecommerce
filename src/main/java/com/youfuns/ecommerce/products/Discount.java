package com.youfuns.ecommerce.products;

import com.youfuns.ecommerce.frontend.payloads.AddDiscountPayload;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Discount {
    private final UUID id;
    private boolean isForSpecificProduct;
    private UUID productId;
    private String discountName;
    private int discountPercent;
    private final LocalDateTime createdAt;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    public Discount(AddDiscountPayload addDiscountPayload) {
        this.id =  UUID.randomUUID();
        this.isForSpecificProduct = addDiscountPayload.isForSpecificProduct();
        if (isForSpecificProduct) {
            if (addDiscountPayload.productId() == null) {
                throw new IllegalArgumentException("productId is null but isForSpecificProduct is true");
            }
            this.productId = addDiscountPayload.productId();
        }
        this.productId = null;
        this.discountName = addDiscountPayload.discountName();
        this.discountPercent = addDiscountPayload.discountPercent();
        this.validFrom = addDiscountPayload.validFrom();
        this.validTo = addDiscountPayload.validTo();
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }
    public boolean isForSpecificProduct() {
        return isForSpecificProduct;
    }
    public UUID getProductId() {
        return productId;
    }
    public String getDiscountName() {
        return discountName;
    }
    public int getDiscountPercent() {
        return discountPercent;
    }
    public double getDiscountMultiplier() {
        return (double) discountPercent / 100;
    }
    public LocalDateTime getValidFrom() {
        return validFrom;
    }
    public LocalDateTime getValidTo() {
        return validTo;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
