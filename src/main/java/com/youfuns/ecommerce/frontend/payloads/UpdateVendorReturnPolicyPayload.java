package com.youfuns.ecommerce.frontend.payloads;

public record UpdateVendorReturnPolicyPayload(
        boolean acceptReturns,
        Integer returnWindowDays,
        String returnPolicy
) {}
