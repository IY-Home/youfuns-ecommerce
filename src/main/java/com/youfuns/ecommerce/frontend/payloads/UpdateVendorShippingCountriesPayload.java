package com.youfuns.ecommerce.frontend.payloads;

import java.util.List;

public record UpdateVendorShippingCountriesPayload(
        List<String> shippingCountries
) {}
