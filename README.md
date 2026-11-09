# Auth Framework — README

A lightweight, generic authentication and RBAC (role-based access control) framework built on top of the `WebServer` framework. It provides user registration, login, password hashing, JWT session tokens, lockout policies, and a permission system with transitive implications and self-scoped checks.

***Note:** Like the WebServer, this framework is intended for prototyping. It does not ship with a database, email verification, or production-grade rate limiting.*

## Overview

The framework is organized around five concepts:

- **`AuthManager<P, U, R>`** — the facade. Owns the `PermissionChecker`, `DefaultPermissions`, and the user `Repository`. Handles login, registration, JWT→user resolution, and user deletion.
- **`User<P>`** — an abstract base class. Subclass it to add app-specific fields (`name`, `approved`, `profile`, ...). Holds the user's ID, `UserCredentials`, and `UserRoleHolder`.
- **`UserCredentials`** — password hash, lockout state, and username set. All credential management (change password, add/remove username, lock/unlock) is done through methods on this object, each guarded by a `RoleToken`.
- **`UserRoleHolder<P>`** — the set of roles assigned to a user. Role mutation (`addRole`, `removeRole`) requires a `RoleToken` with the configured "assign role" permission.
- **`PermissionChecker<P>` + `DefaultPermissions<P>`** — the RBAC engine. `DefaultPermissions` is what you pass around; `checker()` is what you call for manual checks.

## Adding to your project

Import the framework packages:

```java
import com.youfuns.auth.AuthManager;
import com.youfuns.auth.rbac.*;
import com.youfuns.auth.user.*;
```

The framework depends on `password4j` (Argon2) and Jackson (JSON), both brought in transitively by the AuthManager's default `UserCredentials` implementation.

## The permission model

A `Permission` is any enum implementing `com.youfuns.auth.rbac.Permission`:

```java
public interface Permission {
    String name();                           // enum name
    boolean needsSpecificUser();             // true = requires a target user ID
    Set<? extends Permission> implications();// permissions implied by this one
}
```

Example:

```java
public enum MainPerm implements Permission {
    MANAGE_SELF(true, Set.of()),
    MANAGE_ROLES(false, Set.of()),
    MANAGE_USERS(false, Set.of(MANAGE_SELF, MANAGE_ROLES));  // implies both

    private final boolean needsSpecificUser;
    private final Set<Permission> implications;

    MainPerm(boolean needsSpecificUser, Set<Permission> implications) { ... }

    @Override public boolean needsSpecificUser() { return needsSpecificUser; }
    @Override public Set<? extends Permission> implications() { return implications; }
}
```

**Implications are transitive.** If `MANAGE_USERS` implies `MANAGE_ROLES`, and `MANAGE_ROLES` implies `MANAGE_SELF`, then any role carrying `MANAGE_USERS` also passes checks for `MANAGE_ROLES` and `MANAGE_SELF`.

**`needsSpecificUser = true`** marks a permission that must be checked against a target user (e.g. `UPDATE_SELF` only applies to your own account). These are checked with `checkPermissionWithUser(rt, target, perm)`.

## Roles

A `UserRole<P>` is a named set of permissions:

```java
public record UserRole<P extends Enum<P> & Permission>(String name, Set<P> permissions) { ... }
```

Define roles as static constants:

```java
public enum MainRole {
    NORMAL_USER (Set.of(MainPerm.MANAGE_SELF)),
    ADMIN_USER  (Set.of(MainPerm.MANAGE_USERS));

    private final UserRole<MainPerm> role;
    MainRole(Set<MainPerm> perms) {
        this.role = new UserRole<>(name(), perms);
    }
    public UserRole<MainPerm> getRole() { return role; }
}
```

`UserRole.hasPermission(p)` walks the implication graph to answer membership.

## Subclassing `User`

```java
public class MyUser extends User<MainPerm> {
    private final String name;
    private boolean approved;

    public MyUser(String name, UserCredentialPayload creds,
                  DefaultPermissions<MainPerm> perms) {
        super(creds, perms);                    // defaults to initialUserPermissions
        this.name = name;
    }

    public MyUser(String name, Set<UserRole<MainPerm>> roles,
                  UserCredentialPayload creds,
                  DefaultPermissions<MainPerm> perms) {
        super(roles, creds, perms);             // explicit role set
        this.approved = true;
        this.name = name;
    }

    // For deserialization
    protected MyUser(UUID id, UserCredentials creds,
                     UserRoleHolder<MainPerm> holder, Instant created,
                     String name, boolean approved,
                     DefaultPermissions<MainPerm> perms) {
        super(id, creds, holder, created, perms);
        this.name = name;
        this.approved = approved;
    }

    public void approve(RoleToken adminToken) {
        if (approved) return;
        getUserRoleHolder().addRole(adminToken, MainRole.ADMIN_USER.getRole());
        approved = true;
    }
}
```

`User<P>` provides:
- `getId()`, `getCreatedDate()`
- `getUserCredentials()` (all password / username management)
- `getUserRoles()`, `getUserRoleHolder()` (protected)
- `getToken()` — issues a fresh `RoleToken` for this user, consumed by the next `checkPermission*` call

## Wiring: `AuthManager.builder()`

```java
InMemoryRepository<UUID, MyUser> users = new InMemoryRepository<>();

AuthManager<MainPerm, MyUser, InMemoryRepository<UUID, MyUser>> auth =
    AuthManager.<MainPerm, MyUser, InMemoryRepository<UUID, MyUser>>builder()
        .repository(users)
        .toFindUsersByUsername((repo, username) ->
            repo.findWhere(u -> u.getUserCredentials().getUsernames().contains(username))
                .stream().map(Map.Entry::getValue).toList())
        .defaultPermissions(
            Set.of(MainRole.NORMAL_USER.getRole()),  // assigned at registration
            MainPerm.MANAGE_SELF,                     // permission for "manage self"
            MainPerm.MANAGE_USERS,                    // permission for "manage any user"
            MainPerm.MANAGE_ROLES                     // permission for "assign roles"
        )
        .selfPermissionEqualsSelf()                   // self-checks require token.user == target
        .passwordLockoutPolicy(5, 300)                // 5 attempts, 300s lockout
        .sessionTokenService(JwtService::generateToken, JwtService::extractSubject)
        .build();
```

### Builder options

| Method | Purpose |
|---|---|
| `repository(RepositoryClass)` | **Required.** Where users live. |
| `toFindUsersByUsername(BiFunction<R, String, List<U>>)` | **Required.** Returns all users whose username set contains the given string. Must be unique in the app (uniqueness is enforced by `addUser`). |
| `defaultPermissions(initialRoles, manageSelf, manageUsers, assignRole)` | **Required.** Sets the initial role set and the three "management" permissions. |
| `passwordHasher(Function, BiFunction)` | **Optional.** Replace Argon2. Default: `HashingService::argon2Hash` / `verifyArgon2Hash`. |
| `passwordLockoutPolicy(int maxAttempts, int lockoutSecs)` | **Optional.** Default `5, 300`. |
| `enablePasswordStrengthValidation(boolean)` | **Optional.** Default `true`. Toggle `PasswordStrengthValidator`. |
| `sessionTokenService(Function generateToken, Function extractSubject)` | **Optional.** Default: an opaque `SESSION_TOKEN[...]` wrapper. Replace with JWT for production. |
| `selfPermissionEqualsSelf()` | **Optional but strongly recommended.** Registers a global `onUserPermission` handler that requires `token.user == target` for all `needsSpecificUser` permissions. Without it, `checkPermissionWithUser` returns `true` for any target as long as the user has the permission — **a serious hole**. |

### `AuthManager` methods

```java
// Registration & lookup
auth.addUser(user);                                     // checks username uniqueness, inserts
auth.findById(uuid);                                    // Optional<UserClass>
auth.getUserFromJwt(jwt);                               // validates + resolves; null on failure

// Login
UserCredentials.LoginResult r = auth.login(username, password);
r.isSuccess();                                          // boolean
r.jwtToken();                                           // String, null on failure
r.resultReturn();                                       // full ResultReturn with message

// Deletion
auth.deleteUser(user);                                  // checks user has MANAGE_SELF on self
auth.adminDeleteUser(adminToken, user);                 // checks adminToken has MANAGE_USERS

// Accessors
auth.getPermissionChecker();                            // PermissionChecker<P>
auth.getDefaultPermissions();                           // DefaultPermissions<P>
```

## `DefaultPermissions<P>`

The object your domain code calls to check permissions. Holds three configured permissions plus the initial role set.

```java
DefaultPermissions<MainPerm> perms = auth.getDefaultPermissions();

perms.checkManageSelf(rt, targetId);        // throws if token lacks manageSelf on targetId
perms.checkManageUsers(rt);                 // throws if token lacks manageUsers
perms.checkManageRoles(rt);                 // throws if token lacks assignRole

perms.getChecker();                         // PermissionChecker<P> — for custom checks
perms.getInitialUserPermissions();          // Set<UserRole<P>>
perms.issueToken(roleHolder);               // RoleToken (same as roleHolder.getToken())
```

### `DefaultPermissions.AlwaysTruePermissions`

Sealed subclass where every check is a no-op. Useful for tests and demos:

```java
var perms = new DefaultPermissions.AlwaysTruePermissions<MyPerm>(initialRoles);
```

With this, `checkManageSelf`, `checkManageUsers`, and `checkManageRoles` all return immediately. `issueToken(holder)` returns a fake token when `holder == null`, but calls through to the real `TokenManager` otherwise. **The `PermissionChecker` inside an `AlwaysTruePermissions` instance is `null` — do not call `getChecker()` on it.**

### `DefaultPermissions.EmptyPerm`

A sealed-permission set for apps that don't care about specific permissions:

```java
enum P implements Permission { ... }  // anywhere
DefaultPermissions.AlwaysTruePermissions<DefaultPermissions.EmptyPerm> perms =
    new DefaultPermissions.AlwaysTruePermissions<>(Set.of());
```

## `PermissionChecker<P>`

The low-level RBAC engine. Usually you use `DefaultPermissions` wrappers, but for custom checks:

```java
PermissionChecker<MainPerm> checker = perms.getChecker();

// Basic check
boolean ok = checker.checkPermission(rt, MainPerm.MANAGE_USERS);

// With target user (for needsSpecificUser permissions)
boolean ok = checker.checkPermissionWithUser(rt, targetId, MainPerm.MANAGE_SELF);

// Throwing variants (recommended — let onException map to 403)
checker.checkPermissionAndThrow(rt, MainPerm.MANAGE_USERS);
checker.checkPermissionAndThrowWithUser(rt, targetId, MainPerm.MANAGE_SELF);

// Role-only check (skips custom handlers)
boolean ok = checker.checkPermissionOnly(rt, MainPerm.MANAGE_USERS);
```

### Custom per-permission handlers

```java
checker.onPermission(MainPerm.MANAGE_USERS, executorId -> {
    // Return false to deny, true to allow
    return !executorId.equals(SOME_BANNED_ID);
});
checker.onUserPermission(MainPerm.MANAGE_SELF, (executorId, targetId) -> {
    // Only allow self-management
    return executorId.equals(targetId);
});
checker.onPermission((perm, executorId) -> true);        // global handler
checker.onUserPermission((perm, executorId, targetId) -> true);
```

The global handler registered via `selfPermissionEqualsSelf()` is:

```java
checker.onUserPermission((perm, executorId, targetId) -> executorId.equals(targetId));
```

Which is why `selfPermissionEqualsSelf()` should always be on: without it, `checkPermissionWithUser` returns `true` for any target as long as the user holds the permission, letting any user with `MANAGE_SELF` edit anyone's profile.

### `RoleToken` — single-use

A `RoleToken` is a UUID pair `(id, issuedUser)`. `UserRoleHolder.getToken()` issues one, and it's stored in the checker's `TokenManager`. **The next `checkPermission*` call consumes it** (removes from the map). A consumed token can't be reused — every check must issue a fresh token:

```java
checker.checkPermissionAndThrow(user.getToken(), MainPerm.MANAGE_USERS);   // OK
checker.checkPermissionAndThrow(user.getToken(), MainPerm.MANAGE_USERS);   // fails — token consumed
```

**Consequence:** any call path that performs two permission checks must issue two tokens. But `UserCredentials.unlockAccount`, `lockAccount`, `changePassword`, etc. each issue their own token internally (`checkManageSelf(user.getToken(), ...)`) — so calling `user.getUserCredentials().changePassword(user.getToken(), ...)` from outside is correct; the token is consumed once, inside `changePassword`.

**Anti-pattern:** `var t = user.getToken(); checker.checkPermissionAndThrow(t, X); user.getCredentials().changePassword(t, ...);` — the second call fails because `changePassword` will check `MANAGE_SELF` on the already-consumed `t`. Always pass `user.getToken()` fresh per operation.

## `UserCredentials`

Every credential operation is permission-checked internally via the `DefaultPermissions` passed to the constructor. You call them with a `RoleToken`.

### Password management

```java
ResultReturn r = user.getUserCredentials().changePassword(rt, oldPass, newPass);
// rt must have MANAGE_SELF on this user's ID

user.getUserCredentials().resetPasswordAdmin(rt, newPass);
// rt must have MANAGE_USERS — doesn't check old password
```

### Username management

```java
user.getUserCredentials().addUsername(rt, "newAlias");        // MANAGE_SELF
user.getUserCredentials().removeUsername(rt, "oldAlias");     // MANAGE_SELF
user.getUserCredentials().addUsernameAdmin(rt, "newAlias");   // MANAGE_USERS
user.getUserCredentials().removeUsernameAdmin(rt, "oldAlias");// MANAGE_USERS
```

### Lockout

```java
user.getUserCredentials().lockAccount(rt, seconds);           // MANAGE_USERS
user.getUserCredentials().unlockAccount(rt);                  // MANAGE_USERS
boolean locked = user.getUserCredentials().isLocked(rt);      // MANAGE_SELF — auto-clears if expired
boolean locked = user.getUserCredentials().isLockedAdmin(rt); // MANAGE_USERS
```

### Login

```java
UserCredentials.LoginResult r = user.getUserCredentials().login(username, password);
r.isSuccess();
r.jwtToken();        // null on failure
```

`validateLogin` handles the lockout logic:
- If `passwordLocked` and the expiry hasn't passed → returns `"Account is temporarily locked."`
- If `adminLocked` and the expiry hasn't passed → returns `"Account is locked by administrator."`
- Failed attempts increment `failedAttempts`; at `MAX_FAILED_ATTEMPTS` (default 5), `lockAccountForPassword()` fires and the message is `"Account is temporarily locked."`
- Successful login resets both locks and `failedAttempts` to 0.

## `UserRoleHolder<P>` and role mutation

```java
UserRoleHolder<MainPerm> holder = user.getUserRoleHolder(); // protected — use from within User subclass

holder.addRole(adminToken, role);       // requires MANAGE_ROLES on adminToken
holder.removeRole(adminToken, role);    // requires MANAGE_ROLES
holder.roles();                         // Set<UserRole<P>> (immutable copy)
holder.getToken();                      // RoleToken for this holder
```

Note: `getUserRoleHolder()` is `protected` on `User<P>`. Subclasses expose it explicitly:

```java
public class MyUser extends User<MainPerm> {
    public void approve(RoleToken adminToken) {
        getUserRoleHolder().addRole(adminToken, MainRole.ADMIN_USER.getRole());
    }
}
```

## `UserCredentialPayload`

Simple input record used by `User<P>`'s constructor:

```java
new UserCredentialPayload(Set.of("alice", "alice@example.com"), "plaintextPassword");
```

The payload's `password` is hashed at construction time (via `passwordHasher`). The `usernames` set is stored as-is (plaintext), and used for login lookup.

## `SessionTokenService`

Pluggable session-token backend. Default:

```java
generateToken   = str -> "SESSION_TOKEN[" + str + "]";
extractSubject  = str -> str.replaceAll(".*?SESSION_TOKEN\\[(.*?)\\].*", "$1");
validateToken   = (token) -> { try { return extractSubject(token) != null; } catch (...) { return false; } }
```

**The default `validateToken` is insecure.** It returns `true` for *any* string containing `SESSION_TOKEN[...]`, and does not verify a signature. Use it only with the default `SESSION_TOKEN[...]` format in trusted contexts.

To use JWT:

```java
AuthManager.builder()
    ...
    .sessionTokenService(JwtService::generateToken, JwtService::extractSubject)
```

`JwtService` (from the WebServer framework) verifies the signature, and `validateToken` therefore actually means "signature is valid and not expired."

## `UserSerializationDto`

Reflection-based snapshot of `User<P>` for persistence. For a `MyUser` subclass, wrap it:

```java
public record MyUserSerializationDto(
        UserSerializationDto user,
        String name,
        boolean approved) implements Serializable {

    public static MyUserSerializationDto fromUser(MyUser u) {
        try {
            return new MyUserSerializationDto(
                UserSerializationDto.fromUser(u), u.name, u.approved);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public MyUser toUser(DefaultPermissions<MainPerm> live,
                         Function<String, UserRole<MainPerm>> roleParser) {
        UserCredentials creds = user.toCredentials(live);
        UserRoleHolder<MainPerm> holder = user.toRoleHolder(roleParser, live);
        return new MyUser(user.id(), creds, holder, user.createdDate(),
                          name, approved, live);
    }
}
```

### `UserSerializationDto.fromUser(User<?>)`

Reads six private fields from `UserCredentials` via reflection (`passwordHash`, `passwordLocked`, `adminLocked`, `passwordLockoutExpiry`, `adminLockoutExpiry`, `failedAttempts`). Caches `Field` lookups in a static `ConcurrentHashMap`.

### `UserSerializationDto.toCredentials(perms)` / `.toRoleHolder(roleParser, perms)`

Reconstructs the components with a **live** `DefaultPermissions`. The `roleParser` maps a role name string back to a `UserRole<P>`:

```java
name -> MainRole.valueOf(name).getRole()
```

**Field-name fragility:** if you rename any of the six `UserCredentials` fields, serialization breaks silently (fallbacks return safe defaults). The fallbacks are:

| Field | Fallback | Notes |
|---|---|---|
| `passwordHash` | **throws** | No safe default for the hash. |
| `passwordLocked` | `false` | Not the sentinel — `adminLocked` is. |
| `adminLocked` | `true` | **Fail-closed:** unreadable record → admin-locked. |
| `passwordLockoutExpiry` | `Instant.now()` | Irrelevant since `passwordLocked = false`. |
| `adminLockoutExpiry` | `now + 100 years` | Prevents auto-unlock; `Instant.MAX` breaks most DBs. |
| `failedAttempts` | `0` | Harmless while admin-locked. |

If a serialization exception occurs, the user is reconstructed in an **admin-locked** state with no valid password. Recovery requires an admin `unlockAccount` call. This is the correct fail-closed behavior, but you must **watch for the log line** (the framework logs a `WARN` when the fallback fires) — otherwise, admins will see "user is locked by administrator" with no obvious cause.

**JPMS:** `setAccessible(true)` on `UserCredentials`'s private fields works on classpath builds but requires `opens com.youfuns.auth.login to com.youfuns.auth.user;` in your `module-info.java` if you modularize.

## `PasswordStrengthValidator`

Standalone utility called by `UserCredentials` when `enablePasswordStrengthValidation(true)` (the default).

```java
ResultReturn r = PasswordStrengthValidator.validatePasswordWithMessage("hunter2");
r.isSuccess();
r.message();
```

Policy (hardcoded):

- 6–128 characters
- At least one uppercase, one lowercase, one digit, one symbol
- No 3+ repeated characters dominating the password
- Not a common password or fragment (with leetspeak substitutions)

Common-password detection uses `applySubstitutions` (0→`[o0]`, `s`→`[s$5]`, etc.) so `p@ssw0rd` is caught as `password`.

**This is a UX filter, not a security control.** It catches dictionary attacks but not targeted guesses.

## `HashingService`

Package-private; you configure it via `AuthManager.Builder.passwordHasher(...)`. Default:

```java
HashingService.argon2Hash(password)               // Argon2id, m=64MB, t=3, p=4
HashingService.verifyArgon2Hash(password, hash)
```

The Argon2 parameters (`65536, 3, 4, 32, Argon2.ID`) are hardcoded. If you need different parameters, override `passwordHasher` in the builder.

`HashingService` also provides SHA-256 (`sha256Hash`, `verifySha256Hash`) for deterministic (unsalted) hashing — **not used by the framework anymore** (usernames are stored plaintext). Kept for compatibility with external code that wants to hash something deterministically.

## Complete example

See `com.youfuns.cms.Demo` for a full working server:

```java
var auth = AuthManager.<MainPerm, MyUser, InMemoryRepository<UUID, MyUser>>builder()
    .repository(repository)
    .sessionTokenService(JwtService::generateToken, JwtService::extractSubject)
    .toFindUsersByUsername((repo, username) ->
        repo.findWhere(u -> u.getUserCredentials().getUsernames().contains(username))
            .stream().map(Map.Entry::getValue).toList())
    .defaultPermissions(Set.of(MainRole.NORMAL_USER.getRole()),
                        MainPerm.MANAGE_SELF,
                        MainPerm.MANAGE_USERS,
                        MainPerm.MANAGE_ROLES)
    .selfPermissionEqualsSelf()
    .passwordLockoutPolicy(5, 300)
    .build();

// Bootstrap admin
MyUser admin = new MyUser("Admin", Set.of(MainRole.ADMIN_USER.getRole()),
                          new UserCredentialPayload(Set.of("admin"), "..." ),
                          auth.getDefaultPermissions());
auth.addUser(admin);

// Server with auth head
WebServer.create(8080)
    .on("/register", "POST", ex -> {
        String name = ex.getJsonName("User");
        String username = ex.getJsonUsername("user");
        String password = ex.getJsonPassword("password");
        MyUser u = new MyUser(name,
            new UserCredentialPayload(Set.of(username), password),
            auth.getDefaultPermissions());
        auth.addUser(u);
        ex.send("Created: " + u.getId());
    })
    .on("/login", "POST", ex -> {
        var result = auth.login(ex.getJsonUsername(""), ex.getJsonPassword(""));
        ex.sendJson(result);
    })
    .head("/api", ex -> {
        MyUser u = auth.getUserFromJwt(ex.getBearerToken());
        if (u == null) { ex.sendUnauthorized(); return false; }
        ex.setAttribute("user", u);
        return true;
    })
    .on("/api/me", ex -> ex.sendJson(ex.getAttribute("user", MyUser.class)))
    .start();
```

## Warnings & known limitations

1. **`selfPermissionEqualsSelf()` is not on by default.** Without it, `checkPermissionWithUser` will pass for any target — a serious RBAC hole. Always call it unless you're registering your own handler.
2. **`RoleToken` is single-use.** Never cache a token for two checks. `checkPermissionAndThrowWithUser(rt, t, p)` followed by `user.getUserCredentials().changePassword(rt, ...)` will fail because the token is consumed by the first call.
3. **`DefaultPermissions.AlwaysTruePermissions.getChecker()` returns `null`** — do not call it. This class is for tests/demos only.
4. **`UserSerializationDto` fallbacks are silent-by-exception.** If a `UserCredentials` field is renamed, the user is reconstructed admin-locked. Watch for the `WARN` log. The safest fix is to add package-private accessors to `UserCredentials` and drop reflection, but that's a framework change.
5. **Default `SessionTokenService` is insecure** — `validateToken` checks only substring presence, not signature. Always wire `.sessionTokenService(JwtService::generateToken, JwtService::extractSubject)` for any app that authenticates users over HTTP.
6. **`passwordLocked` / `adminLocked` are plain fields, not `volatile`.** Concurrent mutation (login incrementing `failedAttempts` while serialization reads `passwordLocked`) can produce a torn snapshot — a user could be serialized as `passwordLocked=true, passwordLockoutExpiry=null`, which crashes the next login with an NPE. `AtomicInteger` for `failedAttempts` protects the counter, but not the other six fields. Protect with `synchronized` or `volatile` if this matters for your deployment.
7. **`AuthManager.login` scans all users matching the username** via `findUsersByUsername`. The default `InMemoryRepository` implementation is O(n). For anything beyond a prototype, back `findUsersByUsername` with an indexed lookup (e.g. `HashMap<Hash, User>` or a JDBC `user_usernames` table).
8. **`AuthManager` is per-JVM.** `UserCredentials.MAX_FAILED_ATTEMPTS`, `SessionTokenService.generateToken`, and `passwordHasher` are static. Two `AuthManager`s with different settings cannot coexist in one process.

## Directory structure

```
src/
└── com/youfuns/auth/
    ├── AuthManager.java
    ├── login/
    │   ├── HashingService.java
    │   ├── PasswordStrengthValidator.java
    │   ├── SessionTokenService.java
    │   └── UserCredentials.java
    ├── rbac/
    │   ├── AccessDeniedException.java
    │   ├── DefaultPermissions.java
    │   ├── Permission.java
    │   ├── PermissionChecker.java
    │   ├── ResultReturn.java
    │   ├── RoleToken.java
    │   ├── UserRole.java
    │   └── UserRoleHolder.java
    └── user/
        ├── User.java
        ├── UserCredentialPayload.java
        └── UserSerializationDto.java
```

## Other demonstrations

See `com.youfuns.cms.Demo` (full server) and `com.youfuns.cms.CredsDemo` (standalone credentials) for complete examples of:
- Registration, login, JWT, `/api` head, role checks
- Serialization round-trip with a `MyUser` subclass
- Lockout policy in action
- `AlwaysTruePermissions` for bypassing in tests