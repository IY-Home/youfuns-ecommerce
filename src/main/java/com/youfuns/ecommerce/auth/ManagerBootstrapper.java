package com.youfuns.ecommerce.auth;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.frontend.FrontendService;
import com.youfuns.ecommerce.frontend.WebServer;
import com.youfuns.ecommerce.frontend.payloads.RegisterUserPayload;
import com.youfuns.ecommerce.user.User;

import java.time.LocalDate;
import java.util.UUID;

public class ManagerBootstrapper {
    private static final String BOOTSTRAP_USERNAME = System.getenv("YOUFUNS_BOOTSTRAP_MANAGER_USERNAME") != null ? System.getenv("YOUFUNS_BOOTSTRAP_MANAGER_USERNAME") : "manager";
    private static final String BOOTSTRAP_PASSWORD = System.getenv("YOUFUNS_BOOTSTRAP_MANAGER_PASSWORD") != null ? System.getenv("YOUFUNS_BOOTSTRAP_MANAGER_PASSWORD") : "Manager123!";
    private static final UUID BOOTSTRAP_TOKEN = System.getenv("YOUFUNS_BOOTSTRAP_MANAGER_TOKEN") != null ? UUID.fromString(System.getenv("YOUFUNS_BOOTSTRAP_MANAGER_TOKEN")) : UUID.randomUUID();

    public static boolean bootstrap(FrontendService frontendService) {
        LoggerManager.quickLog(ManagerBootstrapper.class, "Creating bootstrap manager...");
        // Check if any user already has MANAGER role
        if (frontendService.existsManager()) {
            LoggerManager.quickLog(ManagerBootstrapper.class, "Manager already exists. Skipping bootstrap.");
            return false;
        }

        if (BOOTSTRAP_USERNAME == null || BOOTSTRAP_PASSWORD == null) {
            LoggerManager.quickLog(ManagerBootstrapper.class, "Couldn't find bootstrap username and password");
            return false;
        }
        User user = null;
        try {
            user = new User("admin@youfuns.com", BOOTSTRAP_USERNAME, BOOTSTRAP_PASSWORD);
        } catch (Exception e) {
            LoggerManager.quickLog(ManagerBootstrapper.class, "Couldn't create user (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            return false;
        }
        frontendService.insertUser(user);
        LoggerManager.quickLog(ManagerBootstrapper.class, "User created and added. Promoting user to manager...");

        return user.getRoleForBootstrap().addRoleBootstrap(BOOTSTRAP_TOKEN);
    }
}
