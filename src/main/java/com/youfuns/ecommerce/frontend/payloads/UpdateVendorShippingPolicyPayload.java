package com.youfuns.ecommerce.frontend.payloads;

public record UpdateVendorShippingPolicyPayload(
        String shippingPolicy,
        Double freeShippingThreshold,
        Double domesticShippingRate,
        Double internationalShippingRate
) {}
