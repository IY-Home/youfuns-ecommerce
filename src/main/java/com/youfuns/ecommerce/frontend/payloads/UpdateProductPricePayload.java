package com.youfuns.ecommerce.frontend.payloads;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductPricePayload(
        UUID productId,
        BigDecimal price
) {}