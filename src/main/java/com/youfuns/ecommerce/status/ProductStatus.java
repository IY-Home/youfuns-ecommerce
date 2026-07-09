package com.youfuns.ecommerce.status;

import java.util.UUID;

public class ProductStatus extends Status<ProductStatus.Status> {
    public enum Status {
        DRAFT, ANNOUNCED, ACTIVE, INACTIVE, OUT_OF_STOCK, REMOVED
    }

    public ProductStatus(UUID productId, Status status) {
        super(productId, status);
    }
}
