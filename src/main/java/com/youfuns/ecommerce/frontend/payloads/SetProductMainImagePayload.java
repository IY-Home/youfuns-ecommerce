package com.youfuns.ecommerce.frontend.payloads;

import java.util.UUID;

public record SetProductMainImagePayload(
        UUID productId,
        String mainImageUrl
) {}
