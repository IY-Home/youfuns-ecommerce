package com.youfuns.ecommerce.user;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.*;
import com.youfuns.ecommerce.frontend.payloads.RegisterUserPayload;
import com.youfuns.ecommerce.frontend.payloads.RegisterVendorPayload;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.ecommerce.status.UserStatus;
import com.youfuns.ecommerce.vendor.VendorRepositoryService;
import com.youfuns.ecommerce.vendor.VendorService;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.paramtypes.EmailAddress;
import com.youfuns.paramtypes.PhoneNumber;
import com.youfuns.paramtypes.Username;
import com.youfuns.paramtypes.UuidFormat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class User {
    private final UUID id;
    private final UserProfile userProfile;
    private final UserRoleHolder role;
    private final UserCredentials userCredentials;
    private final CustomerService customerService;
    private final UserPreferences userPreferences;
    private final VendorService vendorService;
    private UserStatus userStatus;

    public User(RegisterUserPayload registerUserPayload) {
        RegisterUserPayload rc = registerUserPayload;
        LoggerManager.quickLog(this, "Creating User...");
        this.id = UUID.randomUUID();
        this.userProfile = new UserProfile(id, rc.name(), rc.email(), rc.username(), rc.dateOfBirth(), rc.phone());
        this.role = new UserRoleHolder(id);
        this.userCredentials = new UserCredentials(id, Arrays.asList(rc.email(), rc.username()), rc.password());
        this.customerService = new CustomerService(id, rc.registerCustomerPayload());
        this.userPreferences = new UserPreferences(id, rc.registerCustomerPayload());
        this.vendorService = new VendorService(id);
        this.userStatus = new UserStatus(this.id, UserStatus.Status.ACTIVE);
        LoggerManager.quickLog(this, "Created User (id " + UuidFormat.shortenUUID(id) + ")");
    }

    public User(String email, String username, String password) {
        LoggerManager.quickLog(this, "Creating User using simple constructor...");
        this.id = UUID.randomUUID();
        this.userProfile = new UserProfile(id, "Anonymous User", email, username, LocalDate.now(), "0000000000");
        this.role = new UserRoleHolder(id);
        this.userCredentials = new UserCredentials(id, Arrays.asList(email, username), password);
        this.customerService = null;
        this.userPreferences = null;
        this.vendorService = null;
        LoggerManager.quickLog(this, "Created simple User (id " + UuidFormat.shortenUUID(id) + ")");
    }

    // ============= GETTERS =============

    public UUID getId() {
        return id;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public UserCredentials getUserCredentials() {
        return userCredentials;
    }

    public UserRoleHolder getRole() {
        return role;
    }

    public CustomerService getCustomerService() {
        return customerService;
    }

    public UserPreferences getUserPreferences() {
        return userPreferences;
    }

    public VendorService getVendorService() {
        return vendorService;
    }

    // ============= TOKEN ISSUANCE =============

    public RoleToken getToken() {
        return role.getToken();
    }

    public List<UserRole> getRoles() {
        return role.getRoles();
    }

    // ============= VENDOR REGISTRATION =============

    public ResultReturn registerVendor(RoleToken rt, RegisterVendorPayload registerVendorPayload) {
        LoggerManager.quickLog(this, "Registering vendor for user: " + UuidFormat.shortenUUID(id));

        // Check permission
        try {
            PermissionChecker.checkPermission(rt, Permission.BECOME_VENDOR);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions to become a vendor.");
        }

        // Initialize vendor service with payload
        return vendorService.initVendor(registerVendorPayload);
    }

    // ============= VENDOR APPROVAL (Admin Only) =============

    public ResultReturn approveVendorStep1(RoleToken adminRt) {
        LoggerManager.quickLog(this, "Approving vendor step 1 (activate) for user: " + UuidFormat.shortenUUID(id));
        return vendorService.activateVendor(adminRt);
    }

    public ResultReturn approveVendorStep2(RoleToken adminRt) {
        LoggerManager.quickLog(this, "Approving vendor step 2 (add role) for user: " + UuidFormat.shortenUUID(id));

        // Add VENDOR role
        return role.addRole(adminRt, UserRole.VENDOR);
    }

    // Step 3 will be handled in FrontendService

    // ============= USER AND VENDOR DISABLE (Admin Only) =============

    public ResultReturn disableVendor(RoleToken adminRt) {
        LoggerManager.quickLog(this, "Disabling vendor for user: " + UuidFormat.shortenUUID(id));
        return vendorService.deactivateVendor(adminRt);
    }

    public ResultReturn disableUser(RoleToken adminRt) {
        LoggerManager.quickLog(this, "Disabling user: " + UuidFormat.shortenUUID(id));
        try {
            PermissionChecker.checkPermission(adminRt, Permission.DISABLE_ANY_USER);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions to disable user.");
        }
        this.userStatus.updateStatus(UserStatus.Status.SUSPENDED);
        return new ResultReturn(ResultReturn.Result.SUCCESS, "User disabled.");
    }

    public ResultReturn enableUser(RoleToken adminRt) {
        LoggerManager.quickLog(this, "Enabling user: " + UuidFormat.shortenUUID(id));
        if (this.userStatus.getStatus() == UserStatus.Status.ACTIVE) {
            return new ResultReturn(ResultReturn.Result.WARNING, "User is already active.");
        }
        try {
            PermissionChecker.checkPermission(adminRt, Permission.DISABLE_ANY_USER);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions to enable user.");
        }
        this.userStatus.updateStatus(UserStatus.Status.ACTIVE);
        return new ResultReturn(ResultReturn.Result.SUCCESS, "User is active.");
    }

    public UserRoleHolder getRoleForBootstrap() {
        return role;
    }

    // ============= CHECK VENDOR STATUS =============

    public boolean isVendor() {
        return role.getRoles().contains(UserRole.VENDOR);
    }

    public boolean isVendorActive() {
        return isVendor() && vendorService.isActive();
    }
}