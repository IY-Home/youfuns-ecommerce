package com.youfuns.ecommerce.auth;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.user.User;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.paramtypes.JsonWebToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepositoryService {
    private final UserRepository userRepository;
    public UserRepositoryService() {
        LoggerManager.quickLog(this, "Creating UserRepositoryService and UserRepository...");
        this.userRepository = new UserRepository();
        LoggerManager.quickLog(this, "UserRepository created");
    }
    public record SessionToken(JsonWebToken token, ResultReturn result) {
        // Factory methods for clarity
        public static SessionToken success(JsonWebToken token) {
            return new SessionToken(token, new ResultReturn(ResultReturn.Result.SUCCESS, "Login successful"));
        }

        public static SessionToken failure(String message) {
            return new SessionToken(null, new ResultReturn(ResultReturn.Result.FAILURE, message));
        }
    }

    public SessionToken loginUser(String username, String password) {
        LoggerManager.quickLog(this, "Logging in with UserRepository...");

        UserRepository.LoginResult result = userRepository.login(username, password);

        if (result == null || result.resultReturn() == null) {
            LoggerManager.quickLog(this, "Login result is null!");
            return SessionToken.failure("An unknown error occurred during authentication.");
        }

        if (!result.resultReturn().isSuccess() || result.user() == null) {
            LoggerManager.quickLog(this, "Login failed: " + result.resultReturn().message());
            return SessionToken.failure(result.resultReturn().message());
        }

        LoggerManager.quickLog(this, "Successful response received. Generating JWT...");
        JsonWebToken token = JwtService.generateToken(result.user().getId());
        LoggerManager.quickLog(this, "JWT generated. Returning token...");

        return SessionToken.success(token);
    }

    public Optional<User> getUserFromJwt(JsonWebToken token) {
        LoggerManager.quickLog(this, "Getting UUID from JWT...");
        UUID userId = null;
        try {
            userId = JwtService.validateTokenAndGetUserId(token);
        } catch (AccessDeniedException e) {
            LoggerManager.quickLog(this, "JwtService denied token!");
            return Optional.empty();
        }
        if (userId == null) {
            LoggerManager.quickLog(this, "Received null UUID from JwtService!");
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public ResultReturn addUser(User user) {
        return userRepository.insert(user);
    }
    public ResultReturn updateUser(User user) {
        return userRepository.update(user);
    }
    public ResultReturn updateUser(UUID uuid, User user) {
        return userRepository.updateById(uuid, user);
    }
    public ResultReturn deleteUser(User user) {
        return userRepository.delete(user);
    }
    public ResultReturn deleteUser(UUID uuid) {
        return userRepository.deleteById(uuid);
    }

    public boolean existsManager() {
        return userRepository.existsManager();
    }

    // In UserRepositoryService.java

    public List<User> getAllUsers() {
        LoggerManager.quickLog(this, "Getting all users");
        return userRepository.findAll();
    }
}
