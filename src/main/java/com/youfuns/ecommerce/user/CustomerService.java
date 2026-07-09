package com.youfuns.ecommerce.user;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.Permission;
import com.youfuns.ecommerce.auth.PermissionChecker;
import com.youfuns.ecommerce.auth.RoleToken;
import com.youfuns.ecommerce.frontend.payloads.RegisterCustomerPayload;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.paramtypes.Address;
import com.youfuns.paramtypes.Country;
import com.youfuns.paramtypes.Currency;
import com.youfuns.paramtypes.Language;
import com.youfuns.paramtypes.UuidFormat;

import java.util.UUID;

public class CustomerService {
    private final UUID userId;

    // Customer info
    private Address shippingAddress;
    private Address billingAddress;
    private Country country;
    private Language language;
    private Currency currency;

    // Preferences (delegated to UserPreferences)
    private final UserPreferences userPreferences;

    public CustomerService(UUID userId, RegisterCustomerPayload payload) {
        this.userId = userId;
        this.userPreferences = new UserPreferences(userId, payload);

        // Set customer info
        this.shippingAddress = payload.shippingAddress();
        this.billingAddress = payload.billingAddress() != null ? payload.billingAddress() : payload.shippingAddress();
        this.country = Country.fromCode(payload.countryCode());
        this.language = Language.fromCode(payload.languageCode());
        this.currency = Currency.fromCode(payload.currencyCode());

        LoggerManager.quickLog(this, "Created CustomerService for user: " + UuidFormat.shortenUUID(userId));
    }

    // ============= GETTERS (Requires VIEW_SELF_USER) =============

    public Address getShippingAddress(RoleToken rt) {
        checkPermission(rt);
        return shippingAddress;
    }

    public Address getBillingAddress(RoleToken rt) {
        checkPermission(rt);
        return billingAddress;
    }

    public Country getCountry(RoleToken rt) {
        checkPermission(rt);
        return country;
    }

    public Language getLanguage(RoleToken rt) {
        checkPermission(rt);
        return language;
    }

    public Currency getCurrency(RoleToken rt) {
        checkPermission(rt);
        return currency;
    }

    public UserPreferences getUserPreferences(RoleToken rt) {
        checkPermission(rt);
        return userPreferences;
    }

    // ============= GETTERS - ADMIN READ (Requires VIEW_ANY_USERS) =============

    public Address getShippingAddressAdmin(RoleToken rt) {
        checkAdminPermission(rt);
        return shippingAddress;
    }

    public Address getBillingAddressAdmin(RoleToken rt) {
        checkAdminPermission(rt);
        return billingAddress;
    }

    public Country getCountryAdmin(RoleToken rt) {
        checkAdminPermission(rt);
        return country;
    }

    public Language getLanguageAdmin(RoleToken rt) {
        checkAdminPermission(rt);
        return language;
    }

    public Currency getCurrencyAdmin(RoleToken rt) {
        checkAdminPermission(rt);
        return currency;
    }

    public UserPreferences getUserPreferencesAdmin(RoleToken rt) {
        checkAdminPermission(rt);
        return userPreferences;
    }

    // ============= SETTERS (Requires UPDATE_SELF_USER) =============

    public ResultReturn setShippingAddress(RoleToken rt, Address shippingAddress) {
        checkPermission(rt);
        this.shippingAddress = shippingAddress;
        LoggerManager.quickLog(this, "Shipping address updated for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shipping address updated.");
    }

    public ResultReturn setBillingAddress(RoleToken rt, Address billingAddress) {
        checkPermission(rt);
        this.billingAddress = billingAddress;
        LoggerManager.quickLog(this, "Billing address updated for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Billing address updated.");
    }

    public ResultReturn setCountry(RoleToken rt, Country country) {
        checkPermission(rt);
        this.country = country;
        LoggerManager.quickLog(this, "Country updated for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Country updated.");
    }

    public ResultReturn setLanguage(RoleToken rt, Language language) {
        checkPermission(rt);
        this.language = language;
        LoggerManager.quickLog(this, "Language updated for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Language updated.");
    }

    public ResultReturn setCurrency(RoleToken rt, Currency currency) {
        checkPermission(rt);
        this.currency = currency;
        LoggerManager.quickLog(this, "Currency updated for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Currency updated.");
    }

    // ============= SETTERS - ADMIN WRITE (Requires MANAGE_ANY_USERS) =============

    public ResultReturn setShippingAddressAdmin(RoleToken rt, Address shippingAddress) {
        checkAdminPermission(rt);
        this.shippingAddress = shippingAddress;
        LoggerManager.quickLog(this, "Shipping address updated by admin for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shipping address updated by admin.");
    }

    public ResultReturn setBillingAddressAdmin(RoleToken rt, Address billingAddress) {
        checkAdminPermission(rt);
        this.billingAddress = billingAddress;
        LoggerManager.quickLog(this, "Billing address updated by admin for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Billing address updated by admin.");
    }

    public ResultReturn setCountryAdmin(RoleToken rt, Country country) {
        checkAdminPermission(rt);
        this.country = country;
        LoggerManager.quickLog(this, "Country updated by admin for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Country updated by admin.");
    }

    // ============= FULL PROFILE READ =============

    public CustomerFullProfile toFullProfile(RoleToken rt) {
        LoggerManager.quickLog(this, "Getting customer profile...");
        return new CustomerFullProfile(
                userId,
                shippingAddress,
                billingAddress,
                country,
                language,
                currency,
                userPreferences.toFullPreferences(rt)
        );
    }

    public CustomerFullProfile toFullProfileAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Getting customer profile...");
        checkAdminPermission(rt);
        return new CustomerFullProfile(
                userId,
                shippingAddress,
                billingAddress,
                country,
                language,
                currency,
                userPreferences.toFullPreferencesAdmin(rt)
        );
    }

    // ============= PUBLIC PROFILE (Limited info) =============

    public CustomerPublicProfile toPublicProfile() {
        return new CustomerPublicProfile(
                userId,
                country,
                language
        );
    }

    // ============= HELPER METHODS =============

    private void checkPermission(RoleToken rt) {
        PermissionChecker.checkPermissionWithUser(rt, this.userId, Permission.VIEW_SELF_USER);
    }

    private void checkAdminPermission(RoleToken rt) {
        PermissionChecker.checkPermission(rt, Permission.VIEW_ANY_USERS);
    }

    // ============= RECORDS =============

    public record CustomerFullProfile(
            UUID userId,
            Address shippingAddress,
            Address billingAddress,
            Country country,
            Language language,
            Currency currency,
            UserPreferences.UserPreferencesFull preferences
    ) {}

    public record CustomerPublicProfile(
            UUID userId,
            Country country,
            Language language
    ) {}
}