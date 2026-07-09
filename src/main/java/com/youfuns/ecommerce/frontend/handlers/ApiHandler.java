package com.youfuns.ecommerce.frontend.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.RoleToken;
import com.youfuns.ecommerce.auth.UserRole;
import com.youfuns.ecommerce.frontend.*;
import com.youfuns.ecommerce.frontend.payloads.*;
import com.youfuns.ecommerce.frontend.utils.JsonUtils;
import com.youfuns.ecommerce.frontend.utils.ResultPayload;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.ecommerce.orders.Order;
import com.youfuns.ecommerce.products.Cart;
import com.youfuns.ecommerce.products.Product;
import com.youfuns.ecommerce.products.ProductRepositoryService;
import com.youfuns.ecommerce.products.Wishlist;
import com.youfuns.ecommerce.user.CustomerService;
import com.youfuns.ecommerce.user.User;
import com.youfuns.ecommerce.user.UserProfile;
import com.youfuns.ecommerce.vendor.VendorService;
import com.youfuns.exceptions.IllegalFieldException;
import com.youfuns.paramtypes.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.youfuns.ecommerce.frontend.WebServer.setCorsHeaders;

public class ApiHandler implements HttpHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final FrontendService frontendService;

    public ApiHandler(FrontendService frontendService) {
        this.frontendService = frontendService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        LoggerManager.quickLog(this, "Handling request...");
        setCorsHeaders(exchange);

        // Handle preflight
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            WebServer.sendResponse(exchange, 200, "", "text/plain");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        LoggerManager.quickLog(this, "Routing request to " + path + " (" + method + ")...");

        // Route to appropriate handler
        switch (path) {
            case "/api/auth/login":
                handleLogin(exchange, method);
                break;
            case "/api/auth/register":
                handleRegister(exchange, method);
                break;
            case "/api/user/profile":
                handleProfile(exchange, method);
                break;
            case "/api/user/profile/update":
                handleUpdateProfile(exchange, method);
                break;
            case "/api/user/password":
                handleChangePassword(exchange, method);
                break;
            case "/api/user/delete":
                handleDeleteAccount(exchange, method);
                break;
            case "/api/user/roles":
                handleGetRoles(exchange, method);
                break;
            case "/api/user/username/add":
                handleAddUsername(exchange, method);
                break;
            case "/api/user/username/remove":
                handleRemoveUsername(exchange, method);
                break;
            case "/api/admin/user/profile":
                handleAdminGetProfile(exchange, method);
                break;
            case "/api/admin/user/profile/update":
                handleAdminUpdateProfile(exchange, method);
                break;
            case "/api/admin/user/password":
                handleAdminChangePassword(exchange, method);
                break;
            case "/api/public/user":
                handlePublicProfile(exchange, method);
                break;
            case "/api/user/profile/email":
                handleGetEmail(exchange, method);
                break;
            case "/api/user/profile/email/update":
                handleUpdateEmail(exchange, method);
                break;
            case "/api/user/profile/name":
                handleGetName(exchange, method);
                break;
            case "/api/user/profile/name/update":
                handleUpdateName(exchange, method);
                break;
            case "/api/user/profile/phone":
                handleGetPhone(exchange, method);
                break;
            case "/api/user/profile/phone/update":
                handleUpdatePhone(exchange, method);
                break;
            case "/api/user/profile/username":
                handleGetUsername(exchange, method);
                break;
            case "/api/user/profile/username/update":
                handleUpdateUsername(exchange, method);
                break;
            case "/api/user/profile/dob":
                handleGetDateOfBirth(exchange, method);
                break;
            case "/api/user/profile/dob/update":
                handleUpdateDateOfBirth(exchange, method);
                break;
            case "/api/user/profile/picture":
                handleGetProfilePicture(exchange, method);
                break;
            // ============= CART ENDPOINTS =============
            case "/api/cart":
                handleCart(exchange, method);
                break;
            case "/api/cart/add":
                handleCartAdd(exchange, method);
                break;
            case "/api/cart/remove":
                handleCartRemove(exchange, method);
                break;
            case "/api/cart/update":
                handleCartUpdate(exchange, method);
                break;
            case "/api/cart/clear":
                handleCartClear(exchange, method);
                break;
            case "/api/cart/total":
                handleCartTotal(exchange, method);
                break;

            // ============= WISHLIST ENDPOINTS =============
            case "/api/wishlist":
                handleWishlist(exchange, method);
                break;
            case "/api/wishlist/add":
                handleWishlistAdd(exchange, method);
                break;
            case "/api/wishlist/remove":
                handleWishlistRemove(exchange, method);
                break;
            case "/api/wishlist/clear":
                handleWishlistClear(exchange, method);
                break;

            // ============= ORDER ENDPOINTS =============
            case "/api/orders":
                handleOrders(exchange, method);
                break;
            case "/api/orders/create":
                handleOrderCreate(exchange, method);
                break;
            case "/api/orders/status":
                handleOrderStatus(exchange, method);
                break;

            // ============= PRODUCT ENDPOINTS =============
            case "/api/products":
                handleProducts(exchange, method);
                break;
            case "/api/products/search":
                handleProductSearch(exchange, method);
                break;

            // ============= CUSTOMER PROFILE =============
            case "/api/user/customerprofile":
                handleCustomerProfile(exchange, method);
                break;
            case "/api/user/customerprofile/shipping":
                handleCustomerShipping(exchange, method);
                break;
            case "/api/user/customerprofile/billing":
                handleCustomerBilling(exchange, method);
                break;
            case "/api/user/customerprofile/country":
                handleCustomerCountry(exchange, method);
                break;
            case "/api/user/customerprofile/language":
                handleCustomerLanguage(exchange, method);
                break;
            case "/api/user/customerprofile/currency":
                handleCustomerCurrency(exchange, method);
                break;
            case "/api/admin/user/customerprofile":
                handleAdminCustomerProfile(exchange, method);
                break;

            case "/api/user/vendor/register":
                handleVendorRegister(exchange, method);
                break;

            case "/api/vendor/shop/name":
                handleVendorShopName(exchange, method);
                break;
            case "/api/vendor/shop/description":
                handleVendorShopDescription(exchange, method);
                break;
            case "/api/vendor/shop/category":
                handleVendorCategory(exchange, method);
                break;
            case "/api/vendor/shop/logo":
                handleVendorShopLogo(exchange, method);
                break;
            case "/api/vendor/shop/banner":
                handleVendorShopBanner(exchange, method);
                break;
            case "/api/vendor/shop/tagline":
                handleVendorShopTagline(exchange, method);
                break;
            case "/api/vendor/shop/images":
                handleVendorShopImages(exchange, method);
                break;
            case "/api/vendor/contact/email":
                handleVendorBusinessEmail(exchange, method);
                break;
            case "/api/vendor/contact/phone":
                handleVendorBusinessPhone(exchange, method);
                break;
            case "/api/vendor/contact/website":
                handleVendorWebsiteUrl(exchange, method);
                break;
            case "/api/vendor/contact/address":
                handleVendorAddress(exchange, method);
                break;
            case "/api/vendor/social":
                handleVendorSocialLink(exchange, method);
                break;
            case "/api/vendor/business/tax":
                handleVendorTaxId(exchange, method);
                break;
            case "/api/vendor/business/registration":
                handleVendorBusinessRegistration(exchange, method);
                break;
            case "/api/vendor/business/type":
                handleVendorBusinessType(exchange, method);
                break;
            case "/api/vendor/business/legal":
                handleVendorLegalName(exchange, method);
                break;
            case "/api/vendor/business/established":
                handleVendorYearEstablished(exchange, method);
                break;
            case "/api/vendor/store/theme":
                handleVendorStoreTheme(exchange, method);
                break;
            case "/api/vendor/store/language":
                handleVendorStoreLanguage(exchange, method);
                break;
            case "/api/vendor/store/currency":
                handleVendorStoreCurrency(exchange, method);
                break;
            case "/api/vendor/store/visibility":
                handleVendorStoreVisibility(exchange, method);
                break;
            case "/api/vendor/store/returns":
                handleVendorReturnPolicy(exchange, method);
                break;
            case "/api/vendor/store/shipping":
                handleVendorShippingPolicy(exchange, method);
                break;
            case "/api/vendor":
                handleGetVendorProfile(exchange, method);
                break;
            case "/api/products/create":
                handleCreateProduct(exchange, method);
                break;
            case "/api/products/get":
                handleGetProduct(exchange, method);
                break;
            case "/api/products/get/vendor":
                handleGetProductVendor(exchange, method);
                break;
            case "/api/products/update":
                handleUpdateProduct(exchange, method);
                break;
            case "/api/products/delete":
                handleDeleteProduct(exchange, method);
                break;
            case "/api/products/status":
                handleProductStatus(exchange, method);
                break;
            case "/api/products/price":
                handleProductPrice(exchange, method);
                break;
            case "/api/products/stock":
                handleProductStock(exchange, method);
                break;
            case "/api/products/category":
                handleProductCategory(exchange, method);
                break;

            // ============= ADMIN USER MANAGEMENT ENDPOINTS =============
            case "/api/admin/users":
                handleAdminUsers(exchange, method);
                break;
            case "/api/admin/user":
                handleAdminUser(exchange, method);
                break;
            case "/api/admin/user/role/admin/grant":
                handleAdminGrantRole(exchange, method, "ADMIN");
                break;
            case "/api/admin/user/role/manager/grant":
                handleAdminGrantRole(exchange, method, "MANAGER");
                break;
            case "/api/admin/user/role/admin/remove":
                handleAdminRemoveRole(exchange, method, "ADMIN");
                break;
            case "/api/admin/user/role/manager/remove":
                handleAdminRemoveRole(exchange, method, "MANAGER");
                break;
            case "/api/admin/user/disable":
                handleAdminUserDisable(exchange, method);
                break;
            case "/api/admin/user/enable":
                handleAdminUserEnable(exchange, method);
                break;
            case "/api/admin/vendor/disable":
                handleAdminVendorDisable(exchange, method);
                break;
            case "/api/admin/vendor/enable":
                handleAdminVendorEnable(exchange, method);
                break;

            default:
                LoggerManager.quickLog(this, "Unknown endpoint: " + path);
                sendResponse(exchange, 404, Map.of("error", "Not Found"));
        }
    }

    // ============= USER ENDPOINTS =============

    private void handleProfile(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        getBody(exchange); // Consume body (none expected)
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        ResultPayload<UserProfile.FullUserProfile> result = frontendService.getProfile(jwt);
        processResponse(exchange, result);
    }

    private void handleUpdateProfile(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "PUT")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            UpdateProfilePayload payload = JsonUtils.parseUpdateProfilePayload(body);
            ResultReturn result = frontendService.updateProfile(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            LoggerManager.quickLog(this, "An IOException was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            LoggerManager.quickLog(this, e.getClass().getSimpleName() + " was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "An unknown error occurred: " + e.getMessage()));
        }
    }

    private void handleChangePassword(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "PUT")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            ChangePasswordPayload payload = JsonUtils.parseChangePasswordPayload(body);
            ResultReturn result = frontendService.changePassword(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            LoggerManager.quickLog(this, "An IOException was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            LoggerManager.quickLog(this, e.getClass().getSimpleName() + " was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "An unknown error occurred: " + e.getMessage()));
        }
    }

    private void handleDeleteAccount(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "DELETE")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultReturn result = frontendService.deleteAccount(jwt);
        processResponse(exchange, result);
    }

    private void handleGetRoles(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultPayload<List<UserRole>> result = frontendService.getOwnRoles(jwt);
        processResponse(exchange, result);
    }

    private void handleAddUsername(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            AddUsernamePayload payload = JsonUtils.parseAddUsernamePayload(body);
            ResultReturn result = frontendService.addUsername(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            LoggerManager.quickLog(this, "An IOException was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            LoggerManager.quickLog(this, e.getClass().getSimpleName() + " was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "An unknown error occurred: " + e.getMessage()));
        }
    }

    private void handleRemoveUsername(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "DELETE")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            RemoveUsernamePayload payload = JsonUtils.parseRemoveUsernamePayload(body);
            ResultReturn result = frontendService.removeUsername(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            LoggerManager.quickLog(this, "An IOException was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            LoggerManager.quickLog(this, e.getClass().getSimpleName() + " was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "An unknown error occurred: " + e.getMessage()));
        }
    }

    // ============= ADMIN ENDPOINTS =============

    private void handleAdminGetProfile(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            // Expecting {"userId": "uuid-string"}
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID userId = UUID.fromString(params.get("userId"));
            ResultPayload<UserProfile.FullUserProfile> result = frontendService.getProfileAdmin(jwt, userId);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleAdminUpdateProfile(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "PUT")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            AdminUpdateProfilePayload payload = JsonUtils.parseAdminUpdateProfilePayload(body);
            ResultReturn result = frontendService.updateProfileAdmin(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            LoggerManager.quickLog(this, "An IOException was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            LoggerManager.quickLog(this, e.getClass().getSimpleName() + " was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "An unknown error occurred: " + e.getMessage()));
        }
    }

    private void handleAdminChangePassword(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "PUT")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            AdminChangePasswordPayload payload = JsonUtils.parseAdminChangePasswordPayload(body);
            ResultReturn result = frontendService.adminChangePassword(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            LoggerManager.quickLog(this, "An IOException was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            LoggerManager.quickLog(this, e.getClass().getSimpleName() + " was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "An unknown error occurred: " + e.getMessage()));
        }
    }

    // ============= PUBLIC ENDPOINTS (No Auth) =============

    private void handlePublicProfile(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID userId = UUID.fromString(params.get("userId"));
            ResultPayload<VendorService.VendorPublicProfile> result = frontendService.getPublicProfile(userId);
            processResponse(exchange, result);
        } catch (IOException e) {
            LoggerManager.quickLog(this, "An IOException was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            LoggerManager.quickLog(this, e.getClass().getSimpleName() + " was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "An unknown error occurred: " + e.getMessage()));
        }
    }

    // ============= CUSTOMER PROFILE HANDLERS =============

    private void handleCustomerProfile(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultPayload<CustomerService.CustomerFullProfile> result = frontendService.getCustomerProfile(jwt);
        processResponse(exchange, result);
    }

    private void handleCustomerShipping(HttpExchange exchange, String method) throws IOException {
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }

        if ("GET".equals(method)) {
            // Get shipping address
            ResultPayload<CustomerService.CustomerFullProfile> result = frontendService.getCustomerProfile(jwt);
            if (result.resultMessage().isSuccess() && result.payload() != null) {
                ResultPayload<Address> addressResult = new ResultPayload<>(
                        new ResultReturn(ResultReturn.Result.SUCCESS, "Shipping address fetched."),
                        result.payload().shippingAddress()
                );
                processResponse(exchange, addressResult);
            } else {
                processResponse(exchange, result);
            }
        } else if ("PUT".equals(method)) {
            try {
                String body = getBody(exchange);
                Address address = JsonUtils.parseAddress(body);
                ResultReturn result = frontendService.updateCustomerShippingAddress(jwt, address);
                processResponse(exchange, result);
            } catch (Exception e) {
                sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
            }
        } else {
            sendResponse(exchange, 405, Map.of("error", "Method Not Allowed"));
        }
    }

    private void handleCustomerBilling(HttpExchange exchange, String method) throws IOException {
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }

        if ("GET".equals(method)) {
            ResultPayload<CustomerService.CustomerFullProfile> result = frontendService.getCustomerProfile(jwt);
            if (result.resultMessage().isSuccess() && result.payload() != null) {
                ResultPayload<Address> addressResult = new ResultPayload<>(
                        new ResultReturn(ResultReturn.Result.SUCCESS, "Billing address fetched."),
                        result.payload().billingAddress()
                );
                processResponse(exchange, addressResult);
            } else {
                processResponse(exchange, result);
            }
        } else if ("PUT".equals(method)) {
            try {
                String body = getBody(exchange);
                Address address = JsonUtils.parseAddress(body);
                ResultReturn result = frontendService.updateCustomerBillingAddress(jwt, address);
                processResponse(exchange, result);
            } catch (Exception e) {
                sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
            }
        } else {
            sendResponse(exchange, 405, Map.of("error", "Method Not Allowed"));
        }
    }

    private void handleCustomerCountry(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "PUT")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            String countryCode = params.get("countryCode");
            ResultReturn result = frontendService.updateCustomerCountry(jwt, countryCode);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleCustomerLanguage(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "PUT")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            String languageCode = params.get("languageCode");
            ResultReturn result = frontendService.updateCustomerLanguage(jwt, languageCode);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleCustomerCurrency(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "PUT")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            String currencyCode = params.get("currencyCode");
            ResultReturn result = frontendService.updateCustomerCurrency(jwt, currencyCode);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleAdminCustomerProfile(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID userId = UUID.fromString(params.get("userId"));
            ResultPayload<CustomerService.CustomerFullProfile> result = frontendService.getCustomerProfileAdmin(jwt, userId);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }


    // ============= AUTH METHODS =============

    private void handleLogin(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        LoggerManager.quickLog(this, "Reading request...");
        // Read request body
        String body = new String(exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);

        try {
            // Parse JSON
            LoggerManager.quickLog(this, "Parsing request...");
            LoginUserPayload request = JsonUtils.parseLoginPayload(body);
            LoggerManager.quickLog(this, "Validating login request...");
            ResultPayload<JsonWebToken> result = frontendService.loginUser(request);
            String jsonResponse = JsonUtils.toJson(result);
            LoggerManager.quickLog(this, "Processed request; sending response...");
            WebServer.sendResponse(exchange, 200, jsonResponse, "application/json");

        } catch (IOException e) {
            LoggerManager.quickLog(this, "An IOException was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            LoggerManager.quickLog(this, e.getClass().getSimpleName() + " was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "An unknown error occurred: " + e.getMessage()));
        }
    }

    private void handleRegister(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        LoggerManager.quickLog(this, "Reading request...");
        String body = new String(exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);

        try {
            LoggerManager.quickLog(this, "Parsing request...");
            RegisterUserPayload request = JsonUtils.parseRegisterPayload(body);
            LoggerManager.quickLog(this, "Validating register request...");
            ResultReturn result = frontendService.createUser(request);
            String jsonResponse = JsonUtils.toJson(result);
            LoggerManager.quickLog(this, "Processed request; sending response...");
            WebServer.sendResponse(exchange, 200, jsonResponse, "application/json");

        } catch (IOException e) {
            LoggerManager.quickLog(this, "An IOException was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            LoggerManager.quickLog(this, e.getClass().getSimpleName() + " was encountered when parsing the request: " + e.getMessage());
            sendResponse(exchange, 400, Map.of("error", "An unknown error occurred: " + e.getMessage()));
        }
    }

    private void handleVendorRegister(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            RegisterVendorPayload payload = JsonUtils.parseRegisterVendorPayload(body);
            ResultReturn result = frontendService.registerVendor(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    // ============= GET HANDLERS =============

    private void handleGetEmail(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultPayload<EmailAddress> result = frontendService.getEmail(jwt);
        processResponse(exchange, result);
    }

    private void handleGetName(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultPayload<Name> result = frontendService.getName(jwt);
        processResponse(exchange, result);
    }

    private void handleGetPhone(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultPayload<PhoneNumber> result = frontendService.getPhone(jwt);
        processResponse(exchange, result);
    }

    private void handleGetUsername(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultPayload<Username> result = frontendService.getUsername(jwt);
        processResponse(exchange, result);
    }

    private void handleGetDateOfBirth(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultPayload<LocalDate> result = frontendService.getDateOfBirth(jwt);
        processResponse(exchange, result);
    }

    private void handleGetProfilePicture(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultPayload<FilePath> result = frontendService.getProfilePicturePath(jwt);
        processResponse(exchange, result);
    }

// ============= UPDATE HANDLERS =============

    private void handleUpdateEmail(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            // Expecting {"email": "user@example.com"}
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            EmailAddress email = new EmailAddress(params.get("email"));
            ResultReturn result = frontendService.updateEmail(jwt, email);
            processResponse(exchange, result);
        } catch (IllegalFieldException e) {
            sendResponse(exchange, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleUpdateName(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            Name name = new Name(params.get("name"));
            ResultReturn result = frontendService.updateName(jwt, name);
            processResponse(exchange, result);
        } catch (IllegalFieldException e) {
            sendResponse(exchange, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleUpdatePhone(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            PhoneNumber phone = new PhoneNumber(params.get("phone"));
            ResultReturn result = frontendService.updatePhone(jwt, phone);
            processResponse(exchange, result);
        } catch (IllegalFieldException e) {
            sendResponse(exchange, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleUpdateUsername(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            Username username = new Username(params.get("username"));
            ResultReturn result = frontendService.updateUsername(jwt, username);
            processResponse(exchange, result);
        } catch (IllegalFieldException e) {
            sendResponse(exchange, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleUpdateDateOfBirth(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            LocalDate dateOfBirth = LocalDate.parse(params.get("dateOfBirth"));
            ResultReturn result = frontendService.updateDateOfBirth(jwt, dateOfBirth);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    // ============= CART HANDLERS =============

    private void handleCart(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultPayload<Cart> result = frontendService.getCart(jwt);
        processResponse(exchange, result);
    }

    private void handleCartAdd(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            AddToCartPayload payload = JsonUtils.parseAddToCartPayload(body);
            ResultReturn result = frontendService.addToCart(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleCartRemove(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            RemoveFromCartPayload payload = JsonUtils.parseRemoveFromCartPayload(body);
            ResultReturn result = frontendService.removeFromCart(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleCartUpdate(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "PUT")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            UpdateCartQuantityPayload payload = JsonUtils.parseUpdateCartQuantityPayload(body);
            ResultReturn result = frontendService.updateCartQuantity(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleCartClear(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultReturn result = frontendService.clearCart(jwt);
        processResponse(exchange, result);
    }

    private void handleCartTotal(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultPayload<Cart> result = frontendService.getCartTotal(jwt);
        processResponse(exchange, result);
    }

// ============= WISHLIST HANDLERS =============

    private void handleWishlist(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultPayload<Wishlist> result = frontendService.getWishlist(jwt);
        processResponse(exchange, result);
    }

    private void handleWishlistAdd(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            AddToWishlistPayload payload = JsonUtils.parseAddToWishlistPayload(body);
            ResultReturn result = frontendService.addToWishlist(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleWishlistRemove(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            RemoveFromWishlistPayload payload = JsonUtils.parseRemoveFromWishlistPayload(body);
            ResultReturn result = frontendService.removeFromWishlist(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleWishlistClear(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);
        ResultReturn result = frontendService.clearWishlist(jwt);
        processResponse(exchange, result);
    }

// ============= ORDER HANDLERS =============

    private void handleOrders(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);

        // Check if admin flag is present (for listing all orders)
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.contains("all=true")) {
            ResultPayload<List<Order>> result = frontendService.listAllOrders(jwt);
            processResponse(exchange, result);
        } else {
            ResultPayload<List<Order>> result = frontendService.getUserOrders(jwt);
            processResponse(exchange, result);
        }
    }

    private void handleOrderCreate(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            CreateOrderPayload payload = JsonUtils.parseCreateOrderPayload(body);
            ResultReturn result = frontendService.createOrder(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleOrderStatus(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "PUT")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            // Expecting {"orderId": "uuid", "status": "SHIPPED"}
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID orderId = UUID.fromString(params.get("orderId"));
            UpdateOrderStatusPayload payload = new UpdateOrderStatusPayload(params.get("status"));
            ResultReturn result = frontendService.updateOrderStatus(jwt, orderId, payload);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

// ============= PRODUCT HANDLERS =============

// ============= PRODUCT HANDLERS =============

    private void handleProducts(HttpExchange exchange, String method) throws IOException {
        if ("GET".equals(method)) {
            getBody(exchange);
            ResultPayload<List<Product.PublicProduct>> result = frontendService.listAllProductsPublic();
            processResponse(exchange, result);
        } else {
            sendResponse(exchange, 405, Map.of("error", "Method Not Allowed"));
        }
    }

    private void handleCreateProduct(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            ProductCreatePayload payload = JsonUtils.parseProductCreatePayload(body);
            ResultReturn result = frontendService.createProduct(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleGetProduct(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID productId = UUID.fromString(params.get("productId"));
            ResultPayload<Product.PublicProduct> result = frontendService.getProductPublic(productId);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleGetProductVendor(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID productId = UUID.fromString(params.get("productId"));
            ResultPayload<Product> result = frontendService.getProductForVendor(jwt, productId);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleUpdateProduct(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "PUT")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            UpdateProductPayload payload = JsonUtils.parseUpdateProductPayload(body);
            ResultReturn result = frontendService.updateProduct(jwt, payload);
            processResponse(exchange, result);
        } catch (IOException e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleDeleteProduct(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "DELETE")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID productId = UUID.fromString(params.get("productId"));
            ResultReturn result = frontendService.deleteProduct(jwt, productId);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleProductStatus(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "PUT")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID productId = UUID.fromString(params.get("productId"));
            String status = params.get("status");
            ResultReturn result = frontendService.updateProductStatus(jwt, productId, status);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleProductPrice(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "PUT")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, Object> params = objectMapper.readValue(body, Map.class);
            UUID productId = UUID.fromString((String) params.get("productId"));
            BigDecimal price = new BigDecimal(params.get("price").toString());
            ResultReturn result = frontendService.updateProductPrice(jwt, productId, price);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleProductStock(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "PUT")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, Object> params = objectMapper.readValue(body, Map.class);
            UUID productId = UUID.fromString((String) params.get("productId"));
            int stockQuantity = (int) params.get("stockQuantity");
            ResultReturn result = frontendService.updateProductStock(jwt, productId, stockQuantity);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleProductSearch(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        String query = exchange.getRequestURI().getQuery();
        String searchQuery = null;
        if (query != null && query.startsWith("q=")) {
            searchQuery = query.substring(2);
        }
        getBody(exchange);
        ResultPayload<List<Product.PublicProduct>> result = frontendService.searchProductsPublic(searchQuery);
        processResponse(exchange, result);
    }

    private void handleProductCategory(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            String category = params.get("category");
            ResultPayload<List<Product.PublicProduct>> result = frontendService.getProductsByCategoryPublic(category);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }


    // ============= VENDOR HANDLERS =============

    private void handleGetVendorProfile(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        String query = exchange.getRequestURI().getQuery();
        String profileQuery = null;
        if (query != null && query.startsWith("q=")) {
            profileQuery = query.substring(2);
        }
        getBody(exchange);

        ResultPayload<VendorService.VendorPublicProfile> result = frontendService.getPublicProfile(UUID.fromString(profileQuery));
        processResponse(exchange, result);
    }

    private void handleVendorShopName(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorShopName(jwt, JsonUtils.parseUpdateVendorShopNamePayload(body)));
    }

    private void handleVendorShopDescription(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorShopDescription(jwt, JsonUtils.parseUpdateVendorShopDescriptionPayload(body)));
    }

    private void handleVendorCategory(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorCategory(jwt, JsonUtils.parseUpdateVendorCategoryPayload(body)));
    }

    private void handleVendorShopLogo(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorShopLogo(jwt, JsonUtils.parseUpdateVendorShopLogoPayload(body)));
    }

    private void handleVendorShopBanner(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorShopBanner(jwt, JsonUtils.parseUpdateVendorShopBannerPayload(body)));
    }

    private void handleVendorShopTagline(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorShopTagline(jwt, JsonUtils.parseUpdateVendorShopTaglinePayload(body)));
    }

    private void handleVendorShopImages(HttpExchange exchange, String method) throws IOException {
        if ("POST".equals(method)) {
            handleVendorUpdate(exchange, method, "POST",
                    (jwt, body) -> {
                        Map<String, String> params = objectMapper.readValue(body, Map.class);
                        return frontendService.addVendorShopImage(jwt, params.get("imageUrl"));
                    });
        } else if ("DELETE".equals(method)) {
            handleVendorUpdate(exchange, method, "DELETE",
                    (jwt, body) -> {
                        Map<String, String> params = objectMapper.readValue(body, Map.class);
                        return frontendService.removeVendorShopImage(jwt, params.get("imageUrl"));
                    });
        } else if ("PUT".equals(method)) {
            handleVendorUpdate(exchange, method, "PUT",
                    (jwt, body) -> frontendService.updateVendorShopImages(jwt, JsonUtils.parseUpdateVendorShopImagesPayload(body)));
        } else {
            sendResponse(exchange, 405, Map.of("error", "Method Not Allowed"));
        }
    }

    private void handleVendorBusinessEmail(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorBusinessEmail(jwt, JsonUtils.parseUpdateVendorBusinessEmailPayload(body)));
    }

    private void handleVendorBusinessPhone(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorBusinessPhone(jwt, JsonUtils.parseUpdateVendorBusinessPhonePayload(body)));
    }

    private void handleVendorWebsiteUrl(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorWebsiteUrl(jwt, JsonUtils.parseUpdateVendorWebsiteUrlPayload(body)));
    }

    private void handleVendorAddress(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorAddress(jwt, JsonUtils.parseUpdateVendorAddressPayload(body)));
    }

    private void handleVendorSocialLink(HttpExchange exchange, String method) throws IOException {
        if ("POST".equals(method)) {
            handleVendorUpdate(exchange, method, "POST",
                    (jwt, body) -> frontendService.addVendorSocialLink(jwt, JsonUtils.parseUpdateVendorSocialLinkPayload(body)));
        } else if ("DELETE".equals(method)) {
            handleVendorUpdate(exchange, method, "DELETE",
                    (jwt, body) -> frontendService.removeVendorSocialLink(jwt, JsonUtils.parseUpdateVendorSocialLinkPayload(body)));
        } else {
            sendResponse(exchange, 405, Map.of("error", "Method Not Allowed"));
        }
    }

    private void handleVendorTaxId(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorTaxId(jwt, JsonUtils.parseUpdateVendorTaxIdPayload(body)));
    }

    private void handleVendorBusinessRegistration(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorBusinessRegistration(jwt, JsonUtils.parseUpdateVendorBusinessRegistrationPayload(body)));
    }

    private void handleVendorBusinessType(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorBusinessType(jwt, JsonUtils.parseUpdateVendorBusinessTypePayload(body)));
    }

    private void handleVendorLegalName(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorLegalName(jwt, JsonUtils.parseUpdateVendorLegalNamePayload(body)));
    }

    private void handleVendorYearEstablished(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorYearEstablished(jwt, JsonUtils.parseUpdateVendorYearEstablishedPayload(body)));
    }

    private void handleVendorStoreTheme(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorStoreTheme(jwt, JsonUtils.parseUpdateVendorStoreThemePayload(body)));
    }

    private void handleVendorStoreLanguage(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorStoreLanguage(jwt, JsonUtils.parseUpdateVendorStoreLanguagePayload(body)));
    }

    private void handleVendorStoreCurrency(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorStoreCurrency(jwt, JsonUtils.parseUpdateVendorStoreCurrencyPayload(body)));
    }

    private void handleVendorStoreVisibility(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorStoreVisibility(jwt, JsonUtils.parseUpdateVendorStoreVisibilityPayload(body)));
    }

    private void handleVendorReturnPolicy(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorReturnPolicy(jwt, JsonUtils.parseUpdateVendorReturnPolicyPayload(body)));
    }

    private void handleVendorShippingPolicy(HttpExchange exchange, String method) throws IOException {
        handleVendorUpdate(exchange, method, "PUT",
                (jwt, body) -> frontendService.updateVendorShippingPolicy(jwt, JsonUtils.parseUpdateVendorShippingPolicyPayload(body)));
    }

// ============= VENDOR HELPER =============

    @FunctionalInterface
    private interface VendorUpdateFunction {
        ResultReturn apply(JsonWebToken jwt, String body) throws IOException;
    }

    private void handleVendorUpdate(HttpExchange exchange, String method, String targetMethod, VendorUpdateFunction func) throws IOException {
        if (!verifyMethod(exchange, method, targetMethod)) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            ResultReturn result = func.apply(jwt, body);
            processResponse(exchange, result);
        } catch (IOException e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON: " + e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

// ============= ADMIN USER MANAGEMENT HANDLERS =============

    private void handleAdminUsers(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "GET")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        getBody(exchange);

        ResultPayload<List<User>> result = frontendService.listAllUsers(jwt);

        // Convert Users to FullUserProfiles with UUID
        if (result.resultMessage().isSuccess() && result.payload() != null) {
            List<UserProfile.FullUserProfile> profiles = result.payload().stream()
                    .map(user -> {
                        RoleToken token = user.getToken();
                        return user.getUserProfile().toFullProfile(token);
                    })
                    .toList();

            ResultPayload<List<UserProfile.FullUserProfile>> convertedResult =
                    new ResultPayload<>(result.resultMessage(), profiles);
            processResponse(exchange, convertedResult);
        } else {
            // Pass through failure response
            processResponse(exchange, result);
        }
    }

    private void handleAdminUser(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID userId = UUID.fromString(params.get("userId"));

            ResultPayload<User> result = frontendService.getUserByIdAdmin(jwt, userId);

            if (result.resultMessage().isSuccess() && result.payload() != null) {
                User user = result.payload();
                RoleToken token = user.getToken();
                UserProfile.FullUserProfile profile = user.getUserProfile().toFullProfile(token);

                ResultPayload<UserProfile.FullUserProfile> convertedResult =
                        new ResultPayload<>(result.resultMessage(), profile);
                processResponse(exchange, convertedResult);
            } else {
                processResponse(exchange, result);
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleAdminGrantRole(HttpExchange exchange, String method, String roleName) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID userId = UUID.fromString(params.get("userId"));

            ResultReturn result;
            if ("ADMIN".equals(roleName)) {
                result = frontendService.grantAdminRole(jwt, userId);
            } else {
                result = frontendService.grantManagerRole(jwt, userId);
            }
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleAdminRemoveRole(HttpExchange exchange, String method, String roleName) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID userId = UUID.fromString(params.get("userId"));

            ResultReturn result;
            if ("ADMIN".equals(roleName)) {
                result = frontendService.removeAdminRole(jwt, userId);
            } else {
                result = frontendService.removeManagerRole(jwt, userId);
            }
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleAdminUserDisable(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID userId = UUID.fromString(params.get("userId"));
            ResultReturn result = frontendService.disableUser(jwt, userId);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleAdminUserEnable(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID userId = UUID.fromString(params.get("userId"));
            ResultReturn result = frontendService.enableUser(jwt, userId);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleAdminVendorDisable(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID userId = UUID.fromString(params.get("userId"));
            ResultReturn result = frontendService.disableVendor(jwt, userId);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    private void handleAdminVendorEnable(HttpExchange exchange, String method) throws IOException {
        if (!verifyMethod(exchange, method, "POST")) return;
        JsonWebToken jwt = extractJwt(exchange);
        if (jwt == null) {
            sendResponse(exchange, 401, Map.of("error", "Missing or invalid Authorization header"));
            return;
        }
        try {
            String body = getBody(exchange);
            Map<String, String> params = objectMapper.readValue(body, Map.class);
            UUID userId = UUID.fromString(params.get("userId"));
            ResultReturn result = frontendService.enableVendor(jwt, userId);
            processResponse(exchange, result);
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    // ============= HELPER METHODS =============

    private boolean verifyMethod(HttpExchange exchange, String method, String targetMethod) throws IOException {
        if (!method.equals(targetMethod)) {
            LoggerManager.quickLog(this, "Unsupported HTTP method: " + method);
            sendResponse(exchange, 405, Map.of("error", "Method Not Allowed"));
            return false;
        }
        return true;
    }

    private String getBody(HttpExchange exchange) throws IOException {
        LoggerManager.quickLog(this, "Reading request body...");
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private JsonWebToken extractJwt(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return new JsonWebToken(authHeader.substring(7));
        }
        return null;
    }

    private void processResponse(HttpExchange exchange, ResultReturn result) throws IOException {
        String jsonResponse = JsonUtils.toJson(result);
        LoggerManager.quickLog(this, "Processed request; sending response...");
        WebServer.sendResponse(exchange, 200, jsonResponse, "application/json");
    }

    private void processResponse(HttpExchange exchange, ResultPayload<?> result) throws IOException {
        String jsonResponse = JsonUtils.toJson(result);
        LoggerManager.quickLog(this, "Processed request; sending response...");
        WebServer.sendResponse(exchange, 200, jsonResponse, "application/json");
    }
    // Helper methods
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data)
            throws IOException {
        String json = objectMapper.writeValueAsString(data);
        WebServer.sendResponse(exchange, statusCode, json, "application/json");
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, Object data)
            throws IOException {
        String json = objectMapper.writeValueAsString(data);
        WebServer.sendResponse(exchange, statusCode, json, "application/json");
    }
}