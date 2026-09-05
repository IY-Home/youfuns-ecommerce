package com.youfuns.cms.user;

import com.youfuns.auth.DefaultPermissions;
import com.youfuns.auth.RoleToken;
import com.youfuns.cms.auth.CmsPermission;
import com.youfuns.cms.paramtypes.EmailAddress;
import com.youfuns.cms.paramtypes.PhoneNumber;
import com.youfuns.cms.paramtypes.Username;

import java.util.UUID;
import java.util.function.Consumer;

public class UserInfo {
    private UUID id;
    private String name;
    private EmailAddress email;
    private PhoneNumber phone;
    private Username username;
    private Consumer<RoleToken> checkPerm;

    private static DefaultPermissions<CmsPermission> defaultPermissions;

    @SuppressWarnings("unchecked")
    static void setDefaultPermissions(DefaultPermissions<?> defaultPermissions) {
        UserInfo.defaultPermissions = (DefaultPermissions<CmsPermission>) defaultPermissions;
    }

    public UserInfo(UUID id, String name, EmailAddress email, PhoneNumber phoneNumber, Username username) {
        this.name = name;
        this.email = email;
        this.phone = phoneNumber;
        this.username = username;
        this.checkPerm = rt -> defaultPermissions.checkManageSelf(rt, id);
    }
    public String getName() {
        return name;
    }
    public void setName(RoleToken rt, String name) {
        checkPerm.accept(rt);
        this.name = name;
    }
    public EmailAddress getEmail() {
        return email;
    }
    public void setEmail(RoleToken rt, EmailAddress email) {
        checkPerm.accept(rt);
        this.email = email;
    }
    public PhoneNumber getPhone() {
        return phone;
    }
    public void setPhone(RoleToken rt, PhoneNumber phone) {
        checkPerm.accept(rt);
        this.phone = phone;
    }
    public Username getUsername() {
        return username;
    }
    public void setUsername(RoleToken rt, Username username) {
        checkPerm.accept(rt);
        this.username = username;
    }
}
