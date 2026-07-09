package com.youfuns.ecommerce.frontend.payloads;

import java.util.UUID;

public record SetProductThumbnailPayload(
        UUID productId,
        String thumbnailUrl
) {}