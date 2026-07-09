package com.youfuns.ecommerce.products;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.RoleToken;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.ecommerce.repo.InMemoryRepository;
import com.youfuns.ecommerce.status.ProductStatus;
import com.youfuns.paramtypes.Sku;
import com.youfuns.paramtypes.Subcategory;
import com.youfuns.paramtypes.UuidFormat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProductRepository implements InMemoryRepository<UUID, Product> {
    private final Map<UUID, Product> store = new ConcurrentHashMap<>();
    private final Map<UUID, List<UUID>> vendorProducts = new ConcurrentHashMap<>(); // vendorId -> productIds
    private final Map<Sku, UUID> skuIndex = new ConcurrentHashMap<>(); // sku -> productId

    // ============= CRUD OPERATIONS =============

    @Override
    public ResultReturn insert(Product product) {
        LoggerManager.quickLog(this, "Inserting product: " + UuidFormat.shortenUUID(product.getProductIdPublic()));

        if (store.containsKey(product.getProductIdPublic())) {
            LoggerManager.quickLog(this, "Insert failed - product already exists: " + UuidFormat.shortenUUID(product.getProductIdPublic()));
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product already exists.");
        }

        Sku sku = product.getSkuPublic();

        if (skuIndex.containsKey(sku)) {
            LoggerManager.quickLog(this, "Insert failed - SKU already exists: " + product.getSkuPublic().sku());
            return new ResultReturn(ResultReturn.Result.FAILURE, "SKU already exists.");
        }

        store.put(product.getProductIdPublic(), product);
        skuIndex.put(sku, product.getProductIdPublic());
        vendorProducts.computeIfAbsent(product.getVendorIdPublic(), k -> new ArrayList<>())
                .add(product.getProductIdPublic());

        LoggerManager.quickLog(this, "Product inserted successfully: " + UuidFormat.shortenUUID(product.getProductIdPublic()));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Product inserted successfully.");
    }

    @Override
    public ResultReturn update(Product product) {
        LoggerManager.quickLog(this, "Updating product: " + UuidFormat.shortenUUID(product.getProductIdPublic()));

        if (!store.containsKey(product.getProductIdPublic())) {
            LoggerManager.quickLog(this, "Update failed - product not found: " + UuidFormat.shortenUUID(product.getProductIdPublic()));
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product existing = store.get(product.getProductIdPublic());

        // Check SKU uniqueness if changed
        if (!existing.getSkuPublic().equals(product.getSkuPublic()) && skuIndex.containsKey(product.getSkuPublic())) {
            LoggerManager.quickLog(this, "Update failed - SKU already in use: " + product.getSkuPublic().sku());
            return new ResultReturn(ResultReturn.Result.FAILURE, "SKU already in use.");
        }

        // Update SKU index if changed
        if (!existing.getSkuPublic().equals(product.getSkuPublic())) {
            skuIndex.remove(existing.getSkuPublic());
            skuIndex.put(product.getSkuPublic(), product.getProductIdPublic());
        }

        // Update vendor index if changed
        if (!existing.getVendorIdPublic().equals(product.getVendorIdPublic())) {
            vendorProducts.get(existing.getVendorIdPublic()).remove(product.getProductIdPublic());
            vendorProducts.computeIfAbsent(product.getVendorIdPublic(), k -> new ArrayList<>())
                    .add(product.getProductIdPublic());
        }

        store.put(product.getProductIdPublic(), product);

        LoggerManager.quickLog(this, "Product updated successfully: " + UuidFormat.shortenUUID(product.getProductIdPublic()));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Product updated successfully.");
    }

    @Override
    public ResultReturn updateById(UUID id, Product product) {
        LoggerManager.quickLog(this, "Updating product by ID: " + UuidFormat.shortenUUID(id));
        if (!store.containsKey(id)) {
            LoggerManager.quickLog(this, "Update failed - product not found: " + UuidFormat.shortenUUID(id));
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }
        return update(product);
    }

    @Override
    public ResultReturn delete(Product product) {
        LoggerManager.quickLog(this, "Deleting product: " + UuidFormat.shortenUUID(product.getProductIdPublic()));
        return deleteById(product.getProductIdPublic());
    }

    @Override
    public ResultReturn deleteById(UUID id) {
        LoggerManager.quickLog(this, "Deleting product by ID: " + UuidFormat.shortenUUID(id));

        Product removed = store.remove(id);
        if (removed == null) {
            LoggerManager.quickLog(this, "Delete failed - product not found: " + UuidFormat.shortenUUID(id));
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        skuIndex.remove(removed.getSkuPublic());

        // Remove from vendor index
        List<UUID> vendorProductList = vendorProducts.get(removed.getVendorIdPublic());
        if (vendorProductList != null) {
            vendorProductList.remove(id);
            if (vendorProductList.isEmpty()) {
                vendorProducts.remove(removed.getVendorIdPublic());
            }
        }

        LoggerManager.quickLog(this, "Product deleted successfully: " + UuidFormat.shortenUUID(id));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Product deleted successfully.");
    }

    @Override
    public void deleteAll() {
        LoggerManager.quickLog(this, "Deleting all products");
        int count = store.size();
        store.clear();
        skuIndex.clear();
        vendorProducts.clear();
        LoggerManager.quickLog(this, "Deleted " + count + " products.");
    }

    @Override
    public int deleteAllById(List<UUID> ids) {
        LoggerManager.quickLog(this, "Deleting " + ids.size() + " products by ID");
        int deleted = 0;
        for (UUID id : ids) {
            if (deleteById(id).isSuccess()) {
                deleted++;
            }
        }
        LoggerManager.quickLog(this, "Deleted " + deleted + " out of " + ids.size() + " products.");
        return deleted;
    }

    // ============= READ OPERATIONS =============

    @Override
    public Optional<Product> findById(UUID id) {
        LoggerManager.quickLog(this, "Finding product by ID: " + UuidFormat.shortenUUID(id));
        Product product = store.get(id);
        if (product != null) {
            LoggerManager.quickLog(this, "Product found: " + UuidFormat.shortenUUID(id));
        } else {
            LoggerManager.quickLog(this, "Product not found: " + UuidFormat.shortenUUID(id));
        }
        return Optional.ofNullable(product);
    }

    @Override
    public boolean existsById(UUID id) {
        LoggerManager.quickLog(this, "Checking existence of product: " + UuidFormat.shortenUUID(id));
        boolean exists = store.containsKey(id);
        LoggerManager.quickLog(this, "Product exists: " + exists);
        return exists;
    }

    @Override
    public long count() {
        LoggerManager.quickLog(this, "Counting total products");
        long count = store.size();
        LoggerManager.quickLog(this, "Total products: " + count);
        return count;
    }

    @Override
    public List<Product> findAll() {
        LoggerManager.quickLog(this, "Finding all products");
        List<Product> products = new ArrayList<>(store.values());
        LoggerManager.quickLog(this, "Found " + products.size() + " products.");
        return products;
    }

    @Override
    public Map<UUID, Product> findAllAsMap() {
        LoggerManager.quickLog(this, "Finding all products as map");
        Map<UUID, Product> copy = new HashMap<>(store);
        LoggerManager.quickLog(this, "Returning " + copy.size() + " products as map.");
        return copy;
    }

    @Override
    public List<Product> findAllById(List<UUID> ids) {
        LoggerManager.quickLog(this, "Finding " + ids.size() + " products by ID");
        List<Product> products = ids.stream()
                .map(store::get)
                .filter(Objects::nonNull)
                .toList();
        LoggerManager.quickLog(this, "Found " + products.size() + " out of " + ids.size() + " products.");
        return products;
    }

    @Override
    public Map<UUID, Product> findAllByIdAsMap(List<UUID> ids) {
        LoggerManager.quickLog(this, "Finding " + ids.size() + " products by ID as map");
        Map<UUID, Product> result = new HashMap<>();
        for (UUID id : ids) {
            Product product = store.get(id);
            if (product != null) {
                result.put(id, product);
            }
        }
        LoggerManager.quickLog(this, "Found " + result.size() + " out of " + ids.size() + " products.");
        return result;
    }

    // ============= IN-MEMORY SPECIFIC METHODS =============

    @Override
    public List<Product> selectWhere(Predicate<? super Product> predicate) {
        LoggerManager.quickLog(this, "Selecting products with predicate");
        List<Product> result = InMemoryRepository.super.selectWhere(predicate);
        LoggerManager.quickLog(this, "Selected " + result.size() + " products matching predicate.");
        return result;
    }

    @Override
    public Map<UUID, Product> selectWhereAsMap(Predicate<? super Product> predicate) {
        LoggerManager.quickLog(this, "Selecting products with predicate as map");
        Map<UUID, Product> result = InMemoryRepository.super.selectWhereAsMap(predicate);
        LoggerManager.quickLog(this, "Selected " + result.size() + " products matching predicate as map.");
        return result;
    }

    @Override
    public int countWhere(Predicate<? super Product> predicate) {
        LoggerManager.quickLog(this, "Counting products with predicate");
        int count = InMemoryRepository.super.countWhere(predicate);
        LoggerManager.quickLog(this, "Counted " + count + " products matching predicate.");
        return count;
    }

    @Override
    public void deleteWhere(Predicate<? super Product> predicate) {
        LoggerManager.quickLog(this, "Deleting products with predicate");
        InMemoryRepository.super.deleteWhere(predicate);
        LoggerManager.quickLog(this, "Deleted products matching predicate.");
    }

    // ============= CUSTOM QUERIES =============

    public Optional<Product> findBySku(Sku sku) {
        LoggerManager.quickLog(this, "Finding product by SKU: " + sku.sku());
        UUID productId = skuIndex.get(sku);
        if (productId != null) {
            LoggerManager.quickLog(this, "Found product by SKU: " + sku.sku());
            return findById(productId);
        }
        LoggerManager.quickLog(this, "Product not found by SKU: " + sku.sku());
        return Optional.empty();
    }

    public List<Product> findByVendorId(UUID vendorId) {
        LoggerManager.quickLog(this, "Finding products by vendor ID: " + UuidFormat.shortenUUID(vendorId));
        List<UUID> productIds = vendorProducts.getOrDefault(vendorId, List.of());
        List<Product> products = productIds.stream()
                .map(store::get)
                .filter(Objects::nonNull)
                .toList();
        LoggerManager.quickLog(this, "Found " + products.size() + " products for vendor: " + UuidFormat.shortenUUID(vendorId));
        return products;
    }

    public List<Product> findByCategory(Subcategory category) {
        LoggerManager.quickLog(this, "Finding products by category: " + category.getDisplayName());
        List<Product> products = store.values().stream()
                .filter(p -> p.getCategoryPublic() == category)
                .toList();
        LoggerManager.quickLog(this, "Found " + products.size() + " products in category: " + category.getDisplayName());
        return products;
    }

    public List<Product> findActiveProducts() {
        LoggerManager.quickLog(this, "Finding active products");
        List<Product> products = store.values().stream()
                .filter(p -> p.getStatusPublic().getStatus() == ProductStatus.Status.ACTIVE)
                .toList();
        LoggerManager.quickLog(this, "Found " + products.size() + " active products.");
        return products;
    }

    public List<Product> findProductsByPriceRange(double min, double max) {
        LoggerManager.quickLog(this, "Finding products in price range: " + min + " - " + max);
        List<Product> products = store.values().stream()
                .filter(p -> {
                    double price = p.getPricePublic().doubleValue();
                    return price >= min && price <= max;
                })
                .toList();
        LoggerManager.quickLog(this, "Found " + products.size() + " products in price range.");
        return products;
    }

    public List<Product> searchByName(String query) {
        LoggerManager.quickLog(this, "Searching products by name: " + query);
        String lowerQuery = query.toLowerCase();
        List<Product> products = store.values().stream()
                .filter(p -> p.getNamePublic() != null && p.getNamePublic().toLowerCase().contains(lowerQuery))
                .toList();
        LoggerManager.quickLog(this, "Found " + products.size() + " products matching name search.");
        return products;
    }

    public List<Product> searchByNameAndDescription(String query) {
        LoggerManager.quickLog(this, "Searching products by name or description: " + query);
        String lowerQuery = query.toLowerCase();
        List<Product> products = store.values().stream()
                .filter(p -> {
                    boolean nameMatch = p.getNamePublic() != null && p.getNamePublic().toLowerCase().contains(lowerQuery);
                    boolean descMatch = p.getDescriptionPublic() != null && p.getDescriptionPublic().toLowerCase().contains(lowerQuery);
                    return nameMatch || descMatch;
                })
                .toList();
        LoggerManager.quickLog(this, "Found " + products.size() + " products matching search.");
        return products;
    }

    public long countByVendor(UUID vendorId) {
        LoggerManager.quickLog(this, "Counting products for vendor: " + UuidFormat.shortenUUID(vendorId));
        List<UUID> productIds = vendorProducts.getOrDefault(vendorId, List.of());
        return productIds.size();
    }

    public boolean existsBySku(Sku sku) {
        LoggerManager.quickLog(this, "Checking existence of SKU: " + sku.sku());
        boolean exists = skuIndex.containsKey(sku);
        LoggerManager.quickLog(this, "SKU exists: " + exists);
        return exists;
    }

    // ============= SALES & ANALYTICS =============

    public void incrementViewCount(UUID productId) {
        Product product = store.get(productId);
        if (product != null) {
            // Product will need a public method to increment view count
            // This would be handled through the ProductService
            LoggerManager.quickLog(this, "View count incremented for product: " + UuidFormat.shortenUUID(productId));
        }
    }

    public void incrementWishlistCount(UUID productId) {
        Product product = store.get(productId);
        if (product != null) {
            // Product will need a public method to increment wishlist count
            LoggerManager.quickLog(this, "Wishlist count incremented for product: " + UuidFormat.shortenUUID(productId));
        }
    }
}