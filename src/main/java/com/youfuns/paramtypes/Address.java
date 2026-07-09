package com.youfuns.paramtypes;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.exceptions.IllegalFieldException;

public record Address(
        String countryCode,
        String postalCode,
        String state,
        String city,
        String addressLine
) {
    public Address {
        LoggerManager.quickLog(this, "Validating Address...");

        try {
            // Validate country code
            if (countryCode != null && !countryCode.isBlank()) {
                LoggerManager.quickLog(this, "  Validating country code...");
                Country.fromCode(countryCode);
                LoggerManager.quickLog(this, "  Country code valid");
            } else {
                LoggerManager.quickLog(this, "  Country code is null or blank, skipping validation");
            }
        } catch (IllegalFieldException e) {
            LoggerManager.quickLog(this, "  Country validation FAILED: " + e.getMessage());
            throw new IllegalFieldException("Invalid country code: " + countryCode, ParamType.ADDRESS);
        }

        try {
            // Validate postal code
            if (postalCode != null && !postalCode.isBlank()) {
                LoggerManager.quickLog(this, "  Validating postal code...");
                new PostalCode(postalCode);
                LoggerManager.quickLog(this, "  Postal code valid");
            } else {
                LoggerManager.quickLog(this, "  Postal code is null or blank, skipping validation");
            }
        } catch (IllegalFieldException e) {
            LoggerManager.quickLog(this, "  Postal validation FAILED: " + e.getMessage());
            throw new IllegalFieldException("Invalid postal code: " + postalCode, ParamType.ADDRESS);
        }

        LoggerManager.quickLog(this, "Address validation PASSED");
    }
}