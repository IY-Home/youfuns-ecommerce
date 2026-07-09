package com.youfuns.ecommerce.frontend.payloads;

import java.util.UUID;

public record UpdateProductStockPayload(
        UUID productId,
        int stockQuantity
) {}