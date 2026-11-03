package com.youfuns.cms;

import com.youfuns.auth.AuthManager;
import com.youfuns.auth.login.UserCredentials;
import com.youfuns.auth.rbac.*;
import com.youfuns.auth.user.User;
import com.youfuns.auth.user.UserCredentialPayload;
import com.youfuns.logger.LoggerManager;
import com.youfuns.logger.SimpleLogger;
import com.youfuns.repo.InMemoryRepository;
import com.youfuns.webserver.JwtService;
import com.youfuns.webserver.WebServer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Demo {
    public enum MainPerm implements Permission {
        MANAGE_SELF(true, Set.of()),
        MANAGE_ROLES(false, Set.of()),
        MANAGE_USERS(false, Set.of(MANAGE_SELF, MANAGE_ROLES));

        private final boolean needsSpecificUser;
        private final Set<Permission> implications;

        MainPerm(boolean needsSpecificUser, Set<Permission> implications) {
            this.needsSpecificUser = needsSpecificUser;
            this.implications = implications;
        }

        @Override
        public boolean needsSpecificUser() {
            return needsSpecificUser;
        }

        @Override
        public Set<? extends Permission> implications() {
            return implications;
        }
    }

    public static UserRole<MainPerm> NORMAL_USER = new UserRole<>("NORMAL_USER", Set.of(MainPerm.MANAGE_SELF));
    public static UserRole<MainPerm> ADMIN_USER = new UserRole<>("ADMIN_USER", Set.of(MainPerm.MANAGE_USERS));

    public static class MyUser extends User<MainPerm> {
        private static boolean adminRegistered;
        private boolean approved = false;
        String name;

        public MyUser(String name, UserCredentialPayload userRegistrationPayload, DefaultPermissions<MainPerm> defaultPermissions) {
            super(adminRegistered ? null : Set.of(NORMAL_USER, ADMIN_USER), userRegistrationPayload, defaultPermissions);
            if (!adminRegistered) adminRegistered = true;
            this.name = name;
        }

        public void approve(RoleToken adminToken) {
            if (approved) return;
            this.getUserRoleHolder().addRole(adminToken, ADMIN_USER);
            approved = true;
        }
    }

    public static void main(String[] args) {
        LoggerManager.INSTANCE.getLogger().setLogLevel(SimpleLogger.Level.DEBUG);
        // call method directly with explicit type witness
        var authManager = AuthManager.<MainPerm, MyUser, InMemoryRepository<UUID, MyUser>>builder()
                .repository(new InMemoryRepository<>())
                .sessionTokenService(JwtService::generateToken, JwtService::extractSubject)
                .toFindUsersByUsername((repo, username) ->
                        repo
                        .findWhere(user -> user.getUserCredentials().getUsernames().contains(username))
                        .stream().map(Map.Entry::getValue).toList())
                .defaultPermissions(Set.of(NORMAL_USER), MainPerm.MANAGE_SELF, MainPerm.MANAGE_USERS, MainPerm.MANAGE_ROLES)
                .selfPermissionEqualsSelf()
                .passwordLockoutPolicy(5, 300)
                .build();
        UserCredentialPayload payload = new UserCredentialPayload("TomDoe1", "Xk9#mP2!qL");
        MyUser initialAdmin = new MyUser("Tom Doe", payload, authManager.getDefaultPermissions());
        authManager.addUser(initialAdmin);
        System.out.println(authManager.login("TomDoe1", "wrongpass").toString());
        System.out.println(authManager.login("TomDoe1", "Xk9#mP2!qL").toString());

        WebServer.create(8080, LoggerManager.INSTANCE.getLogger())
                .on("/register", exchange -> {
                    String name = exchange.getJsonName("John Doe");
                    String username = exchange.getJsonUsername("JohnDoe1");
                    String password = exchange.getJsonPassword("Xk9#mP2!qL");
                    MyUser user = new MyUser(name, new UserCredentialPayload(username, password), authManager.getDefaultPermissions());
                    authManager.addUser(user);
                    exchange.send("Created user: " + user.getId());
                })
                .on("/login", exchange -> {
                    String username = exchange.getJsonUsername("JohnDoe1");
                    String password = exchange.getJsonPassword("Xk9#mP2!qL");
                    UserCredentials.LoginResult loginResult = authManager.login(username, password);
                    exchange.sendJson(loginResult);
                })
                .on("/api/checkInfo", exchange -> {
                    MyUser user = exchange.getAttribute("user", MyUser.class);
                    exchange.send("Name: " + user.name + ", approved: " + user.approved);
                })
                .on("/api/approve", exchange -> {
                    MyUser user = exchange.getAttribute("user", MyUser.class);
                    UUID target = UUID.fromString(exchange.getJsonParameterAsString("target"));
                    MyUser targetUser = authManager.findById(target).orElse(null);
                    if (targetUser == null) {
                        exchange.send("User not found");
                        return;
                    }
                    try {
                        targetUser.approve(user.getToken());
                    } catch (AccessDeniedException e) {
                        exchange.send("Access denied: " + e.getMessage());
                        return;
                    }
                    exchange.send("Approved");
                })
                .head("/api", exchange -> {
                    MyUser user = authManager.getUserFromJwt(exchange.getBearerToken());
                    if (user == null) { exchange.sendUnauthorized(); return false; }
                    exchange.setAttribute("user", user);
                    return true;
                })
                .start();
    }
}
