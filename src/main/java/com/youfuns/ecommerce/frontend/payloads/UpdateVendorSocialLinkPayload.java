package com.youfuns.ecommerce.frontend.payloads;

public record UpdateVendorSocialLinkPayload(
        String platform,
        String url
) {}
