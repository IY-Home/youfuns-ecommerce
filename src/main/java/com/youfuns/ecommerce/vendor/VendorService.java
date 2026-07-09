package com.youfuns.ecommerce.vendor;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.Permission;
import com.youfuns.ecommerce.auth.PermissionChecker;
import com.youfuns.ecommerce.auth.RoleToken;
import com.youfuns.ecommerce.frontend.payloads.RegisterVendorPayload;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.paramtypes.Category;
import com.youfuns.paramtypes.UuidFormat;
import com.youfuns.utils.ListUtils;
import com.youfuns.utils.MapUtils;
import com.youfuns.utils.SimpleLogger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VendorService {
    private final UUID userId;

    // ============= STATUS =============
    private boolean isActive;
    private LocalDateTime activatedAt;
    private LocalDateTime deactivatedAt;

    // ============= BUSINESS INFORMATION =============
    private String shopName;
    private String shopDescription;
    private Category category;
    private String shopLogoUrl;
    private List<String> shopImageUrls;
    private String shopBannerUrl;
    private String shopTagline;

    // ============= CONTACT & LOCATION =============
    private String businessEmail;
    private String businessPhone;
    private String websiteUrl;
    private String address;
    private String city;
    private String state;
    private String countryCode;
    private String postalCode;

    // ============= SOCIAL MEDIA =============
    private Map<String, String> socialLinks;  // platform -> url

    // ============= BUSINESS DETAILS =============
    private String taxId;
    private String businessRegistrationNumber;
    private String businessType;  // Individual, LLC, Corporation, etc.
    private String legalName;
    private String yearEstablished;

    // ============= STORE SETTINGS =============
    private String storeTheme;
    private String storeLanguage;
    private String storeCurrency;
    private boolean isStoreVisible;
    private boolean acceptReturns;
    private int returnWindowDays;
    private String returnPolicy;

    // ============= SHIPPING =============
    private String shippingPolicy;
    private double freeShippingThreshold;
    private double domesticShippingRate;
    private double internationalShippingRate;
    private List<String> shippingCountries;

    // ============= STATISTICS =============
    private int totalProducts;
    private int totalOrders;
    private int totalRevenue;  // in cents
    private double averageRating;
    private int reviewCount;
    private int totalFollowers;

    // ============= TIMESTAMPS =============
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VendorService(UUID userId) {
        this.userId = userId;
        this.isActive = false;
        this.activatedAt = null;
        this.deactivatedAt = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.totalProducts = 0;
        this.totalOrders = 0;
        this.totalRevenue = 0;
        this.averageRating = 0.0;
        this.reviewCount = 0;
        this.totalFollowers = 0;
        this.isStoreVisible = true;
        this.acceptReturns = true;
        this.returnWindowDays = 30;
        this.freeShippingThreshold = 50.0;
        this.domesticShippingRate = 5.0;
        this.internationalShippingRate = 20.0;
        this.shippingCountries = List.of("US", "CA", "GB", "AU");
        this.socialLinks = Map.of();
        this.shopImageUrls = List.of();

        LoggerManager.quickLog(this, "Created VendorService for user: " + UuidFormat.shortenUUID(userId));
    }

    // ============= INITIALIZATION =============

    public ResultReturn initVendor(RegisterVendorPayload payload) {
        LoggerManager.quickLog(this, "Initializing vendor for user: " + UuidFormat.shortenUUID(userId));

        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor payload is null.");
        }

        // Business Information
        this.shopName = payload.shopName();
        this.shopDescription = payload.shopDescription();
        this.category = payload.category();
        this.shopLogoUrl = payload.shopLogoUrl();
        this.shopImageUrls = payload.shopImageUrls() != null ? payload.shopImageUrls() : List.of();
        this.shopBannerUrl = payload.shopBannerUrl();
        this.shopTagline = payload.shopTagline();

        // Contact & Location
        this.businessEmail = payload.businessEmail();
        this.businessPhone = payload.businessPhone();
        this.websiteUrl = payload.websiteUrl();
        this.address = payload.address();
        this.city = payload.city();
        this.state = payload.state();
        this.countryCode = payload.countryCode();
        this.postalCode = payload.postalCode();

        // Social Media
        this.socialLinks = payload.socialLinks() != null ? payload.socialLinks() : Map.of();

        // Business Details
        this.taxId = payload.taxId();
        this.businessRegistrationNumber = payload.businessRegistrationNumber();
        this.businessType = payload.businessType();
        this.legalName = payload.legalName();
        this.yearEstablished = payload.yearEstablished();

        // Store Settings
        this.storeTheme = payload.storeTheme() != null ? payload.storeTheme() : "default";
        this.storeLanguage = payload.storeLanguage() != null ? payload.storeLanguage() : "en";
        this.storeCurrency = payload.storeCurrency() != null ? payload.storeCurrency() : "USD";
        this.isStoreVisible = payload.isStoreVisible() != null ? payload.isStoreVisible() : true;
        this.acceptReturns = payload.acceptReturns() != null ? payload.acceptReturns() : true;
        this.returnWindowDays = payload.returnWindowDays() != null ? payload.returnWindowDays() : 30;
        this.returnPolicy = payload.returnPolicy();

        // Shipping
        this.shippingPolicy = payload.shippingPolicy();
        this.freeShippingThreshold = payload.freeShippingThreshold() != null ? payload.freeShippingThreshold() : 50.0;
        this.domesticShippingRate = payload.domesticShippingRate() != null ? payload.domesticShippingRate() : 5.0;
        this.internationalShippingRate = payload.internationalShippingRate() != null ? payload.internationalShippingRate() : 20.0;
        this.shippingCountries = payload.shippingCountries() != null ? payload.shippingCountries() : List.of("US", "CA", "GB", "AU");

        this.updatedAt = LocalDateTime.now();

        LoggerManager.quickLog(this, "Vendor initialized for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Vendor initialized successfully.");
    }

    // ============= ACTIVATION / DEACTIVATION =============

    public ResultReturn activateVendor(RoleToken rt) {
        LoggerManager.quickLog(this, "Activating vendor for user: " + UuidFormat.shortenUUID(userId));

        try {
            PermissionChecker.checkPermission(rt, Permission.APPROVE_VENDORS);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions to approve vendors.");
        }

        if (isActive) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor is already active.");
        }

        this.isActive = true;
        this.activatedAt = LocalDateTime.now();
        this.deactivatedAt = null;
        this.updatedAt = LocalDateTime.now();

        LoggerManager.quickLog(this, "Vendor activated for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Vendor activated successfully.");
    }

    public ResultReturn deactivateVendor(RoleToken rt) {
        LoggerManager.quickLog(this, "Deactivating vendor for user: " + UuidFormat.shortenUUID(userId));

        try {
            PermissionChecker.checkPermission(rt, Permission.DISABLE_ANY_VENDOR);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions to disable vendors.");
        }

        if (!isActive) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor is already inactive.");
        }

        this.isActive = false;
        this.deactivatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        LoggerManager.quickLog(this, "Vendor deactivated for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Vendor deactivated successfully.");
    }

    // ============= PRIVATE HELPER =============

    private void checkVendorActive() {
        if (!this.isActive) {
            LoggerManager.quickLog(this, "Attempted to read non-active vendor (" +
                    UuidFormat.shortenUUID(this.userId) + ") as vendor", SimpleLogger.Level.ERROR);
            throw new AccessDeniedException("Vendor is not active");
        }
    }

    // ============= BUSINESS INFORMATION SETTERS =============

    public ResultReturn setShopName(RoleToken rt, String shopName) {
        checkVendorPermission(rt);
        if (shopName == null || shopName.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Shop name cannot be empty.");
        }
        this.shopName = shopName;
        this.updatedAt = LocalDateTime.now();
        LoggerManager.quickLog(this, "Shop name updated for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shop name updated.");
    }

    public ResultReturn setShopDescription(RoleToken rt, String shopDescription) {
        checkVendorPermission(rt);
        this.shopDescription = shopDescription;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shop description updated.");
    }

    public ResultReturn setCategory(RoleToken rt, Category category) {
        checkVendorPermission(rt);
        if (category == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Category cannot be null.");
        }
        this.category = category;
        this.updatedAt = LocalDateTime.now();
        LoggerManager.quickLog(this, "Category updated for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Category updated.");
    }

    public ResultReturn setShopLogoUrl(RoleToken rt, String shopLogoUrl) {
        checkVendorPermission(rt);
        if (shopLogoUrl == null || shopLogoUrl.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Logo URL cannot be empty.");
        }
        this.shopLogoUrl = shopLogoUrl;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shop logo updated.");
    }

    public ResultReturn addShopImage(RoleToken rt, String imageUrl) {
        checkVendorPermission(rt);
        if (imageUrl == null || imageUrl.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Image URL cannot be empty.");
        }
        this.shopImageUrls = ListUtils.addToList(this.shopImageUrls, imageUrl);
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shop image added.");
    }

    public ResultReturn removeShopImage(RoleToken rt, String imageUrl) {
        checkVendorPermission(rt);
        this.shopImageUrls = ListUtils.removeFromList(this.shopImageUrls, imageUrl);
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shop image removed.");
    }

    public ResultReturn setShopBannerUrl(RoleToken rt, String shopBannerUrl) {
        checkVendorPermission(rt);
        this.shopBannerUrl = shopBannerUrl;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shop banner updated.");
    }

    public ResultReturn setShopTagline(RoleToken rt, String shopTagline) {
        checkVendorPermission(rt);
        this.shopTagline = shopTagline;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shop tagline updated.");
    }

    // ============= CONTACT SETTERS =============

    public ResultReturn setBusinessEmail(RoleToken rt, String businessEmail) {
        checkVendorPermission(rt);
        if (businessEmail == null || businessEmail.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Business email cannot be empty.");
        }
        this.businessEmail = businessEmail;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Business email updated.");
    }

    public ResultReturn setBusinessPhone(RoleToken rt, String businessPhone) {
        checkVendorPermission(rt);
        this.businessPhone = businessPhone;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Business phone updated.");
    }

    public ResultReturn setWebsiteUrl(RoleToken rt, String websiteUrl) {
        checkVendorPermission(rt);
        this.websiteUrl = websiteUrl;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Website URL updated.");
    }

    public ResultReturn setAddress(RoleToken rt, String address, String city, String state, String countryCode, String postalCode) {
        checkVendorPermission(rt);
        this.address = address;
        this.city = city;
        this.state = state;
        this.countryCode = countryCode;
        this.postalCode = postalCode;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Address updated.");
    }

    // ============= SOCIAL MEDIA SETTERS =============

    public ResultReturn addSocialLink(RoleToken rt, String platform, String url) {
        checkVendorPermission(rt);
        if (platform == null || platform.isBlank() || url == null || url.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Platform and URL cannot be empty.");
        }
        this.socialLinks = MapUtils.put(this.socialLinks, platform, url);
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Social link added.");
    }

    public ResultReturn removeSocialLink(RoleToken rt, String platform) {
        checkVendorPermission(rt);
        this.socialLinks = MapUtils.remove(this.socialLinks, platform);
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Social link removed.");
    }

    // ============= BUSINESS DETAILS SETTERS =============

    public ResultReturn setTaxId(RoleToken rt, String taxId) {
        checkVendorPermission(rt);
        this.taxId = taxId;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Tax ID updated.");
    }

    public ResultReturn setBusinessRegistrationNumber(RoleToken rt, String businessRegistrationNumber) {
        checkVendorPermission(rt);
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Business registration number updated.");
    }

    public ResultReturn setBusinessType(RoleToken rt, String businessType) {
        checkVendorPermission(rt);
        if (businessType == null || businessType.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Business type cannot be empty.");
        }
        this.businessType = businessType;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Business type updated.");
    }

    public ResultReturn setLegalName(RoleToken rt, String legalName) {
        checkVendorPermission(rt);
        this.legalName = legalName;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Legal name updated.");
    }

    public ResultReturn setYearEstablished(RoleToken rt, String yearEstablished) {
        checkVendorPermission(rt);
        this.yearEstablished = yearEstablished;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Year established updated.");
    }

    // ============= STORE SETTINGS SETTERS =============

    public ResultReturn setStoreTheme(RoleToken rt, String storeTheme) {
        checkVendorPermission(rt);
        this.storeTheme = storeTheme;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Store theme updated.");
    }

    public ResultReturn setStoreLanguage(RoleToken rt, String storeLanguage) {
        checkVendorPermission(rt);
        this.storeLanguage = storeLanguage;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Store language updated.");
    }

    public ResultReturn setStoreCurrency(RoleToken rt, String storeCurrency) {
        checkVendorPermission(rt);
        this.storeCurrency = storeCurrency;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Store currency updated.");
    }

    public ResultReturn setStoreVisible(RoleToken rt, boolean isStoreVisible) {
        checkVendorPermission(rt);
        this.isStoreVisible = isStoreVisible;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Store visibility updated.");
    }

    public ResultReturn setAcceptReturns(RoleToken rt, boolean acceptReturns) {
        checkVendorPermission(rt);
        this.acceptReturns = acceptReturns;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Return policy updated.");
    }

    public ResultReturn setReturnWindowDays(RoleToken rt, int returnWindowDays) {
        checkVendorPermission(rt);
        if (returnWindowDays < 0) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Return window cannot be negative.");
        }
        this.returnWindowDays = returnWindowDays;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Return window updated.");
    }

    public ResultReturn setReturnPolicy(RoleToken rt, String returnPolicy) {
        checkVendorPermission(rt);
        this.returnPolicy = returnPolicy;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Return policy updated.");
    }

    // ============= SHIPPING SETTERS =============

    public ResultReturn setShippingPolicy(RoleToken rt, String shippingPolicy) {
        checkVendorPermission(rt);
        this.shippingPolicy = shippingPolicy;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shipping policy updated.");
    }

    public ResultReturn setFreeShippingThreshold(RoleToken rt, double freeShippingThreshold) {
        checkVendorPermission(rt);
        if (freeShippingThreshold < 0) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Free shipping threshold cannot be negative.");
        }
        this.freeShippingThreshold = freeShippingThreshold;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Free shipping threshold updated.");
    }

    public ResultReturn setDomesticShippingRate(RoleToken rt, double domesticShippingRate) {
        checkVendorPermission(rt);
        if (domesticShippingRate < 0) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Domestic shipping rate cannot be negative.");
        }
        this.domesticShippingRate = domesticShippingRate;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Domestic shipping rate updated.");
    }

    public ResultReturn setInternationalShippingRate(RoleToken rt, double internationalShippingRate) {
        checkVendorPermission(rt);
        if (internationalShippingRate < 0) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "International shipping rate cannot be negative.");
        }
        this.internationalShippingRate = internationalShippingRate;
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "International shipping rate updated.");
    }

    public ResultReturn addShippingCountry(RoleToken rt, String countryCode) {
        checkVendorPermission(rt);
        if (countryCode == null || countryCode.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Country code cannot be empty.");
        }
        this.shippingCountries = ListUtils.addToList(this.shippingCountries, countryCode);
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shipping country added.");
    }

    public ResultReturn removeShippingCountry(RoleToken rt, String countryCode) {
        checkVendorPermission(rt);
        this.shippingCountries = ListUtils.removeFromList(this.shippingCountries, countryCode);
        this.updatedAt = LocalDateTime.now();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shipping country removed.");
    }

    // ============= STATISTICS UPDATES (Internal) =============

    public void incrementTotalProducts() {
        this.totalProducts++;
        this.updatedAt = LocalDateTime.now();
    }

    public void decrementTotalProducts() {
        if (this.totalProducts > 0) {
            this.totalProducts--;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementTotalOrders() {
        this.totalOrders++;
        this.updatedAt = LocalDateTime.now();
    }

    public void addRevenue(int amountInCents) {
        this.totalRevenue += amountInCents;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRating(double newRating) {
        if (reviewCount == 0) {
            this.averageRating = newRating;
        } else {
            this.averageRating = (this.averageRating * reviewCount + newRating) / (reviewCount + 1);
        }
        this.reviewCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementFollowers() {
        this.totalFollowers++;
        this.updatedAt = LocalDateTime.now();
    }

    public void decrementFollowers() {
        if (this.totalFollowers > 0) {
            this.totalFollowers--;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // ============= GETTERS (Vendor Active Required) =============

    public UUID getUserId() {
        return userId;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public LocalDateTime getDeactivatedAt() {
        return deactivatedAt;
    }

    public String getShopName() {
        checkVendorActive();
        return shopName;
    }

    public String getShopDescription() {
        checkVendorActive();
        return shopDescription;
    }

    public Category getCategory() {
        checkVendorActive();
        return category;
    }

    public String getShopLogoUrl() {
        checkVendorActive();
        return shopLogoUrl;
    }

    public List<String> getShopImageUrls() {
        checkVendorActive();
        return shopImageUrls;
    }

    public String getShopBannerUrl() {
        checkVendorActive();
        return shopBannerUrl;
    }

    public String getShopTagline() {
        checkVendorActive();
        return shopTagline;
    }

    public String getBusinessEmail() {
        checkVendorActive();
        return businessEmail;
    }

    public String getBusinessPhone() {
        checkVendorActive();
        return businessPhone;
    }

    public String getWebsiteUrl() {
        checkVendorActive();
        return websiteUrl;
    }

    public String getAddress() {
        checkVendorActive();
        return address;
    }

    public String getCity() {
        checkVendorActive();
        return city;
    }

    public String getState() {
        checkVendorActive();
        return state;
    }

    public String getCountryCode() {
        checkVendorActive();
        return countryCode;
    }

    public String getPostalCode() {
        checkVendorActive();
        return postalCode;
    }

    public Map<String, String> getSocialLinks() {
        checkVendorActive();
        return socialLinks;
    }

    public String getTaxId() {
        checkVendorActive();
        return taxId;
    }

    public String getBusinessRegistrationNumber() {
        checkVendorActive();
        return businessRegistrationNumber;
    }

    public String getBusinessType() {
        checkVendorActive();
        return businessType;
    }

    public String getLegalName() {
        checkVendorActive();
        return legalName;
    }

    public String getYearEstablished() {
        checkVendorActive();
        return yearEstablished;
    }

    public String getStoreTheme() {
        checkVendorActive();
        return storeTheme;
    }

    public String getStoreLanguage() {
        checkVendorActive();
        return storeLanguage;
    }

    public String getStoreCurrency() {
        checkVendorActive();
        return storeCurrency;
    }

    public boolean isStoreVisible() {
        checkVendorActive();
        return isStoreVisible;
    }

    public boolean isAcceptReturns() {
        checkVendorActive();
        return acceptReturns;
    }

    public int getReturnWindowDays() {
        checkVendorActive();
        return returnWindowDays;
    }

    public String getReturnPolicy() {
        checkVendorActive();
        return returnPolicy;
    }

    public String getShippingPolicy() {
        checkVendorActive();
        return shippingPolicy;
    }

    public double getFreeShippingThreshold() {
        checkVendorActive();
        return freeShippingThreshold;
    }

    public double getDomesticShippingRate() {
        checkVendorActive();
        return domesticShippingRate;
    }

    public double getInternationalShippingRate() {
        checkVendorActive();
        return internationalShippingRate;
    }

    public List<String> getShippingCountries() {
        checkVendorActive();
        return shippingCountries;
    }

    public int getTotalProducts() {
        checkVendorActive();
        return totalProducts;
    }

    public int getTotalOrders() {
        checkVendorActive();
        return totalOrders;
    }

    public int getTotalRevenue() {
        checkVendorActive();
        return totalRevenue;
    }

    public double getAverageRating() {
        checkVendorActive();
        return averageRating;
    }

    public int getReviewCount() {
        checkVendorActive();
        return reviewCount;
    }

    public int getTotalFollowers() {
        checkVendorActive();
        return totalFollowers;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        checkVendorActive();
        return updatedAt;
    }

    // ============= ADMIN GETTERS (No active check) =============

    public boolean isActiveAdmin(RoleToken rt) {
        try {
            PermissionChecker.checkPermission(rt, Permission.VIEW_ANY_VENDORS);
        } catch (AccessDeniedException e) {
            return false;
        }
        return isActive;
    }

    public LocalDateTime getActivatedAtAdmin(RoleToken rt) {
        try {
            PermissionChecker.checkPermission(rt, Permission.VIEW_ANY_VENDORS);
        } catch (AccessDeniedException e) {
            return null;
        }
        return activatedAt;
    }

    public LocalDateTime getDeactivatedAtAdmin(RoleToken rt) {
        try {
            PermissionChecker.checkPermission(rt, Permission.VIEW_ANY_VENDORS);
        } catch (AccessDeniedException e) {
            return null;
        }
        return deactivatedAt;
    }

    public String getShopNameAdmin(RoleToken rt) {
        try {
            PermissionChecker.checkPermission(rt, Permission.VIEW_ANY_VENDORS);
        } catch (AccessDeniedException e) {
            return null;
        }
        return shopName;
    }

    public String getShopDescriptionAdmin(RoleToken rt) {
        try {
            PermissionChecker.checkPermission(rt, Permission.VIEW_ANY_VENDORS);
        } catch (AccessDeniedException e) {
            return null;
        }
        return shopDescription;
    }

    public Category getCategoryAdmin(RoleToken rt) {
        try {
            PermissionChecker.checkPermission(rt, Permission.VIEW_ANY_VENDORS);
        } catch (AccessDeniedException e) {
            return null;
        }
        return category;
    }

    public String getShopLogoUrlAdmin(RoleToken rt) {
        try {
            PermissionChecker.checkPermission(rt, Permission.VIEW_ANY_VENDORS);
        } catch (AccessDeniedException e) {
            return null;
        }
        return shopLogoUrl;
    }

    public List<String> getShopImageUrlsAdmin(RoleToken rt) {
        try {
            PermissionChecker.checkPermission(rt, Permission.VIEW_ANY_VENDORS);
        } catch (AccessDeniedException e) {
            return null;
        }
        return shopImageUrls;
    }

    public String getShopBannerUrlAdmin(RoleToken rt) {
        try {
            PermissionChecker.checkPermission(rt, Permission.VIEW_ANY_VENDORS);
        } catch (AccessDeniedException e) {
            return null;
        }
        return shopBannerUrl;
    }

    public String getShopTaglineAdmin(RoleToken rt) {
        try {
            PermissionChecker.checkPermission(rt, Permission.VIEW_ANY_VENDORS);
        } catch (AccessDeniedException e) {
            return null;
        }
        return shopTagline;
    }

    // ============= PUBLIC GETTERS (No token required, active required) =============

    public VendorPublicProfile toPublicProfile() {
        checkVendorActive();
        return new VendorPublicProfile(
                userId,
                shopName,
                shopTagline,
                category,
                shopLogoUrl,
                shopBannerUrl,
                shopImageUrls,
                totalProducts,
                averageRating,
                reviewCount,
                totalFollowers,
                isActive,
                isStoreVisible
        );
    }

    // ============= PRIVATE HELPERS =============

    private void checkVendorPermission(RoleToken rt) {
        PermissionChecker.checkPermissionWithUser(rt, this.userId, Permission.MANAGE_SELF_PRODUCTS);
    }

    // ============= RECORDS =============

    public record VendorPublicProfile(
            UUID userId,
            String shopName,
            String shopTagline,
            Category category,
            String shopLogoUrl,
            String shopBannerUrl,
            List<String> shopImageUrls,
            int totalProducts,
            double averageRating,
            int reviewCount,
            int totalFollowers,
            boolean isActive,
            boolean isStoreVisible
    ) {}
}