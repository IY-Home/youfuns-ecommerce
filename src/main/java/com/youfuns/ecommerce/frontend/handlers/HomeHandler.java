package com.youfuns.ecommerce.frontend.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import static com.youfuns.ecommerce.frontend.WebServer.sendResponse;
import static com.youfuns.ecommerce.frontend.WebServer.setCorsHeaders;

public class HomeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        String endpoints = """
        ============================================================
                            API ENDPOINTS
        ============================================================
        
        AUTH (Public)
        ------------------------------------------------------------
        POST   /api/auth/login              Login with username/password
        POST   /api/auth/register           Register new user
        
        USER PROFILE (Authenticated)
        ------------------------------------------------------------
        GET    /api/user/profile                 Get own full profile
        GET    /api/user/profile/email           Get own email
        GET    /api/user/profile/name            Get own name
        GET    /api/user/profile/phone           Get own phone
        GET    /api/user/profile/username        Get own username
        GET    /api/user/profile/dob             Get own date of birth
        GET    /api/user/profile/picture         Get own profile picture path
        POST   /api/user/profile/email/update    Update own email
        POST   /api/user/profile/name/update     Update own name
        POST   /api/user/profile/phone/update    Update own phone
        POST   /api/user/profile/username/update Update own username
        POST   /api/user/profile/dob/update      Update own date of birth
        PUT    /api/user/profile/update          Update own profile (bulk)
        
        CUSTOMER PROFILE (Authenticated)
        ------------------------------------------------------------
        GET    /api/user/customerprofile               Get full customer profile
        GET    /api/user/customerprofile/shipping      Get shipping address
        PUT    /api/user/customerprofile/shipping      Update shipping address
        GET    /api/user/customerprofile/billing       Get billing address
        PUT    /api/user/customerprofile/billing       Update billing address
        PUT    /api/user/customerprofile/country       Update country
        PUT    /api/user/customerprofile/language      Update language
        PUT    /api/user/customerprofile/currency      Update currency
        GET    /api/admin/user/customerprofile         Get any user's customer profile (admin)
        
        USER ACCOUNT (Authenticated)
        ------------------------------------------------------------
        PUT    /api/user/password                Change password
        DELETE /api/user/delete                  Delete own account
        GET    /api/user/roles                   Get own roles
        POST   /api/user/username/add            Add username
        DELETE /api/user/username/remove         Remove username
        
        CART (Authenticated)
        ------------------------------------------------------------
        GET    /api/cart                         Get current cart
        POST   /api/cart/add                     Add product to cart
        POST   /api/cart/remove                  Remove product from cart
        PUT    /api/cart/update                  Update cart item quantity
        POST   /api/cart/clear                   Clear entire cart
        GET    /api/cart/total                   Get cart total
        
        WISHLIST (Authenticated)
        ------------------------------------------------------------
        GET    /api/wishlist                     Get wishlist
        POST   /api/wishlist/add                 Add product to wishlist
        POST   /api/wishlist/remove              Remove product from wishlist
        POST   /api/wishlist/clear               Clear wishlist
        
        ORDERS (Authenticated)
        ------------------------------------------------------------
        GET    /api/orders                       Get user orders
        GET    /api/orders?all=true              Get all orders (admin only)
        POST   /api/orders/create                Create order from cart
        PUT    /api/orders/status                Update order status (admin only)
        
        PRODUCTS (Public)
        ------------------------------------------------------------
        POST   /api/products/create              Create product (vendor)
        POST   /api/products/get                 Get product (public)
        POST   /api/products/get/vendor          Get product (vendor)
        PUT    /api/products/update              Update product (vendor)
        DELETE /api/products/delete              Delete product (vendor)
        PUT    /api/products/status              Update product status (vendor)
        PUT    /api/products/price               Update product price (vendor)
        PUT    /api/products/stock               Update product stock (vendor)
        GET    /api/products                     List all active products (public)
        GET    /api/products/search?q=query      Search products
        POST   /api/products/category            Get products by category
        
        PRODUCT IMAGES (Authenticated Vendor)
        ------------------------------------------------------------
        POST   /api/upload/product/image         Add product image (multipart/form-data)
        POST   /api/upload/product/main          Set product main image (multipart/form-data)
        POST   /api/upload/product/thumbnail     Set product thumbnail (multipart/form-data)
        
        VENDOR (Authenticated Vendor)
        ------------------------------------------------------------
        POST   /api/user/vendor/register                Register as vendor
        PUT    /api/vendor/shop/name                    Update shop name
        PUT    /api/vendor/shop/description             Update shop description
        PUT    /api/vendor/shop/category                Update shop category
        PUT    /api/vendor/shop/logo                    Update shop logo URL
        PUT    /api/vendor/shop/banner                  Update shop banner URL
        PUT    /api/vendor/shop/tagline                 Update shop tagline
        POST   /api/vendor/shop/images                  Add shop image
        DELETE /api/vendor/shop/images                  Remove shop image
        PUT    /api/vendor/shop/images                  Replace all shop images
        PUT    /api/vendor/contact/email                Update business email
        PUT    /api/vendor/contact/phone                Update business phone
        PUT    /api/vendor/contact/website              Update website URL
        PUT    /api/vendor/contact/address              Update address
        POST   /api/vendor/social                       Add social link
        DELETE /api/vendor/social                       Remove social link
        PUT    /api/vendor/business/tax                 Update tax ID
        PUT    /api/vendor/business/registration        Update business registration
        PUT    /api/vendor/business/type                Update business type
        PUT    /api/vendor/business/legal               Update legal name
        PUT    /api/vendor/business/established         Update year established
        PUT    /api/vendor/store/theme                  Update store theme
        PUT    /api/vendor/store/language               Update store language
        PUT    /api/vendor/store/currency               Update store currency
        PUT    /api/vendor/store/visibility             Update store visibility
        PUT    /api/vendor/store/returns                Update return policy
        PUT    /api/vendor/store/shipping               Update shipping policy
        
        VENDOR APPROVAL (Admin only)
        ------------------------------------------------------------
        Step 1: Activate vendor service
        Step 2: Add VENDOR role to user
        Step 3: Update repository status
        
        UPLOADS (Authenticated)
        ------------------------------------------------------------
        POST   /api/upload/user/profile/picture  Upload profile picture (multipart/form-data)
        
        ADMIN (Admin/Manager only)
        ------------------------------------------------------------
        GET    /api/admin/user/profile           Get any user's profile
        PUT    /api/admin/user/profile/update    Update any user's profile
        PUT    /api/admin/user/password          Change any user's password
        GET    /api/admin/user/customerprofile   Get any user's customer profile
        
        PUBLIC (No auth required)
        ------------------------------------------------------------
        GET    /api/public/user                  Get public vendor profile
        
        FILES
        ------------------------------------------------------------
        GET    /api/uploads/{filename}           Download uploaded file
        
        ROOT
        ------------------------------------------------------------
        GET    /                                 Show this endpoint list
        ============================================================
        """;
        sendResponse(exchange, 200, endpoints, "text/plain");
    }
}