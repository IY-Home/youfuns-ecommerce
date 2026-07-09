package com.youfuns.ecommerce.frontend.payloads;

import java.util.UUID;

public record AddProductImagePayload(
        UUID productId,
        String imageUrl
) {}