package com.youfuns.ecommerce.frontend;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.frontend.handlers.ApiHandler;

import com.youfuns.ecommerce.frontend.handlers.FileDownloadHandler;
import com.youfuns.ecommerce.frontend.handlers.FileUploadHandler;
import com.youfuns.ecommerce.frontend.handlers.HomeHandler;
import com.youfuns.ecommerce.frontend.payloads.*;
import com.youfuns.ecommerce.frontend.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class WebServer {
    private static final FrontendService FRONTEND_SERVICE = new FrontendService();
    private static final String UPLOAD_DIR = "./uploads/";
    private static final int PORT = 9377;

    public WebServer() throws IOException {
        LoggerManager.quickLog(WebServer.class, "Starting HttpServer...");
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        LoggerManager.quickLog(WebServer.class, "Created HttpServer at port " + PORT);

        LoggerManager.quickLog(WebServer.class, "Creating FrontendService...");

        // Register endpoints
        LoggerManager.quickLog(WebServer.class, "Setting up endpoint...");
        server.createContext("/", new HomeHandler());
        server.createContext("/api", new ApiHandler(FRONTEND_SERVICE));
        server.createContext("/api/upload", new FileUploadHandler(FRONTEND_SERVICE, UPLOAD_DIR));
        server.createContext("/api/uploads", new FileDownloadHandler(UPLOAD_DIR));

        // Use virtual threads or cached thread pool
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        LoggerManager.quickLog(WebServer.class, "Server started on http://localhost:" + PORT);
    }



    public static void sendResponse(HttpExchange exchange, int statusCode, String body,
                                     String contentType) throws IOException {
        LoggerManager.quickLog(WebServer.class, "Setting headers...");
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        headers.set("Access-Control-Allow-Origin", "*");

        LoggerManager.quickLog(WebServer.class, "Sending headers...");
        exchange.sendResponseHeaders(statusCode, response.length);

        try (OutputStream os = exchange.getResponseBody()) {
            LoggerManager.quickLog(WebServer.class, "Sending response...");
            os.write(response);
        }
    }

    public static void sendResponse(HttpExchange exchange, int statusCode, Object data)
            throws IOException {
        String json = JsonUtils.getMapper().writeValueAsString(data);
        WebServer.sendResponse(exchange, statusCode, json, "application/json");
    }

    public static void setCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        headers.set("Access-Control-Allow-Credentials", "true");
    }

    public static FrontendService getFrontendService() {
        return FRONTEND_SERVICE;
    }
}