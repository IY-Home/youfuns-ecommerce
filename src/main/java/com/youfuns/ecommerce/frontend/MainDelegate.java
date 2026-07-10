package com.youfuns.ecommerce.frontend;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.ManagerBootstrapper;
import com.youfuns.utils.SimpleLogger;

import java.io.IOException;

public class MainDelegate {

    public static void startServer() {
        LoggerManager.quickLog(MainDelegate.class, "Starting WebServer...");
        try {
            WebServer webServer = new WebServer();
            LoggerManager.quickLog(MainDelegate.class, "WebServer started.");
            LoggerManager.quickLog(MainDelegate.class, "Bootstrapping manager...");
            if (ManagerBootstrapper.bootstrap(WebServer.getFrontendService())) {
                LoggerManager.quickLog(MainDelegate.class, "Manager created with username and password from environment variables.");
            } else {
                LoggerManager.quickLog(MainDelegate.class, "The manager could not be created.");
            }
        } catch (IOException e) {
            LoggerManager.quickLog(MainDelegate.class, "An IOException was encountered when starting the WebServer: " + e.getMessage(), SimpleLogger.Level.ERROR );
        }
    }
}
