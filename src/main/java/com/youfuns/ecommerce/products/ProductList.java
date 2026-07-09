package com.youfuns.ecommerce.products;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.Permission;
import com.youfuns.ecommerce.auth.PermissionChecker;
import com.youfuns.ecommerce.auth.RoleToken;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.paramtypes.UuidFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class ProductList {
    protected final UUID listId;
    protected final List<Entry> entries;
    protected final LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    public record Entry(Product product, LocalDateTime addedAt, int quantity) {
        public Entry(Product product, LocalDateTime addedAt) {
            this(product, addedAt, 1);
        }
    }

    public ProductList() {
        this.listId = UUID.randomUUID();
        this.entries = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        LoggerManager.quickLog(this, "Created " + getClass().getSimpleName() + " with ID: " + UuidFormat.shortenUUID(listId));
    }

    // ============= READ OPERATIONS =============

    public List<Entry> getEntries(RoleToken rt) {
        checkPermission(rt);
        LoggerManager.quickLog(this, "Returning " + entries.size() + " entries from " + getClass().getSimpleName());
        return List.copyOf(entries);
    }

    public Optional<Entry> findEntryByProductId(RoleToken rt, UUID productId) {
        checkPermission(rt);
        return entries.stream()
                .filter(entry -> entry.product().getProductIdPublic().equals(productId))
                .findFirst();
    }

    public boolean containsProduct(RoleToken rt, UUID productId) {
        checkPermission(rt);
        return entries.stream()
                .anyMatch(entry -> entry.product().getProductIdPublic().equals(productId));
    }

    public int size(RoleToken rt) {
        checkPermission(rt);
        return entries.size();
    }

    public boolean isEmpty(RoleToken rt) {
        checkPermission(rt);
        return entries.isEmpty();
    }

    // ============= MODIFICATION OPERATIONS =============

    public ResultReturn addEntry(RoleToken rt, Product product) {
        checkPermission(rt);

        if (product == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product cannot be null.");
        }

        // Check if product already exists
        Optional<Entry> existing = findEntryByProductId(rt, product.getProductIdPublic());
        if (existing.isPresent()) {
            // Update quantity instead of adding duplicate
            Entry entry = existing.get();
            int newQuantity = entry.quantity() + 1;
            return updateQuantity(rt, product.getProductIdPublic(), newQuantity);
        }

        Entry newEntry = new Entry(product, LocalDateTime.now());
        entries.add(newEntry);
        touch();

        LoggerManager.quickLog(this, "Added product " + UuidFormat.shortenUUID(product.getProductIdPublic()) + " to " + getClass().getSimpleName());
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Product added to " + getClass().getSimpleName() + ".");
    }

    public ResultReturn addEntry(RoleToken rt, Product product, int quantity) {
        checkPermission(rt);

        if (product == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product cannot be null.");
        }
        if (quantity <= 0) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Quantity must be greater than 0.");
        }

        Optional<Entry> existing = findEntryByProductId(rt, product.getProductIdPublic());
        if (existing.isPresent()) {
            int newQuantity = existing.get().quantity() + quantity;
            return updateQuantity(rt, product.getProductIdPublic(), newQuantity);
        }

        Entry newEntry = new Entry(product, LocalDateTime.now(), quantity);
        entries.add(newEntry);
        touch();

        LoggerManager.quickLog(this, "Added " + quantity + " of product " + UuidFormat.shortenUUID(product.getProductIdPublic()) + " to " + getClass().getSimpleName());
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Product added to " + getClass().getSimpleName() + ".");
    }

    public ResultReturn removeEntry(RoleToken rt, UUID productId) {
        checkPermission(rt);

        boolean removed = entries.removeIf(entry -> entry.product().getProductIdPublic().equals(productId));
        if (!removed) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found in " + getClass().getSimpleName() + ".");
        }

        touch();
        LoggerManager.quickLog(this, "Removed product " + UuidFormat.shortenUUID(productId) + " from " + getClass().getSimpleName());
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Product removed from " + getClass().getSimpleName() + ".");
    }

    public ResultReturn updateQuantity(RoleToken rt, UUID productId, int newQuantity) {
        checkPermission(rt);

        if (newQuantity <= 0) {
            return removeEntry(rt, productId);
        }

        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if (entry.product().getProductIdPublic().equals(productId)) {
                entries.set(i, new Entry(entry.product(), entry.addedAt(), newQuantity));
                touch();
                LoggerManager.quickLog(this, "Updated quantity for product " + UuidFormat.shortenUUID(productId) + " to " + newQuantity);
                return new ResultReturn(ResultReturn.Result.SUCCESS, "Quantity updated.");
            }
        }

        return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found in " + getClass().getSimpleName() + ".");
    }

    public ResultReturn clear(RoleToken rt) {
        checkPermission(rt);

        int count = entries.size();
        entries.clear();
        touch();

        LoggerManager.quickLog(this, "Cleared " + count + " entries from " + getClass().getSimpleName());
        return new ResultReturn(ResultReturn.Result.SUCCESS, getClass().getSimpleName() + " cleared.");
    }

    // ============= QUANTITY METHODS =============

    public int getQuantity(RoleToken rt, UUID productId) {
        checkPermission(rt);

        Optional<Entry> entry = findEntryByProductId(rt, productId);
        return entry.map(Entry::quantity).orElse(0);
    }

    public int getTotalItems(RoleToken rt) {
        checkPermission(rt);

        int total = 0;
        for (Entry entry : entries) {
            total += entry.quantity();
        }
        return total;
    }

    // ============= ABSTRACT METHODS =============

    protected abstract void checkPermission(RoleToken rt);

    // ============= HELPER METHODS =============

    protected void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // ============= GETTERS =============

    public UUID getListId() {
        return listId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}