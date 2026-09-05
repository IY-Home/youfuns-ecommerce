package com.youfuns.cms.frontend;

import com.youfuns.logger.LoggerManager;
import com.youfuns.webserver.WebServer;

import java.util.UUID;

public class WebServerManager {
    private final WebServer<?, ?, ?> server;

    protected WebServerManager(int port) {
        LoggerManager.quickLog(this, "Creating WebServerManager on port " + port);
        server = WebServer.builder()
                .port(port)
                .logger(LoggerManager.INSTANCE.getLogger())
                .build()
                .on("/id", UUID.randomUUID().toString());
    }

    protected WebServer<?, ?, ?> getServer() {
        return server;
    }
}
