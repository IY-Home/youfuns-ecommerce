package com.youfuns.ecommerce.frontend.payloads;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddDiscountPayload(boolean isForSpecificProduct, UUID productId, String discountName, int discountPercent, LocalDateTime validFrom, LocalDateTime validTo) {
}
