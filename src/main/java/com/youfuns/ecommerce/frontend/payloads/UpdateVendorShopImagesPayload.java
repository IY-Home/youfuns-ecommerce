package com.youfuns.ecommerce.frontend.payloads;

import java.util.List;

public record UpdateVendorShopImagesPayload(
        List<String> shopImageUrls
) {}
