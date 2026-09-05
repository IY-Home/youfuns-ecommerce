package com.youfuns.cms.frontend;

import com.youfuns.auth.PermissionChecker;
import com.youfuns.cms.auth.CmsPermission;
import com.youfuns.cms.user.User;
import com.youfuns.cms.user.UserRegistrationPayload;
import com.youfuns.repo.InMemoryRepository;

import java.io.IOException;
import java.util.UUID;

public class Orchestrator {
    private final WebServerManager server;
    private final PermissionChecker<CmsPermission> permissionChecker;
    private final InMemoryRepository<UUID, User> userRepository;

    public Orchestrator(int port) {
        this.server = new WebServerManager(port);
        this.permissionChecker = new PermissionChecker<>();
        this.userRepository = new InMemoryRepository<>();
        registerEndpoints();
    }

    private void registerEndpoints() {
        server.getServer()
                .on("/register", e -> {
                    try {
                        UserRegistrationPayload payload = e.parseBodyAsJson(UserRegistrationPayload.class);
                        User user = new User(payload);
                        userRepository.insert(user.getId(), user);
                        e.send("The user was registered successfully. Name: " + user.getUserInfo().getName() + ", UUID: " + user.getId());
                    } catch (IOException e1) {
                        e.sendBadRequest(e1.getMessage());
                    }
                }).start();
    }

    public static void main(String[] args) {
        new Orchestrator(8080);
    }
}
