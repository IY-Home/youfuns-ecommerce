package com.youfuns.ecommerce.frontend.payloads;

import com.youfuns.paramtypes.Currency;
import com.youfuns.paramtypes.Sku;
import com.youfuns.paramtypes.Subcategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductCreatePayload(
        // ===== IDENTIFIERS =====
        UUID vendorId,
        String sku,

        // ===== BASIC INFORMATION =====
        String name,
        String description,
        String shortDescription,
        String brand,

        // ===== CATEGORIZATION =====
        String subcategory,           // Will be converted to Subcategory enum

        // ===== PRICING =====
        String currency,              // Will be converted to Currency enum
        BigDecimal price,
        BigDecimal compareAtPrice,
        BigDecimal costPrice,

        // ===== INVENTORY =====
        Integer stockQuantity,        // Can be null if not tracking
        Integer lowStockThreshold,
        Boolean trackInventory,

        // ===== VARIANTS =====
        Map<String, List<String>> variants,  // Will be converted to ProductVariant
        Boolean hasVariants,

        // ===== PHYSICAL ATTRIBUTES =====
        Double weight,
        Double length,
        Double width,
        Double height,
        String weightUnit,
        String dimensionUnit,

        // ===== MEDIA =====
        List<String> imageUrls,
        String mainImageUrl,
        String thumbnailUrl,

        // ===== STATUS =====
        String initialStatus,          // Will be converted to ProductStatus.Status

        // ===== SHIPPING =====
        String shippingClass,
        Boolean requiresShipping,
        Boolean requiresSpecialHandling,
        String customsDescription,

        // ===== WARRANTY & RETURNS =====
        String warrantyInformation,
        Boolean hasReturns,
        Integer returnDays,

        // ===== DISCOUNTS =====
        Boolean onSale,

        // ===== CUSTOM ATTRIBUTES =====
        Map<String, String> customAttributes,

        // ===== RESTRICTIONS =====
        Integer minimumOrderQuantity,
        Integer maximumOrderQuantity,
        Boolean ageRestricted,
        Integer minimumAge
) {}