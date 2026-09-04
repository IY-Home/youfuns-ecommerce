package com.youfuns.cms.frontend;

import com.youfuns.logger.LoggerManager;
import com.youfuns.webserver.WebServer;

public class WebServerManager {
    private final WebServer<?, ?, ?> server;

    protected WebServerManager(int port) {
        LoggerManager.quickLog(this, "Creating WebServerManager on port " + port);
        server = WebServer.builder().port(port).build();
    }

    protected WebServer<?, ?, ?> getServer() {
        return server;
    }
}
