package com.youfuns.ecommerce.user;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.Permission;
import com.youfuns.ecommerce.auth.PermissionChecker;
import com.youfuns.ecommerce.auth.RoleToken;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.exceptions.IllegalFieldException;
import com.youfuns.paramtypes.*;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.utils.SimpleLogger;
import com.youfuns.paramtypes.UuidFormat;

import java.time.LocalDate;
import java.util.UUID;

public class UserProfile {
    private UUID id;
    private Name name;
    private EmailAddress email;
    private Username username;
    private FilePath profilePicturePath;
    private LocalDate dateOfBirth;
    private PhoneNumber phone;
    private boolean isEmailVerified;
    private boolean isPhoneVerified;

    public UserProfile(UUID id, String name, String email, String username,
                       LocalDate dateOfBirth, String phone) {
        LoggerManager.quickLog(this, "Creating UserProfile instance...");
        this.id = id;
        try {
            this.name = new Name(name);
            this.email = new EmailAddress(email);
            this.username = new Username(username);
            this.phone = new PhoneNumber(phone);
            this.dateOfBirth = dateOfBirth;
        } catch (IllegalFieldException e) {
            LoggerManager.quickLog(this, e.getMessage() + ": " + e.getReceivedValue(), SimpleLogger.Level.ERROR);
            throw e;
        }
        LoggerManager.quickLog(this, "Successfully created UserProfile instance for " + UuidFormat.shortenUUID(id));
    }

    // ============= PRIVATE HELPER METHODS =============

    /**
     * Check if the token belongs to this user and has the VIEW_SELF_USER permission
     */
    private void checkReadSelf(RoleToken rt) {
        PermissionChecker.checkPermissionWithUser(rt, this.id, Permission.VIEW_SELF_USER);
    }

    /**
     * Check if the token has the VIEW_ANY_USER permission (admin read)
     */
    private void checkReadAdmin(RoleToken rt) {
        PermissionChecker.checkPermission(rt, Permission.VIEW_ANY_USERS);
    }

    /**
     * Check if the token belongs to this user and has the UPDATE_SELF_USER permission
     */
    private void checkWriteSelf(RoleToken rt) {
        PermissionChecker.checkPermissionWithUser(rt, this.id, Permission.UPDATE_SELF_USER);
    }

    /**
     * Check if the token has the MANAGE_ANY_USERS permission (admin write)
     */
    private void checkWriteAdmin(RoleToken rt) {
        PermissionChecker.checkPermission(rt, Permission.MANAGE_ANY_USERS);
    }

    // ============= GETTERS - SELF READ (Requires VIEW_SELF_USER) =============

    public Name getName(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning name...");
        checkReadSelf(rt);
        return name;
    }

    public EmailAddress getEmail(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning email...");
        checkReadSelf(rt);
        return email;
    }

    public PhoneNumber getPhone(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning phone number...");
        checkReadSelf(rt);
        return phone;
    }

    public Username getUsername(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning username...");
        checkReadSelf(rt);
        return username;
    }

    public FilePath getProfilePicturePath(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning profile picture path...");
        checkReadSelf(rt);
        return profilePicturePath;
    }

    public LocalDate getDateOfBirth(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning date of birth...");
        checkReadSelf(rt);
        return dateOfBirth;
    }


    public boolean isEmailVerified(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning email verification status...");
        checkReadSelf(rt);
        return isEmailVerified;
    }

    public boolean isPhoneVerified(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning phone verification status...");
        checkReadSelf(rt);
        return isPhoneVerified;
    }

    public UUID getId(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning ID...");
        checkReadSelf(rt);
        return id;
    }

    // ============= GETTERS - ADMIN READ (Requires VIEW_ANY_USER) =============

    public Name getNameAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning name to admin user...");
        checkReadAdmin(rt);
        return name;
    }

    public EmailAddress getEmailAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning email to admin user...");
        checkReadAdmin(rt);
        return email;
    }

    public PhoneNumber getPhoneAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning phone number to admin user...");
        checkReadAdmin(rt);
        return phone;
    }

    public Username getUsernameAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning username to admin user...");
        checkReadAdmin(rt);
        return username;
    }

    public FilePath getProfilePicturePathAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning profile picture path to admin user...");
        checkReadAdmin(rt);
        return profilePicturePath;
    }

    public LocalDate getDateOfBirthAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning date of birth to admin user...");
        checkReadAdmin(rt);
        return dateOfBirth;
    }


    public boolean isEmailVerifiedAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning email verification status to admin user...");
        checkReadAdmin(rt);
        return isEmailVerified;
    }

    public boolean isPhoneVerifiedAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning phone verification status to admin user...");
        checkReadAdmin(rt);
        return isPhoneVerified;
    }

    public UUID getIdAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Returning ID to admin user...");
        checkReadAdmin(rt);
        return id;
    }

    // ============= SETTERS - SELF WRITE (Requires UPDATE_SELF_USER) =============

    public ResultReturn setName(RoleToken rt, Name name) {
        LoggerManager.quickLog(this, "Setting name...");
        checkWriteSelf(rt);
        this.name = name;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Name has been set.");
    }

    public ResultReturn setEmail(RoleToken rt, EmailAddress email) {
        LoggerManager.quickLog(this, "Setting email...");
        checkWriteSelf(rt);
        this.email = email;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Email has been set.");
    }

    public ResultReturn setPhone(RoleToken rt, PhoneNumber phone) {
        LoggerManager.quickLog(this, "Setting phone number...");
        checkWriteSelf(rt);
        this.phone = phone;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Phone number has been set.");
    }

    public ResultReturn setUsername(RoleToken rt, Username username) {
        LoggerManager.quickLog(this, "Setting username...");
        checkWriteSelf(rt);
        this.username = username;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Username has been set.");
    }

    public ResultReturn setProfilePicturePath(RoleToken rt, FilePath profilePicturePath) {
        LoggerManager.quickLog(this, "Setting profile picture path...");
        checkWriteSelf(rt);
        this.profilePicturePath = profilePicturePath;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Profile picture path has been set.");
    }

    public ResultReturn setDateOfBirth(RoleToken rt, LocalDate dateOfBirth) {
        LoggerManager.quickLog(this, "Setting date of birth...");
        checkWriteSelf(rt);
        this.dateOfBirth = dateOfBirth;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Date of birth has been set.");
    }

    // ============= SETTERS - ADMIN WRITE (Requires MANAGE_ANY_USERS) =============

    public ResultReturn setNameAdmin(RoleToken rt, Name name) {
        LoggerManager.quickLog(this, "Setting name by admin user...");
        checkWriteAdmin(rt);
        this.name = name;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Name has been set by admin user.");
    }

    public ResultReturn setEmailAdmin(RoleToken rt, EmailAddress email) {
        LoggerManager.quickLog(this, "Setting email by admin user...");
        checkWriteAdmin(rt);
        this.email = email;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Email has been set by admin user.");
    }

    public ResultReturn setPhoneAdmin(RoleToken rt, PhoneNumber phone) {
        LoggerManager.quickLog(this, "Setting phone number...");
        checkWriteAdmin(rt);
        this.phone = phone;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Phone number has been set by admin user.");
    }

    public ResultReturn setUsernameAdmin(RoleToken rt, Username username) {
        LoggerManager.quickLog(this, "Setting username by admin user...");
        checkWriteAdmin(rt);
        this.username = username;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Username has been set by admin user.");
    }

    public ResultReturn setProfilePicturePathAdmin(RoleToken rt, FilePath profilePicturePath) {
        LoggerManager.quickLog(this, "Setting profile picture path by admin user...");
        checkWriteAdmin(rt);
        this.profilePicturePath = profilePicturePath;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Profile picture path has been set by admin user.");
    }

    public ResultReturn setDateOfBirthAdmin(RoleToken rt, LocalDate dateOfBirth) {
        LoggerManager.quickLog(this, "Setting date of birth by admin user...");
        checkWriteAdmin(rt);
        this.dateOfBirth = dateOfBirth;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Date of birth has been set by admin user.");
    }


    public ResultReturn setEmailVerifiedAdmin(RoleToken rt, boolean isEmailVerified) {
        LoggerManager.quickLog(this, "Setting email verification status by admin user...");
        checkWriteAdmin(rt);
        this.isEmailVerified = isEmailVerified;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Email verification status has been set by admin user.");
    }

    public ResultReturn setPhoneVerifiedAdmin(RoleToken rt, boolean isPhoneVerified) {
        LoggerManager.quickLog(this, "Setting phone verification status by admin user...");
        checkWriteAdmin(rt);
        this.isPhoneVerified = isPhoneVerified;
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Phone verification status has been set by admin user.");
    }

    // Automated vendor, email, and phone number verification logic will be seperately implemented soon.

    // ============= FULL PROFILE READ METHODS =============

    /**
     * Read the full UserProfile with self-permission checking.
     * Requires VIEW_SELF_USER permission and the token must belong to this user.
     */
    public UserProfile readSelf(RoleToken rt) {
        LoggerManager.quickLog(this, "Reading full profile with self permission check...");
        checkReadSelf(rt);
        return this;
    }
    /**
     * Full view for self or administrator.
     * Requires token.
     */
    public FullUserProfile toFullProfile(RoleToken rt) {
        LoggerManager.quickLog(this, "Creating full profile view...");
        checkReadSelf(rt);
        return new FullUserProfile(
                this.id,
                this.name,
                this.email,
                this.phone,
                this.dateOfBirth,
                this.profilePicturePath,
                this.username,
                this.isEmailVerified,
                this.isPhoneVerified
        );
    }
    public FullUserProfile toFullProfileAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Creating full profile view...");
        checkReadAdmin(rt);
        return new FullUserProfile(
                this.id,
                this.name,
                this.email,
                this.phone,
                this.dateOfBirth,
                this.profilePicturePath,
                this.username,
                this.isEmailVerified,
                this.isPhoneVerified
        );
    }

    /**
     * Full profile - visible only to authorized users
     */
    public record FullUserProfile(
            UUID id,
            Name name,
            EmailAddress emailAddress,
            PhoneNumber phoneNumber,
            LocalDate dateOfBirth,
            FilePath profilePicturePath,
            Username username,
            boolean isEmailVerified,
            boolean isPhoneVerified
    ) {}

}