package com.youfuns.ecommerce.auth;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.repo.InMemoryRepository;
import com.youfuns.ecommerce.user.User;
import com.youfuns.paramtypes.Hash;
import com.youfuns.paramtypes.UuidFormat;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

class UserRepository implements InMemoryRepository<UUID, User> {
    private final Map<UUID, User> store = new ConcurrentHashMap<>();
    private final Map<Hash, User> usernames = new ConcurrentHashMap<>();

    public UserRepository() {
        LoggerManager.quickLog(this, "Created UserRepository");
    }

    @Override
    public ResultReturn insert(User user) {
        LoggerManager.quickLog(this, "Inserting user: " + UuidFormat.shortenUUID(user.getId()));
        if (store.containsKey(user.getId())) {
            LoggerManager.quickLog(this, "Insert failed - user already exists: " + UuidFormat.shortenUUID(user.getId()));
            return new ResultReturn(ResultReturn.Result.FAILURE, "The user already exists");
        }
        if (!anyExistsByUsername(user.getUserCredentials().getUsernamesInternal())) {
            store.put(user.getId(), user);
            putByUsername(user, user.getUserCredentials().getUsernamesInternal());
            LoggerManager.quickLog(this, "User inserted successfully: " + UuidFormat.shortenUUID(user.getId()));
            return new ResultReturn(ResultReturn.Result.SUCCESS, "The user was inserted successfully");
        }
        LoggerManager.quickLog(this, "Username already exists");
        return new ResultReturn(ResultReturn.Result.FAILURE, "The username already exists");
    }

    @Override
    public ResultReturn update(User user) {
        LoggerManager.quickLog(this, "Updating user: " + UuidFormat.shortenUUID(user.getId()));
        if (!store.containsKey(user.getId())) {
            LoggerManager.quickLog(this, "Update failed - user not found: " + UuidFormat.shortenUUID(user.getId()));
            return new ResultReturn(ResultReturn.Result.FAILURE, "The user was not found");
        }
        store.put(user.getId(), user);
        LoggerManager.quickLog(this, "User updated successfully: " + UuidFormat.shortenUUID(user.getId()));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "The user was updated successfully");
    }

    @Override
    public ResultReturn updateById(UUID id, User user) {
        LoggerManager.quickLog(this, "Updating user: " + UuidFormat.shortenUUID(id));
        if (!store.containsKey(id)) {
            LoggerManager.quickLog(this, "Update failed - user not found: " + UuidFormat.shortenUUID(id));
            return new ResultReturn(ResultReturn.Result.FAILURE, "The user was not found");
        }
        store.put(id, user);
        LoggerManager.quickLog(this, "User updated successfully: " + UuidFormat.shortenUUID(id));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "The user was updated successfully");
    }

    @Override
    public ResultReturn delete(User user) {
        LoggerManager.quickLog(this, "Deleting user: " + UuidFormat.shortenUUID(user.getId()));
        return deleteById(user.getId());
    }

    @Override
    public ResultReturn deleteById(UUID id) {
        LoggerManager.quickLog(this, "Deleting user by id: " + UuidFormat.shortenUUID(id));
        User removed = store.remove(id);
        if (removed != null) {
            LoggerManager.quickLog(this, "User deleted successfully: " + UuidFormat.shortenUUID(id));
            return new ResultReturn(ResultReturn.Result.SUCCESS, "The user was deleted successfully");
        }
        LoggerManager.quickLog(this, "Delete failed - user not found: " + UuidFormat.shortenUUID(id));
        return new ResultReturn(ResultReturn.Result.FAILURE, "The user was not found");
    }

    @Override
    public void deleteAll() {
        LoggerManager.quickLog(this, "Deleting all users");
        int count = store.size();
        store.clear();
        LoggerManager.quickLog(this, "Deleted " + count + " users");
    }

    @Override
    public int deleteAllById(List<UUID> ids) {
        LoggerManager.quickLog(this, "Deleting " + ids.size() + " users by id");
        int deleted = 0;
        for (UUID id : ids) {
            if (deleteById(id).isSuccess()) {
                deleted++;
            }
        }
        LoggerManager.quickLog(this, "Deleted " + deleted + " out of " + ids.size() + " users");
        return deleted;
    }

    @Override
    public Optional<User> findById(UUID id) {
        LoggerManager.quickLog(this, "Finding user by id: " + UuidFormat.shortenUUID(id));
        User user = store.get(id);
        if (user != null) {
            LoggerManager.quickLog(this, "User found: " + UuidFormat.shortenUUID(id));
        } else {
            LoggerManager.quickLog(this, "User not found: " + UuidFormat.shortenUUID(id));
        }
        return Optional.ofNullable(user);
    }

    @Override
    public boolean existsById(UUID id) {
        LoggerManager.quickLog(this, "Checking existence of user: " + UuidFormat.shortenUUID(id));
        boolean exists = store.containsKey(id);
        LoggerManager.quickLog(this, "User exists: " + exists + " for id: " + UuidFormat.shortenUUID(id));
        return exists;
    }

    @Override
    public long count() {
        LoggerManager.quickLog(this, "Counting total users");
        long count = store.size();
        LoggerManager.quickLog(this, "Total users: " + count);
        return count;
    }

    @Override
    public List<User> findAll() {
        LoggerManager.quickLog(this, "Finding all users");
        List<User> users = new ArrayList<>(store.values());
        LoggerManager.quickLog(this, "Found " + users.size() + " users");
        return users;
    }

    @Override
    public Map<UUID, User> findAllAsMap() {
        LoggerManager.quickLog(this, "Finding all users as map");
        Map<UUID, User> copy = new HashMap<>(store);
        LoggerManager.quickLog(this, "Returning " + copy.size() + " users as map");
        return copy;
    }

    @Override
    public List<User> findAllById(List<UUID> ids) {
        LoggerManager.quickLog(this, "Finding " + ids.size() + " users by id");
        List<User> users = ids.stream()
                .map(store::get)
                .filter(Objects::nonNull)
                .toList();
        LoggerManager.quickLog(this, "Found " + users.size() + " out of " + ids.size() + " users");
        return users;
    }

    @Override
    public Map<UUID, User> findAllByIdAsMap(List<UUID> ids) {
        LoggerManager.quickLog(this, "Finding " + ids.size() + " users by id as map");
        Map<UUID, User> result = new HashMap<>();
        for (UUID id : ids) {
            User user = store.get(id);
            if (user != null) {
                result.put(id, user);
            }
        }
        LoggerManager.quickLog(this, "Found " + result.size() + " out of " + ids.size() + " users");
        return result;
    }

    // ===== IN-MEMORY SPECIFIC METHODS (from InMemoryRepository) =====

    @Override
    public List<User> selectWhere(Predicate<? super User> predicate) {
        LoggerManager.quickLog(this, "Selecting users with predicate");
        List<User> result = InMemoryRepository.super.selectWhere(predicate);
        LoggerManager.quickLog(this, "Selected " + result.size() + " users matching predicate");
        return result;
    }

    @Override
    public Map<UUID, User> selectWhereAsMap(Predicate<? super User> predicate) {
        LoggerManager.quickLog(this, "Selecting users with predicate as map");
        Map<UUID, User> result = InMemoryRepository.super.selectWhereAsMap(predicate);
        LoggerManager.quickLog(this, "Selected " + result.size() + " users matching predicate as map");
        return result;
    }

    @Override
    public int countWhere(Predicate<? super User> predicate) {
        LoggerManager.quickLog(this, "Counting users with predicate");
        int count = InMemoryRepository.super.countWhere(predicate);
        LoggerManager.quickLog(this, "Counted " + count + " users matching predicate");
        return count;
    }

    @Override
    public void deleteWhere(Predicate<? super User> predicate) {
        LoggerManager.quickLog(this, "Deleting users with predicate");
        InMemoryRepository.super.deleteWhere(predicate);
        LoggerManager.quickLog(this, "Deleted users matching predicate");
    }

    public boolean anyExistsByUsername(List<Hash> username) {
        LoggerManager.quickLog(this, "Checking if username exists");
        for (Hash hash : username) {
            if (usernames.get(hash) != null) {
                LoggerManager.quickLog(this, "Username exists");
                return true;
            }
        }
        LoggerManager.quickLog(this, "Username does not exist");
        return false;
    }

    public void putByUsername(User user, List<Hash> usernames) {
        for (Hash hash : usernames) {
            this.usernames.put(hash, user);
        }
    }

    LoginResult login(String username, String password) {
        LoggerManager.quickLog(this, "Authenticating user: " + username);
        Hash usernameHashed = HashingService.hashUsername(username);
        User usernameMatches = usernames.get(usernameHashed);
        if (usernameMatches == null) {
            LoggerManager.quickLog(this, "User not found: " + username);
            // Return generic message
            return new LoginResult(null, new ResultReturn(ResultReturn.Result.FAILURE,
                    "Invalid username or password."));
        }
        LoggerManager.quickLog(this, "Found username, validating password");
        ResultReturn loginResult = usernameMatches.getUserCredentials().validateLogin(username, password);
        if (loginResult.isSuccess()) {
            return new LoginResult(usernameMatches, loginResult);
        }
        return new LoginResult(null, loginResult);
    }

    public boolean existsManager() {
        LoggerManager.quickLog(this, "Checking if manager exists");
        return store.values().stream()
                .anyMatch(user -> user.getRoles().contains(UserRole.MANAGER));
    }

    public record LoginResult(User user, ResultReturn resultReturn) {}
}