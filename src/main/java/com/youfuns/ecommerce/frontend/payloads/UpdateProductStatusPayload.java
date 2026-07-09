package com.youfuns.ecommerce.frontend.payloads;

import java.util.UUID;

public record UpdateProductStatusPayload(
        UUID productId,
        String status
) {}