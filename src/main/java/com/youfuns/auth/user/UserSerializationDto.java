package com.youfuns.auth.user;

import com.youfuns.auth.login.AuthContext;
import com.youfuns.auth.login.UserCredentials;
import com.youfuns.auth.login.UserLoginConfig;
import com.youfuns.auth.rbac.DefaultPermissions;
import com.youfuns.auth.rbac.Permission;
import com.youfuns.auth.rbac.UserRole;
import com.youfuns.auth.rbac.UserRoleHolder;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public record UserSerializationDto(
        UUID id,
        UserCredentialsDto userCredentials,
        Set<String> roles,
        Instant createdDate) implements Serializable {

    public record UserCredentialsDto(Set<String> usernames, String passwordHash,
                                     LockoutStateDto lockoutState,
                                     UserLoginConfig customLoginConfig) implements Serializable {


        public record LockoutStateDto(boolean passwordLocked, boolean adminLocked,
                                      Instant passwordLockoutExpiry, Instant adminLockoutExpiry,
                                      int failedAttempts) implements Serializable {}

        private UserCredentials toCredentials(UUID id, AuthContext<?> context) {
            return new UserCredentials(
                    id, usernames, passwordHash,
                    lockoutState.passwordLocked, lockoutState.adminLocked,
                    lockoutState.passwordLockoutExpiry, lockoutState.adminLockoutExpiry,
                    lockoutState.failedAttempts, customLoginConfig, context);
        }
        private static final Map<String, Field> credFields = new ConcurrentHashMap<>();
    }

    public UserCredentials toCredentials(AuthContext<?> context) {
         return userCredentials.toCredentials(id, context);
    }

    public <T extends Enum<T> & Permission> UserRoleHolder<T> toRoleHolder(Function<String, UserRole<T>> roleParser, DefaultPermissions<T> livePermissions) {
        return new UserRoleHolder<>(id, roles.stream().map(roleParser).collect(Collectors.toSet()), livePermissions);
    }

    public static UserSerializationDto fromUser(User<?> user)
            throws ReflectiveOperationException {
        UserCredentials creds = user.getUserCredentials();
        UserCredentials.LockoutState lockout = getField(creds, "lockoutState");
        return new UserSerializationDto(
                user.getId(),
                new UserCredentialsDto(
                    creds.getUsernames(),
                    getField(creds, "passwordHash"), // if even the hash can't be found, throw
                    new UserCredentialsDto.LockoutStateDto(getField(lockout, "passwordLocked", false),
                    getField(lockout, "adminLocked", true), // fail-closed
                    getField(lockout, "passwordLockoutExpiry", Instant.now()),
                    getField(lockout, "adminLockoutExpiry", Instant.now().plus(Duration.ofDays(365L * 100))), // fail-closed — expires 100 years later
                    getField(lockout, "failedAttempts", new AtomicInteger(0)).get()),
                    getField(creds, "overrideConfig", null)
                ),
                user.getUserRoles().stream().map(UserRole::name).collect(Collectors.toSet()),
                user.getCreatedDate());
    }

    @SuppressWarnings("unchecked")
    private static <T, R> R getField(T object, String field) throws ReflectiveOperationException {
        if (!(object instanceof UserCredentials) && !(object instanceof UserCredentials.LockoutState)) return null;
        if (UserCredentialsDto.credFields.containsKey(field)) {
            return (R) UserCredentialsDto.credFields.get(field).get(object);
        }
        Field f = object.getClass().getDeclaredField(field);
        f.setAccessible(true);
        UserCredentialsDto.credFields.put(field, f);
        return (R) f.get(object);
    }
    private static <T, R> R getField(T object, String field, R fallback) {
        try {
            return getField(object, field);
        } catch (ReflectiveOperationException e) {
            return fallback;
        }
    }
}