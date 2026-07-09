package com.youfuns;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.ManagerBootstrapper;
import com.youfuns.ecommerce.frontend.WebServer;
import com.youfuns.utils.SimpleLogger;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        LoggerManager.quickLog(Main.class, "Starting WebServer...");
        try {
            WebServer webServer = new WebServer();
            LoggerManager.quickLog(Main.class, "WebServer started.");
            LoggerManager.quickLog(Main.class, "Bootstrapping manager...");
            if (ManagerBootstrapper.bootstrap(WebServer.getFrontendService())) {
                LoggerManager.quickLog(Main.class, "Manager created with username and password from environment variables.");
            } else {
                LoggerManager.quickLog(Main.class, "The manager could not be created.");
            }
        } catch (IOException e) {
            LoggerManager.quickLog(Main.class, "An IOException was encountered when starting the WebServer: " + e.getMessage(), SimpleLogger.Level.ERROR );
        }
    }
}
