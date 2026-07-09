package com.youfuns.ecommerce.frontend.payloads;

import com.youfuns.paramtypes.Category;

import java.util.List;
import java.util.Map;

public record RegisterVendorPayload(
        // ===== BUSINESS INFORMATION =====
        String shopName,
        String shopDescription,
        Category category,
        String shopLogoUrl,
        List<String> shopImageUrls,
        String shopBannerUrl,
        String shopTagline,

        // ===== CONTACT & LOCATION =====
        String businessEmail,
        String businessPhone,
        String websiteUrl,
        String address,
        String city,
        String state,
        String countryCode,
        String postalCode,

        // ===== SOCIAL MEDIA =====
        Map<String, String> socialLinks,

        // ===== BUSINESS DETAILS =====
        String taxId,
        String businessRegistrationNumber,
        String businessType,
        String legalName,
        String yearEstablished,

        // ===== STORE SETTINGS =====
        String storeTheme,
        String storeLanguage,
        String storeCurrency,
        Boolean isStoreVisible,
        Boolean acceptReturns,
        Integer returnWindowDays,
        String returnPolicy,

        // ===== SHIPPING =====
        String shippingPolicy,
        Double freeShippingThreshold,
        Double domesticShippingRate,
        Double internationalShippingRate,
        List<String> shippingCountries
) {}