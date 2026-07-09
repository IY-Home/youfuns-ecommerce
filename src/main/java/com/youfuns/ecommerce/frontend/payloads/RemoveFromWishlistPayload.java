package com.youfuns.ecommerce.frontend.payloads;

import java.util.UUID;

public record RemoveFromWishlistPayload(
        UUID productId
) {}