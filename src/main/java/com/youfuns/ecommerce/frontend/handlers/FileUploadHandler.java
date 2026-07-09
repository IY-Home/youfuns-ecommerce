package com.youfuns.ecommerce.frontend.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.frontend.FrontendService;
import com.youfuns.ecommerce.frontend.utils.ApacheHttpExchangeContext;
import com.youfuns.ecommerce.frontend.utils.JsonUtils;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.paramtypes.JsonWebToken;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.youfuns.ecommerce.frontend.WebServer.sendResponse;
import static com.youfuns.ecommerce.frontend.WebServer.setCorsHeaders;

public class FileUploadHandler implements HttpHandler {
    private final String UPLOAD_DIR;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_REQUEST_SIZE = 10 * 1024 * 1024; // 10MB

    private final FrontendService frontendService;

    public FileUploadHandler(FrontendService frontendService, String uploadDir) {
        this.frontendService = frontendService;
        this.UPLOAD_DIR = uploadDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 200, "", "text/plain");
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendMapResponse(exchange, 405, Map.of("error", "Method Not Allowed"));
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        LoggerManager.quickLog(this, "Routing request to " + path + " (" + method + ")...");

        switch (path) {
            case "/api/upload/user/profile/picture":
                handleProfilePictureUpload(exchange);
                break;
            case "/api/upload/product/image":
                handleProductImageUpload(exchange);
                break;
            case "/api/upload/product/main":
                handleProductMainImageUpload(exchange);
                break;
            case "/api/upload/product/thumbnail":
                handleProductThumbnailUpload(exchange);
                break;
            default:
                sendMapResponse(exchange, 404, Map.of("error", "Not Found"));
        }
    }

    // ============= HANDLERS =============

    private void handleProfilePictureUpload(HttpExchange exchange) throws IOException {
        JwtAndPath uploaded = uploadFile(exchange);
        if (uploaded == null) {
            return;
        }

        ResultReturn result = frontendService.updateProfilePicture(uploaded.jwt(), uploaded.path());
        String jsonResponse = JsonUtils.toJson(result);
        sendResponse(exchange, 200, jsonResponse, "application/json");
    }

    private void handleProductImageUpload(HttpExchange exchange) throws IOException {
        JwtAndProductId uploaded = uploadFileWithProductId(exchange);
        if (uploaded == null) {
            return;
        }

        ResultReturn result = frontendService.addProductImage(
                uploaded.jwt(),
                uploaded.productId(),
                uploaded.path()
        );
        String jsonResponse = JsonUtils.toJson(result);
        sendResponse(exchange, 200, jsonResponse, "application/json");
    }

    private void handleProductMainImageUpload(HttpExchange exchange) throws IOException {
        JwtAndProductId uploaded = uploadFileWithProductId(exchange);
        if (uploaded == null) {
            return;
        }

        ResultReturn result = frontendService.setProductMainImage(
                uploaded.jwt(),
                uploaded.productId(),
                uploaded.path()
        );
        String jsonResponse = JsonUtils.toJson(result);
        sendResponse(exchange, 200, jsonResponse, "application/json");
    }

    private void handleProductThumbnailUpload(HttpExchange exchange) throws IOException {
        JwtAndProductId uploaded = uploadFileWithProductId(exchange);
        if (uploaded == null) {
            return;
        }

        ResultReturn result = frontendService.setProductThumbnail(
                uploaded.jwt(),
                uploaded.productId(),
                uploaded.path()
        );
        String jsonResponse = JsonUtils.toJson(result);
        sendResponse(exchange, 200, jsonResponse, "application/json");
    }

    // ============= UPLOAD HELPERS =============

    private JwtAndPath uploadFile(HttpExchange exchange) throws IOException {
        // Create upload directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        try {
            // Extract JWT from header
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendMapResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
                return null;
            }
            JsonWebToken jwt = new JsonWebToken(authHeader.substring(7));

            // Parse multipart request
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(MAX_FILE_SIZE);
            upload.setSizeMax(MAX_REQUEST_SIZE);

            List<FileItem> items = upload.parseRequest(new ApacheHttpExchangeContext(exchange));

            String fileName = null;
            String filePath = null;

            for (FileItem item : items) {
                if (!item.isFormField() && "file".equals(item.getFieldName())) {
                    String originalName = item.getName();
                    String extension = getFileExtension(originalName);
                    String uniqueName = UUID.randomUUID().toString() + extension;
                    filePath = UPLOAD_DIR + uniqueName;
                    fileName = "/" + filePath;

                    Path path = Paths.get(filePath);
                    Files.write(path, item.get());

                    LoggerManager.quickLog(this, "File uploaded: " + filePath);
                }
            }

            if (filePath == null) {
                sendMapResponse(exchange, 400, Map.of("error", "No file uploaded"));
                return null;
            }
            return new JwtAndPath(jwt, fileName);

        } catch (FileUploadException e) {
            LoggerManager.quickLog(this, "File upload failed: " + e.getMessage());
            sendMapResponse(exchange, 400, Map.of("error", "File upload failed: " + e.getMessage()));
            return null;
        } catch (Exception e) {
            LoggerManager.quickLog(this, "Error: " + e.getMessage());
            sendMapResponse(exchange, 500, Map.of("error", "Internal server error"));
            return null;
        }
    }

    private JwtAndProductId uploadFileWithProductId(HttpExchange exchange) throws IOException {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendMapResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
                return null;
            }
            JsonWebToken jwt = new JsonWebToken(authHeader.substring(7));

            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(MAX_FILE_SIZE);
            upload.setSizeMax(MAX_REQUEST_SIZE);

            List<FileItem> items = upload.parseRequest(new ApacheHttpExchangeContext(exchange));

            UUID productId = null;
            String filePath = null;
            String fileName = null;

            for (FileItem item : items) {
                if (item.isFormField() && "productId".equals(item.getFieldName())) {
                    productId = UUID.fromString(item.getString());
                } else if (!item.isFormField() && "file".equals(item.getFieldName())) {
                    String originalName = item.getName();
                    String extension = getFileExtension(originalName);
                    String uniqueName = UUID.randomUUID().toString() + extension;
                    filePath = UPLOAD_DIR + uniqueName;
                    fileName = "/" + filePath;

                    Path path = Paths.get(filePath);
                    Files.write(path, item.get());

                    LoggerManager.quickLog(this, "File uploaded: " + filePath);
                }
            }

            if (productId == null) {
                sendMapResponse(exchange, 400, Map.of("error", "Product ID is required"));
                return null;
            }

            if (filePath == null) {
                sendMapResponse(exchange, 400, Map.of("error", "No file uploaded"));
                return null;
            }

            return new JwtAndProductId(jwt, productId, fileName);

        } catch (FileUploadException e) {
            LoggerManager.quickLog(this, "File upload failed: " + e.getMessage());
            sendMapResponse(exchange, 400, Map.of("error", "File upload failed: " + e.getMessage()));
            return null;
        } catch (Exception e) {
            LoggerManager.quickLog(this, "Error: " + e.getMessage());
            sendMapResponse(exchange, 500, Map.of("error", "Internal server error"));
            return null;
        }
    }

    // ============= RECORDS =============

    record JwtAndPath(JsonWebToken jwt, String path) {}

    record JwtAndProductId(JsonWebToken jwt, UUID productId, String path) {}

    // ============= HELPERS =============

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot);
        }
        return ".png"; // default
    }

    private void sendMapResponse(HttpExchange exchange, int statusCode, Map<String, String> map) throws IOException {
        sendResponse(exchange, statusCode, JsonUtils.toJson(map), "application/json");
    }
}