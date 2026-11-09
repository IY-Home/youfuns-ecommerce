package com.youfuns.cms;

import com.youfuns.auth.AuthManager;
import com.youfuns.auth.login.AuthContext;
import com.youfuns.auth.login.UserCredentials;
import com.youfuns.auth.login.UserLoginConfig;
import com.youfuns.auth.rbac.*;
import com.youfuns.auth.user.User;
import com.youfuns.auth.user.UserCredentialPayload;
import com.youfuns.auth.user.UserSerializationDto;
import com.youfuns.logger.LoggerManager;
import com.youfuns.logger.SimpleLogger;
import com.youfuns.repo.InMemoryRepository;
import com.youfuns.webserver.JwtService;
import com.youfuns.webserver.WebServer;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

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

    public enum MainRole {
        NORMAL_USER  (Set.of(MainPerm.MANAGE_SELF)),
        ADMIN_USER (Set.of(MainPerm.MANAGE_USERS));

        private final UserRole<MainPerm> role;

        MainRole(Set<MainPerm> perms) {
            this.role = new UserRole<>(name(), perms);   // "USER" / "ADMIN"
        }
        public UserRole<MainPerm> getRole() { return role; }
    }

    public static class MyUser extends User<MainPerm> {
        private boolean approved = false;
        private final String name;

        public MyUser(String name, UserCredentialPayload userRegistrationPayload, AuthContext<MainPerm> context) {
            super(null, userRegistrationPayload, context, null);
            this.name = name;
        }

        public MyUser(String name, Set<UserRole<MainPerm>> initialRoles, UserCredentialPayload userRegistrationPayload, AuthContext<MainPerm> context) {
            super(initialRoles, userRegistrationPayload, context, null);
            this.approved = true;
            this.name = name;
        }

        protected MyUser(UUID id,
                         UserCredentials userCredentials,
                         UserRoleHolder<MainPerm> userRoleHolder,
                         Instant createdDate,
                         String name,
                         boolean approved, DefaultPermissions<MainPerm> livePermissions) {
            super(id, userCredentials, userRoleHolder, createdDate, livePermissions);
            this.name = name;
            this.approved = approved;
        }

        public void approve(RoleToken adminToken) {
            if (approved) return;
            this.getUserRoleHolder().addRole(adminToken, MainRole.ADMIN_USER.getRole());
            approved = true;
        }
    }

    public record MyUserSerializationDto(UserSerializationDto user, String name, boolean approved)
            implements Serializable {
        public static MyUserSerializationDto fromUser(MyUser user) {
            try {
                return new MyUserSerializationDto(UserSerializationDto.fromUser(user), user.name, user.approved);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        public MyUser toUser(AuthContext<MainPerm> context, Function<String, UserRole<MainPerm>> resolveRole) {
            UserCredentials creds = user.toCredentials(context);
            UserRoleHolder<MainPerm> holder = user.toRoleHolder(resolveRole, context.permissions());
            return new MyUser(user.id(), creds, holder, user.createdDate(), name, approved, context.permissions());
        }
    }

    public static void main(String[] args) {
        LoggerManager.INSTANCE.getLogger().setLogLevel(SimpleLogger.Level.DEBUG);
        InMemoryRepository<UUID, MyUser> repository = new InMemoryRepository<>();
        UserLoginConfig loginConfig = UserLoginConfig.defaults();

        // call method directly with explicit type witness on builder
        var authManager = AuthManager.<MainPerm, MyUser, InMemoryRepository<UUID, MyUser>>builder()
                .repository(repository)
                .userLoginConfig(loginConfig)
                .sessionTokenService(JwtService::generateToken, JwtService::extractSubject)
                .toFindUsersByUsername((repo, username) ->
                        repo
                        .findWhere(user -> user.getUserCredentials().getUsernames().contains(username))
                        .stream().map(Map.Entry::getValue).toList())
                .defaultPermissions(Set.of(MainRole.NORMAL_USER.getRole()), MainPerm.MANAGE_SELF, MainPerm.MANAGE_USERS, MainPerm.MANAGE_ROLES)
                .selfPermissionEqualsSelf()
                .build();
        UserCredentialPayload payload = new UserCredentialPayload(Set.of("TomDoe1", "tom1@doe.com"), "Xk9#mP2!qL");
        MyUser initialAdmin = new MyUser("Tom Doe", Set.of(MainRole.ADMIN_USER.getRole()), payload, authManager.authContext());
        System.out.println("Initial Admin UUID: " + initialAdmin.getId());
        authManager.addUser(initialAdmin);
        System.out.println(authManager.login("TomDoe1", "wrongpass").toString());
        System.out.println(authManager.login("TomDoe1", "Xk9#mP2!qL").toString());

        WebServer.create(8080, LoggerManager.INSTANCE.getLogger())
                .on("/health", "OK")
                .on("/register", "POST", exchange -> {
                    String name = exchange.getJsonName("John Doe");
                    String username = exchange.getJsonUsername("JohnDoe1");
                    String password = exchange.getJsonPassword("Xk9#mP2!qL");
                    MyUser user = new MyUser(name, new UserCredentialPayload(Set.of(username), password), authManager.authContext());
                    authManager.addUser(user);
                    exchange.send("Created user: " + user.getId());
                })
                .on("/login", "POST", exchange -> {
                    String username = exchange.getJsonUsername("JohnDoe1");
                    String password = exchange.getJsonPassword("Xk9#mP2!qL");
                    UserCredentials.LoginResult loginResult = authManager.login(username, password);
                    exchange.sendJson(loginResult);
                })
                .on("/register", exchange -> exchange.sendMethodNotAllowed("POST"))
                .on("/login", exchange -> exchange.sendMethodNotAllowed("POST"))
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
                // These are for demonstrating serialization, remove for production
                .on("/jsonFromUser/?", (params, exchange) -> {
                    try {
                        exchange.sendJson(MyUserSerializationDto.fromUser(repository.findById(UUID.fromString(params[0])).orElseThrow()));
                    } catch (Exception e) {
                        exchange.sendBadRequest("Invalid user");
                    }
                })
                .on("/userFromJson", "POST", exchange -> {
                    MyUserSerializationDto dto = exchange.parseBodyAsJson(MyUserSerializationDto.class);
                    authManager.addUser(dto.toUser(authManager.authContext(), name -> MainRole.valueOf(name).getRole()));
                    exchange.send("User added.");
                })
                .on("/serializeAll", exchange -> {
                    List<MyUserSerializationDto> list = new ArrayList<>();
                    for (MyUser user : repository.findAll().values()) {
                        MyUserSerializationDto dto = MyUserSerializationDto.fromUser(user);
                        list.add(dto);
                    }
                    exchange.sendJson(list);
                })
                .onException((exchange, exception) -> {
                    LoggerManager.quickLog(Demo.class, "Unhandled: " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
                    try {
                        if (exception instanceof IllegalArgumentException)
                            exchange.sendBadRequest(exception.getMessage());
                        else if (exception instanceof java.io.IOException)
                            exchange.sendBadRequest("Malformed request body");
                        else
                            exchange.sendError("Internal error");
                    } catch (Exception ignored) {}
                })
                .start();
    }
}
