package com.youfuns.ecommerce.frontend.payloads;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UpdateProductPayload(
        UUID productId,  // Added productId to payload
        String name,
        String description,
        String shortDescription,
        String brand,
        String subcategory,
        String currency,
        BigDecimal price,
        BigDecimal compareAtPrice,
        BigDecimal costPrice,
        Integer stockQuantity,
        Integer lowStockThreshold,
        Boolean trackInventory,
        Double weight,
        String weightUnit,
        String dimensionUnit,
        Boolean requiresShipping,
        Boolean requiresSpecialHandling,
        String customsDescription,
        String warrantyInformation,
        Boolean hasReturns,
        Integer returnDays,
        Boolean onSale,
        Map<String, String> customAttributes,
        Integer minimumOrderQuantity,
        Integer maximumOrderQuantity,
        Boolean ageRestricted,
        Integer minimumAge
) {}