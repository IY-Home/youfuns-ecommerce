package com.youfuns.ecommerce.frontend.payloads;

import com.youfuns.paramtypes.Address;

public record RegisterCustomerPayload(
        // Customer info
        Address shippingAddress,
        Address billingAddress,
        String countryCode,
        String languageCode,
        String currencyCode,

        // Preferences (will be passed to UserPreferences)
        String preferredContactMethod,
        String dateFormat,
        String timeFormat,
        String numberFormat,
        boolean allowEmailNotifications,
        boolean allowSmsNotifications,
        boolean allowPushNotifications,
        boolean allowMarketingEmails,
        boolean allowOrderUpdates,
        boolean allowPromotionalOffers,
        boolean allowNewsletter,
        boolean profileVisible,
        boolean showOnlineStatus,
        boolean allowDataCollection,
        boolean shareAnalytics
) {
    public RegisterCustomerPayload {
        // Default values for null fields
        if (preferredContactMethod == null) preferredContactMethod = "email";
        if (dateFormat == null) dateFormat = "MM/dd/yyyy";
        if (timeFormat == null) timeFormat = "hh:mm a";
        if (numberFormat == null) numberFormat = "1,234.56";
    }
}