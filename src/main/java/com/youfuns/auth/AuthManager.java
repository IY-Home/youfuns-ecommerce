package com.youfuns.auth;

import com.youfuns.auth.login.UserCredentials;
import com.youfuns.auth.rbac.*;
import com.youfuns.logger.LoggerManager;
import com.youfuns.repo.Repository;
import com.youfuns.auth.user.User;
import com.youfuns.auth.login.SessionTokenService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AuthManager<PermissionClass extends Enum<PermissionClass> & Permission, UserClass extends User<PermissionClass>, RepositoryClass extends Repository<UUID, UserClass>> {
    private final PermissionChecker<PermissionClass> permissionChecker;
    private final DefaultPermissions<PermissionClass> defaultPermissions;
    private final RepositoryClass userRepository;
    private final BiFunction<RepositoryClass, String, List<UserClass>> findUsersByUsername;

    private AuthManager(PermissionChecker<PermissionClass> permissionChecker,
                        DefaultPermissions<PermissionClass> defaultPermissions,
                        RepositoryClass initialRepository,
                        BiFunction<RepositoryClass, String, List<UserClass>> findUsersByUsername) {
        this.permissionChecker = permissionChecker;
        this.defaultPermissions = defaultPermissions;
        this.userRepository = initialRepository;
        this.findUsersByUsername = findUsersByUsername;
        LoggerManager.quickLog(this, "Created AuthManager");
    }

    public static <T extends Enum<T> & Permission, U extends User<T>, V extends Repository<UUID, U>> Builder<T, U, V> builder() {
        return new Builder<>();
    }

    public UserCredentials.LoginResult login(String username, String password) {
        UserCredentials.LoginResult loginResult = null;
        for (UserClass user : findUsersByUsername.apply(userRepository, username)) {
            loginResult = user.getUserCredentials().login(username, password);
            if (!loginResult.isSuccess()) continue;
            break;
        }
        return (loginResult == null) ? new UserCredentials.LoginResult(UserCredentials.genericLoginFailure, null) : loginResult;
    }

    public ResultReturn addUser(UserClass user) {
        Set<String> usernames = user.getUserCredentials().getUsernames();
        for (String username : usernames) {
            if (!findUsersByUsername.apply(userRepository, username).isEmpty()) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Username is already in use");
            }
        }
        return userRepository.insert(user.getId(), user);
    }

    public Optional<UserClass> findById(UUID id) {
        return userRepository.findById(id);
    }

    public UserClass getUserFromJwt(String jwt) {
        if (!SessionTokenService.validateToken(jwt)) return null;
        String subject = SessionTokenService.extractSubject(jwt);
        if (subject == null) return null;
        Optional<UserClass> user = userRepository.findById(UUID.fromString(subject));
        return user.orElse(null);
    }

    public PermissionChecker<PermissionClass> getPermissionChecker() {
        return permissionChecker;
    }

    public ResultReturn deleteUser(UserClass user) {
        defaultPermissions.checkManageSelf(user.getUserRoleHolder().getToken(), user.getId());
        return userRepository.delete(user.getId());
    }

    public ResultReturn adminDeleteUser(RoleToken adminToken, UserClass user) {
        defaultPermissions.checkManageUsers(adminToken);
        return userRepository.delete(user.getId());
    }

    public DefaultPermissions<PermissionClass> getDefaultPermissions() {
        return defaultPermissions;
    }

    public static class Builder<PermissionClass extends Enum<PermissionClass> & Permission, UserClass extends User<PermissionClass>, RepositoryClass extends Repository<UUID, UserClass>> {
        private DefaultPermissions<PermissionClass> defaultPermissions;
        private final PermissionChecker<PermissionClass> permissionChecker;
        private RepositoryClass userRepository;
        private BiFunction<RepositoryClass, String, List<UserClass>> findUsersByUsername;

        private Builder() {
            LoggerManager.quickLog(this, "Beginning AuthManager.Builder");
            this.permissionChecker = new PermissionChecker<>();
        }

        public Builder<PermissionClass, UserClass, RepositoryClass> defaultPermissions(Set<UserRole<PermissionClass>> initialPerms, PermissionClass manageSelf, PermissionClass manageAdmin, PermissionClass assignRole) {
            if (defaultPermissions != null) throw new IllegalStateException("Default permissions already initialized.");
            this.defaultPermissions = new DefaultPermissions<>(initialPerms, permissionChecker, manageSelf, manageAdmin, assignRole);
            LoggerManager.quickLog(this, "Set default permissions");
            return this;
        }

        public Builder<PermissionClass, UserClass, RepositoryClass> passwordHasher(Function<String, String> passwordHasher, BiFunction<String, String, Boolean> passwordValidator) {
            UserCredentials.setPasswordHasher(passwordHasher, passwordValidator);
            LoggerManager.quickLog(this, "Set password hasher");
            return this;
        }

        public Builder<PermissionClass, UserClass, RepositoryClass> enablePasswordStrengthValidation(boolean enable) {
            UserCredentials.enablePasswordValidation(enable);
            LoggerManager.quickLog(this, "Set enable password strength validation:");
            return this;
        }

        public Builder<PermissionClass, UserClass, RepositoryClass> sessionTokenService(Function<String, String> generateToken, Function<String, String> extractSubject) {
            UserCredentials.setSessionTokenService(generateToken, extractSubject);
            LoggerManager.quickLog(this, "Set session token service");
            return this;
        }

        public Builder<PermissionClass, UserClass, RepositoryClass> repository(RepositoryClass userRepository) {
            this.userRepository = userRepository;
            LoggerManager.quickLog(this, "Set repository");
            return this;
        }

        public Builder<PermissionClass, UserClass, RepositoryClass> toFindUsersByUsername(BiFunction<RepositoryClass, String, List<UserClass>> function) {
            this.findUsersByUsername = function;
            LoggerManager.quickLog(this, "Set findUsersByUsername function");
            return this;
        }

        public Builder<PermissionClass, UserClass, RepositoryClass> selfPermissionEqualsSelf() {
            permissionChecker.onUserPermission((permission, executor, target) -> executor.equals(target));
            LoggerManager.quickLog(this, "Enabled selfPermissionEqualsSelf");
            return this;
        }

        public Builder<PermissionClass, UserClass, RepositoryClass> passwordLockoutPolicy(int maxAttempts, int lockoutSecs) {
            UserCredentials.setPasswordLockoutPolicy(maxAttempts, lockoutSecs);
            LoggerManager.quickLog(this, "Set password lockout policy");
            return this;
        }

        public AuthManager<PermissionClass, UserClass, RepositoryClass> build() {
            if (defaultPermissions == null) throw new IllegalStateException("Default permissions not initialized.");
            if (userRepository == null) throw new IllegalStateException("User repository not initialized.");
            if (findUsersByUsername == null) throw new IllegalStateException("Username search function not initialized.");
            LoggerManager.quickLog(this, "Building AuthManager");
            return new AuthManager<>(permissionChecker, defaultPermissions, userRepository, findUsersByUsername);
        }
    }
}
