package com.youfuns.ecommerce.frontend.payloads;

import java.util.UUID;

public record RemoveProductImagePayload(
        UUID productId,
        String imageUrl
) {}