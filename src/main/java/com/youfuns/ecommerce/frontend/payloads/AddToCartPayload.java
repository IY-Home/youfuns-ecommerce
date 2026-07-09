package com.youfuns.ecommerce.frontend.payloads;

import java.util.UUID;

public record AddToCartPayload(
        UUID productId,
        int quantity
) {}