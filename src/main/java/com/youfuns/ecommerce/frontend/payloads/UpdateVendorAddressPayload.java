package com.youfuns.ecommerce.frontend.payloads;

public record UpdateVendorAddressPayload(
        String address,
        String city,
        String state,
        String countryCode,
        String postalCode
) {}
