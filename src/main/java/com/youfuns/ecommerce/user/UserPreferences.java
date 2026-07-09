package com.youfuns.ecommerce.user;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.Permission;
import com.youfuns.ecommerce.auth.PermissionChecker;
import com.youfuns.ecommerce.auth.RoleToken;
import com.youfuns.ecommerce.frontend.payloads.RegisterCustomerPayload;
import com.youfuns.paramtypes.LocaleInfo;
import com.youfuns.paramtypes.UuidFormat;

import java.util.UUID;

public class UserPreferences {
    private final UUID userId;

    // Locale & Regional
    private LocaleInfo locale;
    private String timezone;
    private String dateFormat;
    private String timeFormat;
    private String numberFormat;

    // Notifications
    private boolean allowEmailNotifications;
    private boolean allowSmsNotifications;
    private boolean allowPushNotifications;
    private boolean allowMarketingEmails;
    private boolean allowOrderUpdates;
    private boolean allowPromotionalOffers;
    private boolean allowNewsletter;

    // Privacy
    private boolean profileVisible;
    private boolean showOnlineStatus;
    private boolean allowDataCollection;
    private boolean shareAnalytics;

    // Communication
    private String preferredContactMethod;
    private String language;

    public UserPreferences(UUID userId, RegisterCustomerPayload payload) {
        this.userId = userId;

        // Locale & Regional
        this.locale = LocaleInfo.fromCountryCode(payload.countryCode());
        this.timezone = "America/New_York"; // Default, can be updated later
        this.dateFormat = payload.dateFormat() != null ? payload.dateFormat() : "MM/dd/yyyy";
        this.timeFormat = payload.timeFormat() != null ? payload.timeFormat() : "hh:mm a";
        this.numberFormat = payload.numberFormat() != null ? payload.numberFormat() : "1,234.56";

        // Notifications
        this.allowEmailNotifications = payload.allowEmailNotifications();
        this.allowSmsNotifications = payload.allowSmsNotifications();
        this.allowPushNotifications = payload.allowPushNotifications();
        this.allowMarketingEmails = payload.allowMarketingEmails();
        this.allowOrderUpdates = payload.allowOrderUpdates();
        this.allowPromotionalOffers = payload.allowPromotionalOffers();
        this.allowNewsletter = payload.allowNewsletter();

        // Privacy
        this.profileVisible = payload.profileVisible();
        this.showOnlineStatus = payload.showOnlineStatus();
        this.allowDataCollection = payload.allowDataCollection();
        this.shareAnalytics = payload.shareAnalytics();

        // Communication
        this.preferredContactMethod = payload.preferredContactMethod() != null ? payload.preferredContactMethod() : "email";
        this.language = payload.languageCode() != null ? payload.languageCode() : "en";

        LoggerManager.quickLog(this, "Created UserPreferences for user: " + UuidFormat.shortenUUID(userId));
    }

    public UserPreferencesFull toFullPreferences(RoleToken rt) {
        checkPermission(rt);
        return new UserPreferencesFull(
                locale,
                timezone,
                dateFormat,
                timeFormat,
                numberFormat,
                allowEmailNotifications,
                allowSmsNotifications,
                allowPushNotifications,
                allowMarketingEmails,
                allowOrderUpdates,
                allowPromotionalOffers,
                allowNewsletter,
                profileVisible,
                showOnlineStatus,
                allowDataCollection,
                shareAnalytics,
                preferredContactMethod,
                language
        );
    }

    public UserPreferencesFull toFullPreferencesAdmin(RoleToken rt) {
        checkAdminPermission(rt);
        return toFullPreferences(rt);
    }

    private void checkPermission(RoleToken rt) {
        PermissionChecker.checkPermissionWithUser(rt, this.userId, Permission.VIEW_SELF_USER);
    }

    private void checkAdminPermission(RoleToken rt) {
        PermissionChecker.checkPermission(rt, Permission.VIEW_ANY_USERS);
    }

    // Record for full preferences view
    public record UserPreferencesFull(
            LocaleInfo locale,
            String timezone,
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
            boolean shareAnalytics,
            String preferredContactMethod,
            String language
    ) {}

    // ============= GETTERS =============

    public UUID getUserId() {
        return userId;
    }

    public LocaleInfo getLocale() {
        return locale;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public boolean isAllowEmailNotifications() {
        return allowEmailNotifications;
    }

    public boolean isAllowSmsNotifications() {
        return allowSmsNotifications;
    }

    public boolean isAllowPushNotifications() {
        return allowPushNotifications;
    }

    public boolean isAllowMarketingEmails() {
        return allowMarketingEmails;
    }

    public boolean isAllowOrderUpdates() {
        return allowOrderUpdates;
    }

    public boolean isAllowPromotionalOffers() {
        return allowPromotionalOffers;
    }

    public boolean isAllowNewsletter() {
        return allowNewsletter;
    }

    public boolean isProfileVisible() {
        return profileVisible;
    }

    public boolean isShowOnlineStatus() {
        return showOnlineStatus;
    }

    public boolean isAllowDataCollection() {
        return allowDataCollection;
    }

    public boolean isShareAnalytics() {
        return shareAnalytics;
    }

    public String getPreferredContactMethod() {
        return preferredContactMethod;
    }

    public String getLanguage() {
        return language;
    }

    // ============= SETTERS =============

    public UserPreferences setLocale(LocaleInfo locale) {
        if (locale != null) {
            this.locale = locale;
            this.timezone = locale.getTimeZoneId();
            this.language = locale.getLanguageCode();
            LoggerManager.quickLog(this, "Locale updated to: " + locale.getCountryCode() + " | " + locale.getLanguageCode());
        }
        return this;
    }

    public UserPreferences setTimezone(String timezone) {
        if (timezone != null && !timezone.isBlank()) {
            this.timezone = timezone;
            LoggerManager.quickLog(this, "Timezone updated to: " + timezone);
        }
        return this;
    }

    public UserPreferences setDateFormat(String dateFormat) {
        if (dateFormat != null && !dateFormat.isBlank()) {
            this.dateFormat = dateFormat;
            LoggerManager.quickLog(this, "Date format updated to: " + dateFormat);
        }
        return this;
    }

    public UserPreferences setTimeFormat(String timeFormat) {
        if (timeFormat != null && !timeFormat.isBlank()) {
            this.timeFormat = timeFormat;
            LoggerManager.quickLog(this, "Time format updated to: " + timeFormat);
        }
        return this;
    }

    public UserPreferences setNumberFormat(String numberFormat) {
        if (numberFormat != null && !numberFormat.isBlank()) {
            this.numberFormat = numberFormat;
            LoggerManager.quickLog(this, "Number format updated to: " + numberFormat);
        }
        return this;
    }

    public UserPreferences setAllowEmailNotifications(boolean allowEmailNotifications) {
        this.allowEmailNotifications = allowEmailNotifications;
        LoggerManager.quickLog(this, "Email notifications set to: " + allowEmailNotifications);
        return this;
    }

    public UserPreferences setAllowSmsNotifications(boolean allowSmsNotifications) {
        this.allowSmsNotifications = allowSmsNotifications;
        LoggerManager.quickLog(this, "SMS notifications set to: " + allowSmsNotifications);
        return this;
    }

    public UserPreferences setAllowPushNotifications(boolean allowPushNotifications) {
        this.allowPushNotifications = allowPushNotifications;
        LoggerManager.quickLog(this, "Push notifications set to: " + allowPushNotifications);
        return this;
    }

    public UserPreferences setAllowMarketingEmails(boolean allowMarketingEmails) {
        this.allowMarketingEmails = allowMarketingEmails;
        LoggerManager.quickLog(this, "Marketing emails set to: " + allowMarketingEmails);
        return this;
    }

    public UserPreferences setAllowOrderUpdates(boolean allowOrderUpdates) {
        this.allowOrderUpdates = allowOrderUpdates;
        LoggerManager.quickLog(this, "Order updates set to: " + allowOrderUpdates);
        return this;
    }

    public UserPreferences setAllowPromotionalOffers(boolean allowPromotionalOffers) {
        this.allowPromotionalOffers = allowPromotionalOffers;
        LoggerManager.quickLog(this, "Promotional offers set to: " + allowPromotionalOffers);
        return this;
    }

    public UserPreferences setAllowNewsletter(boolean allowNewsletter) {
        this.allowNewsletter = allowNewsletter;
        LoggerManager.quickLog(this, "Newsletter set to: " + allowNewsletter);
        return this;
    }

    public UserPreferences setProfileVisible(boolean profileVisible) {
        this.profileVisible = profileVisible;
        LoggerManager.quickLog(this, "Profile visibility set to: " + profileVisible);
        return this;
    }

    public UserPreferences setShowOnlineStatus(boolean showOnlineStatus) {
        this.showOnlineStatus = showOnlineStatus;
        LoggerManager.quickLog(this, "Online status set to: " + showOnlineStatus);
        return this;
    }

    public UserPreferences setAllowDataCollection(boolean allowDataCollection) {
        this.allowDataCollection = allowDataCollection;
        LoggerManager.quickLog(this, "Data collection set to: " + allowDataCollection);
        return this;
    }

    public UserPreferences setShareAnalytics(boolean shareAnalytics) {
        this.shareAnalytics = shareAnalytics;
        LoggerManager.quickLog(this, "Share analytics set to: " + shareAnalytics);
        return this;
    }


    public UserPreferences setPreferredContactMethod(String preferredContactMethod) {
        if (preferredContactMethod != null && !preferredContactMethod.isBlank()) {
            this.preferredContactMethod = preferredContactMethod;
            LoggerManager.quickLog(this, "Preferred contact method updated to: " + preferredContactMethod);
        }
        return this;
    }

    public UserPreferences setLanguage(String language) {
        if (language != null && !language.isBlank()) {
            this.language = language;
            LoggerManager.quickLog(this, "Language updated to: " + language);
        }
        return this;
    }

    // ============= BULK UPDATES =============

    public UserPreferences updateAll(UserPreferences newPrefs) {
        if (newPrefs == null) return this;

        if (newPrefs.getLocale() != null) setLocale(newPrefs.getLocale());
        if (newPrefs.getTimezone() != null) setTimezone(newPrefs.getTimezone());
        if (newPrefs.getDateFormat() != null) setDateFormat(newPrefs.getDateFormat());
        if (newPrefs.getTimeFormat() != null) setTimeFormat(newPrefs.getTimeFormat());
        if (newPrefs.getNumberFormat() != null) setNumberFormat(newPrefs.getNumberFormat());
        setAllowEmailNotifications(newPrefs.isAllowEmailNotifications());
        setAllowSmsNotifications(newPrefs.isAllowSmsNotifications());
        setAllowPushNotifications(newPrefs.isAllowPushNotifications());
        setAllowMarketingEmails(newPrefs.isAllowMarketingEmails());
        setAllowOrderUpdates(newPrefs.isAllowOrderUpdates());
        setAllowPromotionalOffers(newPrefs.isAllowPromotionalOffers());
        setAllowNewsletter(newPrefs.isAllowNewsletter());
        setProfileVisible(newPrefs.isProfileVisible());
        setShowOnlineStatus(newPrefs.isShowOnlineStatus());
        setAllowDataCollection(newPrefs.isAllowDataCollection());
        setShareAnalytics(newPrefs.isShareAnalytics());
        if (newPrefs.getPreferredContactMethod() != null) setPreferredContactMethod(newPrefs.getPreferredContactMethod());
        if (newPrefs.getLanguage() != null) setLanguage(newPrefs.getLanguage());

        LoggerManager.quickLog(this, "Bulk update applied to UserPreferences for user: " + UuidFormat.shortenUUID(userId));
        return this;
    }

    // ============= HELPER METHODS =============

    public UserPreferences resetToDefaults() {
        LoggerManager.quickLog(this, "Resetting UserPreferences to defaults for user: " + UuidFormat.shortenUUID(userId));
        this.locale = LocaleInfo.fromCountryCode("US");
        this.timezone = "America/New_York";
        this.dateFormat = "MM/dd/yyyy";
        this.timeFormat = "hh:mm a";
        this.numberFormat = "1,234.56";
        this.allowEmailNotifications = true;
        this.allowSmsNotifications = false;
        this.allowPushNotifications = true;
        this.allowMarketingEmails = false;
        this.allowOrderUpdates = true;
        this.allowPromotionalOffers = false;
        this.allowNewsletter = false;
        this.profileVisible = true;
        this.showOnlineStatus = true;
        this.allowDataCollection = true;
        this.shareAnalytics = true;
        this.preferredContactMethod = "email";
        this.language = "en";
        return this;
    }

    @Override
    public String toString() {
        return "UserPreferences{" +
                "userId=" + UuidFormat.shortenUUID(userId) +
                ", locale=" + locale.getCountryCode() +
                ", timezone='" + timezone + '\'' +
                ", allowEmailNotifications=" + allowEmailNotifications +
                '}';
    }
}