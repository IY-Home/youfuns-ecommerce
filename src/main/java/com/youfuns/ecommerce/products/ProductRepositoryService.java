package com.youfuns.ecommerce.products;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.*;
import com.youfuns.ecommerce.frontend.utils.ResultPayload;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.ecommerce.frontend.payloads.ProductCreatePayload;
import com.youfuns.ecommerce.status.ProductStatus;
import com.youfuns.ecommerce.user.User;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.exceptions.IllegalFieldException;
import com.youfuns.paramtypes.JsonWebToken;
import com.youfuns.paramtypes.Sku;
import com.youfuns.paramtypes.Subcategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductRepositoryService {
    private final ProductRepository productRepository;
    private final UserRepositoryService userRepositoryService;

    public ProductRepositoryService(UserRepositoryService userRepositoryService) {
        LoggerManager.quickLog(this, "Creating ProductRepositoryService and ProductRepository...");
        this.productRepository = new ProductRepository();
        this.userRepositoryService = userRepositoryService;
        LoggerManager.quickLog(this, "ProductRepository created");
    }

    // ============= CREATE =============

    public ResultReturn createProduct(JsonWebToken jwt, ProductCreatePayload payload) {
        LoggerManager.quickLog(this, "Creating product...");

        // Validate JWT
        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        // Check vendor permission
        try {
            PermissionChecker.checkPermission(token, Permission.CREATE_PRODUCT);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions to create product.");
        }

        // Check if vendorId matches the authenticated user
        if (!user.getId().equals(payload.vendorId())) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor ID does not match authenticated user.");
        }

        // Check SKU uniqueness
        Sku sku = new Sku(payload.sku());
        if (productRepository.existsBySku(sku)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "SKU already exists.");
        }

        try {
            Product product = new Product(payload);
            return productRepository.insert(product);
        } catch (IllegalFieldException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, e.getMessage());
        }
    }

    // ============= READ =============

    public ResultPayload<Product> getProductById(UUID productId) {
        LoggerManager.quickLog(this, "Getting product by ID: " + productId);

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Product not found."),
                    null
            );
        }

        Product product = productOpt.get();
        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Product fetched successfully."),
                product
        );
    }

    public ResultPayload<Product.PublicProduct> getProductPublic(UUID productId) {
        LoggerManager.quickLog(this, "Getting product publicly: " + productId);

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Product not found."),
                    null
            );
        }

        Product product = productOpt.get();
        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Product fetched successfully."),
                product.toPublicProduct()
        );
    }

    public ResultPayload<Product> getProductForVendor(JsonWebToken jwt, UUID productId) {
        LoggerManager.quickLog(this, "Getting product for vendor: " + productId);

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."),
                    null
            );
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Product not found."),
                    null
            );
        }

        Product product = productOpt.get();

        // Check if vendor owns this product
        if (!product.getVendorIdPublic().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_PRODUCTS);
            } catch (AccessDeniedException e) {
                return new ResultPayload<>(
                        new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions."),
                        null
                );
            }
        }

        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Product fetched successfully."),
                product
        );
    }

    public ResultPayload<Product> getProductForAdmin(JsonWebToken jwt, UUID productId) {
        LoggerManager.quickLog(this, "Getting product for admin: " + productId);

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."),
                    null
            );
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        try {
            PermissionChecker.checkPermission(token, Permission.VIEW_ANY_PRODUCTS);
        } catch (AccessDeniedException e) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions."),
                    null
            );
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Product not found."),
                    null
            );
        }

        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Product fetched successfully."),
                productOpt.get()
        );
    }

    // ============= LIST =============

    public ResultPayload<List<Product.PublicProduct>> listAllProductsPublic() {
        LoggerManager.quickLog(this, "Listing all products publicly");

        List<Product> products = productRepository.findActiveProducts();
        List<Product.PublicProduct> publicProducts = products.stream()
                .map(Product::toPublicProduct)
                .toList();

        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Products fetched successfully."),
                publicProducts
        );
    }

    public ResultPayload<List<Product>> listProductsForVendor(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Listing products for vendor");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."),
                    null
            );
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        try {
            PermissionChecker.checkPermission(token, Permission.MANAGE_SELF_PRODUCTS);
        } catch (AccessDeniedException e) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions."),
                    null
            );
        }

        List<Product> products = productRepository.findByVendorId(user.getId());
        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Products fetched successfully."),
                products
        );
    }

    // ============= UPDATE =============

    public ResultReturn updateProduct(JsonWebToken jwt, UUID productId, Product updatedProduct) {
        LoggerManager.quickLog(this, "Updating product: " + productId);

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<Product> existingOpt = productRepository.findById(productId);
        if (existingOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product existing = existingOpt.get();

        // Check if vendor owns this product
        if (!existing.getVendorIdPublic().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_PRODUCTS);
            } catch (AccessDeniedException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
            }
        }

        return productRepository.update(updatedProduct);
    }

    public ResultReturn updateProductStatus(JsonWebToken jwt, UUID productId, ProductStatus.Status newStatus) {
        LoggerManager.quickLog(this, "Updating product status: " + productId + " -> " + newStatus);

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product product = productOpt.get();

        // Check if vendor owns this product
        if (!product.getVendorIdPublic().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_PRODUCTS);
            } catch (AccessDeniedException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
            }
        }

        return product.updateStatus(token, newStatus);
    }

    public ResultReturn updatePrice(JsonWebToken jwt, UUID productId, BigDecimal price) {
        LoggerManager.quickLog(this, "Updating product price: " + productId);

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product product = productOpt.get();

        // Check if vendor owns this product
        if (!product.getVendorIdPublic().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_PRODUCTS);
            } catch (AccessDeniedException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
            }
        }

        return product.setPrice(token, price);
    }

    public ResultReturn updateStock(JsonWebToken jwt, UUID productId, int stockQuantity) {
        LoggerManager.quickLog(this, "Updating product stock: " + productId);

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product product = productOpt.get();

        // Check if vendor owns this product
        if (!product.getVendorIdPublic().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_PRODUCTS);
            } catch (AccessDeniedException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
            }
        }

        return product.setStockQuantity(token, stockQuantity);
    }

    // ============= DELETE =============

    public ResultReturn deleteProduct(JsonWebToken jwt, UUID productId) {
        LoggerManager.quickLog(this, "Deleting product: " + productId);

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product product = productOpt.get();

        // Check if vendor owns this product
        if (!product.getVendorIdPublic().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_PRODUCTS);
            } catch (AccessDeniedException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
            }
        }

        return productRepository.delete(product);
    }

    // ============= SEARCH =============

    public ResultPayload<List<Product.PublicProduct>> searchProductsPublic(String query) {
        LoggerManager.quickLog(this, "Searching products publicly: " + query);

        if (query == null || query.isBlank()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Search query cannot be empty."),
                    List.of()
            );
        }

        List<Product> products = productRepository.searchByNameAndDescription(query);
        List<Product.PublicProduct> publicProducts = products.stream()
                .filter(p -> p.getStatusPublic().getStatus() == ProductStatus.Status.ACTIVE)
                .map(Product::toPublicProduct)
                .toList();

        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Search completed."),
                publicProducts
        );
    }

    public ResultPayload<List<Product.PublicProduct>> getProductsByCategoryPublic(Subcategory category) {
        LoggerManager.quickLog(this, "Getting products by category: " + category.getDisplayName());

        List<Product> products = productRepository.findByCategory(category);
        List<Product.PublicProduct> publicProducts = products.stream()
                .filter(p -> p.getStatusPublic().getStatus() == ProductStatus.Status.ACTIVE)
                .map(Product::toPublicProduct)
                .toList();

        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Products fetched by category."),
                publicProducts
        );
    }

    public ResultPayload<List<Product.PublicProduct>> getProductsByPriceRangePublic(double min, double max) {
        LoggerManager.quickLog(this, "Getting products by price range: " + min + " - " + max);

        List<Product> products = productRepository.findProductsByPriceRange(min, max);
        List<Product.PublicProduct> publicProducts = products.stream()
                .filter(p -> p.getStatusPublic().getStatus() == ProductStatus.Status.ACTIVE)
                .map(Product::toPublicProduct)
                .toList();

        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Products fetched by price range."),
                publicProducts
        );
    }

    // ============= UTILITY =============

    public boolean productExists(UUID productId) {
        return productRepository.existsById(productId);
    }

    public boolean productExistsBySku(Sku sku) {
        return productRepository.existsBySku(sku);
    }
}