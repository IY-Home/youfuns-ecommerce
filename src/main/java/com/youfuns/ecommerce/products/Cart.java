package com.youfuns.ecommerce.products;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.Permission;
import com.youfuns.ecommerce.auth.PermissionChecker;
import com.youfuns.ecommerce.auth.RoleToken;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.ecommerce.status.ProductStatus;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.paramtypes.UuidFormat;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class Cart extends ProductList {
    private final UUID userId;
    private BigDecimal totalPrice;

    public Cart(UUID userId) {
        super();
        this.userId = userId;
        this.totalPrice = BigDecimal.ZERO;
        LoggerManager.quickLog(this, "Created Cart for user: " + UuidFormat.shortenUUID(userId));
    }

    @Override
    protected void checkPermission(RoleToken rt) {
        try {
            PermissionChecker.checkPermissionWithUser(rt, this.userId, Permission.MANAGE_SELF_LISTS);
        } catch (AccessDeniedException e) {
            // Try admin permission as fallback
            PermissionChecker.checkPermission(rt, Permission.MANAGE_ANY_LISTS);
        }
    }

    // ============= CART-SPECIFIC METHODS =============

    public ResultReturn addToCart(RoleToken rt, Product product, int quantity) {
        LoggerManager.quickLog(this, "Adding product to cart: " + product.getProductIdPublic() + " x" + quantity);
        checkPermission(rt);

        // Check product availability using status
        ProductStatus.Status status = product.getStatusPublic().getStatus();

        if (status == ProductStatus.Status.OUT_OF_STOCK) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product is out of stock.");
        }

        if (status == ProductStatus.Status.INACTIVE || status == ProductStatus.Status.REMOVED) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product is currently unavailable.");
        }

        if (status != ProductStatus.Status.ACTIVE && status != ProductStatus.Status.ANNOUNCED) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product is not available for purchase.");
        }

        ResultReturn result = addEntry(rt, product, quantity);
        if (result.isSuccess()) {
            recalculateTotal();
        }
        return result;
    }
    public ResultReturn removeFromCart(RoleToken rt, UUID productId) {
        LoggerManager.quickLog(this, "Removing product from cart: " + productId);
        checkPermission(rt);

        ResultReturn result = removeEntry(rt, productId);
        if (result.isSuccess()) {
            recalculateTotal();
        }
        return result;
    }

    public ResultReturn updateCartQuantity(RoleToken rt, UUID productId, int quantity) {
        LoggerManager.quickLog(this, "Updating cart quantity: " + productId + " -> " + quantity);
        checkPermission(rt);

        if (quantity > 0) {
            Optional<Entry> existing = findEntryByProductId(rt, productId);
            if (existing.isPresent()) {
                Product product = existing.get().product();
                ProductStatus.Status status = product.getStatusPublic().getStatus();

                if (status == ProductStatus.Status.OUT_OF_STOCK) {
                    return new ResultReturn(ResultReturn.Result.FAILURE, "Product is out of stock.");
                }

                if (status == ProductStatus.Status.INACTIVE || status == ProductStatus.Status.REMOVED) {
                    return new ResultReturn(ResultReturn.Result.FAILURE, "Product is currently unavailable.");
                }

                if (status != ProductStatus.Status.ACTIVE && status != ProductStatus.Status.ANNOUNCED) {
                    return new ResultReturn(ResultReturn.Result.FAILURE, "Product is not available for purchase.");
                }
            }
        }

        ResultReturn result = updateQuantity(rt, productId, quantity);
        if (result.isSuccess()) {
            recalculateTotal();
        }
        return result;
    }

    public ResultReturn clearCart(RoleToken rt) {
        LoggerManager.quickLog(this, "Clearing cart");
        checkPermission(rt);

        ResultReturn result = clear(rt);
        if (result.isSuccess()) {
            this.totalPrice = BigDecimal.ZERO;
        }
        return result;
    }

    public BigDecimal getTotalPrice(RoleToken rt) {
        checkPermission(rt);
        return totalPrice;
    }

    public int getTotalUniqueItems(RoleToken rt) {
        checkPermission(rt);
        return size(rt);
    }

    // ============= HELPER METHODS =============

    private void recalculateTotal() {
        this.totalPrice = entries.stream()
                .map(entry -> entry.product().getPricePublic()
                        .multiply(BigDecimal.valueOf(entry.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        LoggerManager.quickLog(this, "Cart total recalculated: " + totalPrice);
    }

    // ============= CHECKOUT =============

    public ResultReturn validateCart(RoleToken rt) {
        checkPermission(rt);

        if (entries.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Cart is empty.");
        }

        for (Entry entry : entries) {
            Product product = entry.product();
            int requestedQuantity = entry.quantity();

            // Check product availability using status
            ProductStatus.Status status = product.getStatusPublic().getStatus();

            if (status == ProductStatus.Status.OUT_OF_STOCK) {
                return new ResultReturn(ResultReturn.Result.FAILURE,
                        "Product \"" + product.getNamePublic() + "\" is out of stock.");
            }

            if (status == ProductStatus.Status.INACTIVE || status == ProductStatus.Status.REMOVED) {
                return new ResultReturn(ResultReturn.Result.FAILURE,
                        "Product \"" + product.getNamePublic() + "\" is currently unavailable.");
            }

            if (status != ProductStatus.Status.ACTIVE && status != ProductStatus.Status.ANNOUNCED) {
                return new ResultReturn(ResultReturn.Result.FAILURE,
                        "Product \"" + product.getNamePublic() + "\" is not available for purchase.");
            }
        }

        return new ResultReturn(ResultReturn.Result.SUCCESS, "Cart is valid.");
    }

    // ============= GETTERS =============

    public UUID getUserId() {
        return userId;
    }
}