package com.youfuns.ecommerce.frontend.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.youfuns.ecommerce.LoggerManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static com.youfuns.ecommerce.frontend.WebServer.sendResponse;
import static com.youfuns.ecommerce.frontend.WebServer.setCorsHeaders;

public class FileDownloadHandler implements HttpHandler {
    private final String UPLOAD_DIR;

    public FileDownloadHandler(String uploadDir) {
        this.UPLOAD_DIR = uploadDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 200, "", "text/plain");
            return;
        }

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, Map.of("error", "Method Not Allowed"));
            return;
        }

        String path = exchange.getRequestURI().getPath();
        // /api/uploads/product/filename.png OR /api/uploads/profile/filename.jpg
        String[] segments = path.split("/");

        // Get filename from the last segment
        String fileName = segments[segments.length - 1];

        if (fileName == null || fileName.isEmpty()) {
            sendResponse(exchange, 400, Map.of("error", "No filename provided"));
            return;
        }

        // Security: prevent path traversal
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            sendResponse(exchange, 400, Map.of("error", "Invalid filename"));
            return;
        }

        // Determine subdirectory from path (product or profile)
        String subDir = "";
        if (path.contains("/product/")) {
            subDir = "product/";
        } else if (path.contains("/profile/")) {
            subDir = "profile/";
        }

        // Full file path
        String fullPath = UPLOAD_DIR + subDir + fileName;
        File file = new File(fullPath);

        // If not found, try without subdirectory
        if (!file.exists()) {
            file = new File(UPLOAD_DIR + fileName);
        }

        if (!file.exists()) {
            LoggerManager.quickLog(this, "File not found: " + fullPath);
            sendResponse(exchange, 404, Map.of("error", "File not found"));
            return;
        }

        // Determine content type
        String mimeType = Files.probeContentType(Paths.get(file.getPath()));
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        // Send file
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.getResponseHeaders().set("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        exchange.sendResponseHeaders(200, file.length());

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = exchange.getResponseBody()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        LoggerManager.quickLog(this, "File downloaded: " + fileName);
    }
}