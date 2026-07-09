package com.youfuns.ecommerce.products;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.Permission;
import com.youfuns.ecommerce.auth.PermissionChecker;
import com.youfuns.ecommerce.auth.RoleToken;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.ecommerce.frontend.payloads.ProductCreatePayload;
import com.youfuns.ecommerce.status.ProductStatus;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.exceptions.IllegalFieldException;
import com.youfuns.paramtypes.*;
import com.youfuns.utils.ListUtils;
import com.youfuns.utils.MapUtils;
import com.youfuns.utils.SimpleLogger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Product {
    // ============= IDENTIFIERS =============
    private final UUID productId;
    private UUID vendorId;
    private final Sku sku;

    // ============= BASIC INFORMATION =============
    private String name;
    private String description;
    private String shortDescription;
    private String brand;

    // ============= CATEGORIZATION =============
    private Subcategory category;

    // ============= PRICING =============
    private Currency currency;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private BigDecimal costPrice;

    // ============= INVENTORY =============
    private int stockQuantity;
    private int lowStockThreshold;
    private boolean trackInventory;

    // ============= VARIANTS =============
    private ProductVariant variants;
    private boolean hasVariants;

    // ============= PHYSICAL ATTRIBUTES =============
    private double weight;
    private double length;
    private double width;
    private double height;
    private String weightUnit;
    private String dimensionUnit;

    // ============= MEDIA =============
    private List<String> imageUrls;
    private String mainImageUrl;
    private String thumbnailUrl;
    private List<String> videoUrls;

    // ============= STATUS =============
    private ProductStatus status;

    // ============= SHIPPING =============
    private String shippingClass;
    private boolean requiresShipping;
    private boolean requiresSpecialHandling;
    private String customsDescription;

    // ============= SALES & ANALYTICS =============
    private int totalSold;
    private int viewCount;
    private int wishlistCount;
    private int cartCount;
    private int orderCount;

    // ============= TIMESTAMPS =============
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime archivedAt;

    // ============= WARRANTY & RETURNS =============
    private String warrantyInformation;
    private boolean hasReturns;
    private int returnDays;

    // ============= DISCOUNTS & PROMOTIONS =============
    private List<Discount> activeDiscounts;
    private boolean onSale;

    // ============= CUSTOM ATTRIBUTES =============
    private Map<String, String> customAttributes;

    // ============= RESTRICTIONS =============
    private int minimumOrderQuantity;
    private int maximumOrderQuantity;
    private boolean ageRestricted;
    private int minimumAge;

    // ============= REVIEWS =============
    private final ReviewManager reviewManager;

    // ============= CONSTRUCTOR =============

    public Product(ProductCreatePayload payload) {
        this.productId = UUID.randomUUID();
        this.vendorId = payload.vendorId();
        this.sku = new Sku(payload.sku());

        this.name = payload.name();
        this.description = payload.description();
        this.shortDescription = payload.shortDescription();
        this.brand = payload.brand();

        this.category = Subcategory.fromString(payload.subcategory());

        this.currency = payload.currency() != null ? Currency.fromCode(payload.currency()) : Currency.USD;
        if (this.currency == null) this.currency = Currency.USD;
        this.price = payload.price();
        this.compareAtPrice = payload.compareAtPrice();
        this.costPrice = payload.costPrice();

        this.stockQuantity = payload.stockQuantity() != null ? payload.stockQuantity() : 0;
        this.lowStockThreshold = payload.lowStockThreshold() != null ? payload.lowStockThreshold() : 5;
        this.trackInventory = payload.trackInventory() != null ? payload.trackInventory() : true;

        this.variants = new ProductVariant();
        if (payload.variants() != null && !payload.variants().isEmpty()) {
            for (Map.Entry<String, List<String>> entry : payload.variants().entrySet()) {
                this.variants.addVariant(entry.getKey(), entry.getValue());
            }
            this.hasVariants = true;
        } else {
            this.hasVariants = false;
        }

        this.weight = payload.weight() != null ? payload.weight() : 0.0;
        this.length = payload.length() != null ? payload.length() : 0.0;
        this.width = payload.width() != null ? payload.width() : 0.0;
        this.height = payload.height() != null ? payload.height() : 0.0;
        this.weightUnit = payload.weightUnit() != null ? payload.weightUnit() : "kg";
        this.dimensionUnit = payload.dimensionUnit() != null ? payload.dimensionUnit() : "cm";

        this.imageUrls = payload.imageUrls() != null ? payload.imageUrls() : List.of();
        this.mainImageUrl = payload.mainImageUrl();
        this.thumbnailUrl = payload.thumbnailUrl();

        ProductStatus.Status initialStatus = ProductStatus.Status.DRAFT;
        if (payload.initialStatus() != null) {
            try {
                initialStatus = ProductStatus.Status.valueOf(payload.initialStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                initialStatus = ProductStatus.Status.DRAFT;
            }
        }
        this.status = new ProductStatus(this.productId, initialStatus);

        this.shippingClass = payload.shippingClass();
        this.requiresShipping = payload.requiresShipping() != null ? payload.requiresShipping() : true;
        this.requiresSpecialHandling = payload.requiresSpecialHandling() != null ? payload.requiresSpecialHandling() : false;
        this.customsDescription = payload.customsDescription();

        this.totalSold = 0;
        this.viewCount = 0;
        this.wishlistCount = 0;
        this.cartCount = 0;
        this.orderCount = 0;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = null;
        this.publishedAt = null;
        this.archivedAt = null;

        this.warrantyInformation = payload.warrantyInformation();
        this.hasReturns = payload.hasReturns() != null ? payload.hasReturns() : false;
        this.returnDays = payload.returnDays() != null ? payload.returnDays() : 30;

        this.onSale = payload.onSale() != null ? payload.onSale() : false;
        this.activeDiscounts = List.of();

        this.customAttributes = payload.customAttributes() != null ? payload.customAttributes() : Map.of();

        this.minimumOrderQuantity = payload.minimumOrderQuantity() != null ? payload.minimumOrderQuantity() : 1;
        this.maximumOrderQuantity = payload.maximumOrderQuantity() != null ? payload.maximumOrderQuantity() : 999;
        this.ageRestricted = payload.ageRestricted() != null ? payload.ageRestricted() : false;
        this.minimumAge = payload.minimumAge() != null ? payload.minimumAge() : 18;

        this.reviewManager = new ReviewManager(this.productId);

        LoggerManager.quickLog(this, "Created Product (id: " + UuidFormat.shortenUUID(productId) + ", sku: " + sku.sku() + ")");
    }

    // ============= PRIVATE HELPER METHODS =============

    private void checkReadSelf(RoleToken rt) {
        PermissionChecker.checkPermissionWithUser(rt, this.vendorId, Permission.MANAGE_SELF_PRODUCTS);
    }

    private void checkReadAdmin(RoleToken rt) {
        PermissionChecker.checkPermission(rt, Permission.MANAGE_ANY_PRODUCTS);
    }

    private void checkWriteSelf(RoleToken rt) {
        PermissionChecker.checkPermissionWithUser(rt, this.vendorId, Permission.MANAGE_SELF_PRODUCTS);
    }

    private void checkWriteAdmin(RoleToken rt) {
        PermissionChecker.checkPermission(rt, Permission.MANAGE_ANY_PRODUCTS);
    }

    // ============= GETTERS - VENDOR READ (Requires VIEW_SELF_USER) =============

    public UUID getProductId(RoleToken rt) {
        checkReadSelf(rt);
        return productId;
    }

    public UUID getVendorId(RoleToken rt) {
        checkReadSelf(rt);
        return vendorId;
    }

    public Sku getSku(RoleToken rt) {
        checkReadSelf(rt);
        return sku;
    }

    public String getName(RoleToken rt) {
        checkReadSelf(rt);
        return name;
    }

    public String getDescription(RoleToken rt) {
        checkReadSelf(rt);
        return description;
    }

    public String getShortDescription(RoleToken rt) {
        checkReadSelf(rt);
        return shortDescription;
    }

    public String getBrand(RoleToken rt) {
        checkReadSelf(rt);
        return brand;
    }

    public Subcategory getCategory(RoleToken rt) {
        checkReadSelf(rt);
        return category;
    }

    public Currency getCurrency(RoleToken rt) {
        checkReadSelf(rt);
        return currency;
    }

    public BigDecimal getPrice(RoleToken rt) {
        checkReadSelf(rt);
        return price;
    }

    public BigDecimal getCompareAtPrice(RoleToken rt) {
        checkReadSelf(rt);
        return compareAtPrice;
    }

    public BigDecimal getCostPrice(RoleToken rt) {
        checkReadSelf(rt);
        return costPrice;
    }

    public int getStockQuantity(RoleToken rt) {
        checkReadSelf(rt);
        return stockQuantity;
    }

    public int getLowStockThreshold(RoleToken rt) {
        checkReadSelf(rt);
        return lowStockThreshold;
    }

    public boolean isTrackInventory(RoleToken rt) {
        checkReadSelf(rt);
        return trackInventory;
    }

    public ProductVariant getVariants(RoleToken rt) {
        checkReadSelf(rt);
        return variants;
    }

    public boolean hasVariants(RoleToken rt) {
        checkReadSelf(rt);
        return hasVariants;
    }

    public double getWeight(RoleToken rt) {
        checkReadSelf(rt);
        return weight;
    }

    public double getLength(RoleToken rt) {
        checkReadSelf(rt);
        return length;
    }

    public double getWidth(RoleToken rt) {
        checkReadSelf(rt);
        return width;
    }

    public double getHeight(RoleToken rt) {
        checkReadSelf(rt);
        return height;
    }

    public String getWeightUnit(RoleToken rt) {
        checkReadSelf(rt);
        return weightUnit;
    }

    public String getDimensionUnit(RoleToken rt) {
        checkReadSelf(rt);
        return dimensionUnit;
    }

    public List<String> getImageUrls(RoleToken rt) {
        checkReadSelf(rt);
        return imageUrls;
    }

    public String getMainImageUrl(RoleToken rt) {
        checkReadSelf(rt);
        return mainImageUrl;
    }

    public String getThumbnailUrl(RoleToken rt) {
        checkReadSelf(rt);
        return thumbnailUrl;
    }

    public ProductStatus getStatus(RoleToken rt) {
        checkReadSelf(rt);
        return status;
    }

    public String getShippingClass(RoleToken rt) {
        checkReadSelf(rt);
        return shippingClass;
    }

    public boolean isRequiresShipping(RoleToken rt) {
        checkReadSelf(rt);
        return requiresShipping;
    }

    public boolean isRequiresSpecialHandling(RoleToken rt) {
        checkReadSelf(rt);
        return requiresSpecialHandling;
    }

    public String getCustomsDescription(RoleToken rt) {
        checkReadSelf(rt);
        return customsDescription;
    }

    public int getTotalSold(RoleToken rt) {
        checkReadSelf(rt);
        return totalSold;
    }

    public int getViewCount(RoleToken rt) {
        checkReadSelf(rt);
        return viewCount;
    }

    public int getWishlistCount(RoleToken rt) {
        checkReadSelf(rt);
        return wishlistCount;
    }

    public int getCartCount(RoleToken rt) {
        checkReadSelf(rt);
        return cartCount;
    }

    public int getOrderCount(RoleToken rt) {
        checkReadSelf(rt);
        return orderCount;
    }

    public LocalDateTime getCreatedAt(RoleToken rt) {
        checkReadSelf(rt);
        return createdAt;
    }

    public LocalDateTime getUpdatedAt(RoleToken rt) {
        checkReadSelf(rt);
        return updatedAt;
    }

    public LocalDateTime getPublishedAt(RoleToken rt) {
        checkReadSelf(rt);
        return publishedAt;
    }

    public LocalDateTime getArchivedAt(RoleToken rt) {
        checkReadSelf(rt);
        return archivedAt;
    }

    public String getWarrantyInformation(RoleToken rt) {
        checkReadSelf(rt);
        return warrantyInformation;
    }

    public boolean isHasReturns(RoleToken rt) {
        checkReadSelf(rt);
        return hasReturns;
    }

    public int getReturnDays(RoleToken rt) {
        checkReadSelf(rt);
        return returnDays;
    }

    public List<Discount> getActiveDiscounts(RoleToken rt) {
        checkReadSelf(rt);
        return activeDiscounts;
    }

    public boolean isOnSale(RoleToken rt) {
        checkReadSelf(rt);
        return onSale;
    }

    public Map<String, String> getCustomAttributes(RoleToken rt) {
        checkReadSelf(rt);
        return customAttributes;
    }

    public int getMinimumOrderQuantity(RoleToken rt) {
        checkReadSelf(rt);
        return minimumOrderQuantity;
    }

    public int getMaximumOrderQuantity(RoleToken rt) {
        checkReadSelf(rt);
        return maximumOrderQuantity;
    }

    public boolean isAgeRestricted(RoleToken rt) {
        checkReadSelf(rt);
        return ageRestricted;
    }

    public int getMinimumAge(RoleToken rt) {
        checkReadSelf(rt);
        return minimumAge;
    }

    // ============= GETTERS - ADMIN READ (Requires VIEW_ANY_PRODUCTS) =============

    public UUID getProductIdAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return productId;
    }

    public UUID getVendorIdAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return vendorId;
    }

    public Sku getSkuAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return sku;
    }

    public String getNameAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return name;
    }

    public String getDescriptionAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return description;
    }

    public String getShortDescriptionAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return shortDescription;
    }

    public String getBrandAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return brand;
    }

    public Subcategory getCategoryAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return category;
    }

    public Currency getCurrencyAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return currency;
    }

    public BigDecimal getPriceAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return price;
    }

    public BigDecimal getCompareAtPriceAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return compareAtPrice;
    }

    public BigDecimal getCostPriceAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return costPrice;
    }

    public int getStockQuantityAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return stockQuantity;
    }

    public int getLowStockThresholdAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return lowStockThreshold;
    }

    public boolean isTrackInventoryAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return trackInventory;
    }

    public ProductVariant getVariantsAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return variants;
    }

    public boolean hasVariantsAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return hasVariants;
    }

    public double getWeightAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return weight;
    }

    public double getLengthAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return length;
    }

    public double getWidthAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return width;
    }

    public double getHeightAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return height;
    }

    public String getWeightUnitAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return weightUnit;
    }

    public String getDimensionUnitAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return dimensionUnit;
    }

    public List<String> getImageUrlsAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return imageUrls;
    }

    public String getMainImageUrlAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return mainImageUrl;
    }

    public String getThumbnailUrlAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return thumbnailUrl;
    }

    public ProductStatus getStatusAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return status;
    }

    public String getShippingClassAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return shippingClass;
    }

    public boolean isRequiresShippingAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return requiresShipping;
    }

    public boolean isRequiresSpecialHandlingAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return requiresSpecialHandling;
    }

    public String getCustomsDescriptionAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return customsDescription;
    }

    public int getTotalSoldAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return totalSold;
    }

    public int getViewCountAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return viewCount;
    }

    public int getWishlistCountAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return wishlistCount;
    }

    public int getCartCountAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return cartCount;
    }

    public int getOrderCountAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return orderCount;
    }

    public LocalDateTime getCreatedAtAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return createdAt;
    }

    public LocalDateTime getUpdatedAtAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return updatedAt;
    }

    public LocalDateTime getPublishedAtAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return publishedAt;
    }

    public LocalDateTime getArchivedAtAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return archivedAt;
    }

    public String getWarrantyInformationAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return warrantyInformation;
    }

    public boolean isHasReturnsAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return hasReturns;
    }

    public int getReturnDaysAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return returnDays;
    }

    public List<Discount> getActiveDiscountsAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return activeDiscounts;
    }

    public boolean isOnSaleAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return onSale;
    }

    public Map<String, String> getCustomAttributesAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return customAttributes;
    }

    public int getMinimumOrderQuantityAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return minimumOrderQuantity;
    }

    public int getMaximumOrderQuantityAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return maximumOrderQuantity;
    }

    public boolean isAgeRestrictedAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return ageRestricted;
    }

    public int getMinimumAgeAdmin(RoleToken rt) {
        checkReadAdmin(rt);
        return minimumAge;
    }


    // ============= GETTERS - PUBLIC (Only non-sensitive values) =============

    public UUID getProductIdPublic() {
        return productId;
    }

    public UUID getVendorIdPublic() {
        return vendorId;
    }

    public String getNamePublic() {
        return name;
    }

    public Sku getSkuPublic() {
        return sku;
    }

    public String getDescriptionPublic() {
        return description;
    }

    public String getShortDescriptionPublic() {
        return shortDescription;
    }

    public String getBrandPublic() {
        return brand;
    }

    public Subcategory getCategoryPublic() {
        return category;
    }

    public Currency getCurrencyPublic() {
        return currency;
    }

    public BigDecimal getPricePublic() {
        return price;
    }

    public BigDecimal getCompareAtPricePublic() {
        return compareAtPrice;
    }

    public ProductVariant getVariantsPublic() {
        return variants;
    }

    public boolean hasVariantsPublic() {
        return hasVariants;
    }

    public double getWeightPublic() {
        return weight;
    }

    public double getLengthPublic() {
        return length;
    }

    public double getWidthPublic() {
        return width;
    }

    public double getHeightPublic() {
        return height;
    }

    public String getWeightUnitPublic() {
        return weightUnit;
    }

    public String getDimensionUnitPublic() {
        return dimensionUnit;
    }

    public List<String> getImageUrlsPublic() {
        return imageUrls;
    }

    public String getMainImageUrlPublic() {
        return mainImageUrl;
    }

    public String getThumbnailUrlPublic() {
        return thumbnailUrl;
    }

    public ProductStatus getStatusPublic() {
        return status;
    }

    public boolean isRequiresShippingPublic() {
        return requiresShipping;
    }


    public String getWarrantyInformationPublic() {
        return warrantyInformation;
    }

    public boolean isHasReturnsPublic() {
        return hasReturns;
    }

    public int getReturnDaysPublic() {
        return returnDays;
    }

    public List<Discount> getActiveDiscountsPublic() {
        return activeDiscounts;
    }

    public boolean isOnSalePublic() {
        return onSale;
    }

    public Map<String, String> getCustomAttributesPublic() {
        return customAttributes;
    }

    public int getMinimumOrderQuantityPublic() {
        return minimumOrderQuantity;
    }

    public int getMaximumOrderQuantityPublic() {
        return maximumOrderQuantity;
    }

    public boolean isAgeRestrictedPublic() {
        return ageRestricted;
    }

    public int getMinimumAgePublic() {
        return minimumAge;
    }

    // ============= GETTERS - PUBLIC READ (No token required) =============

    public PublicProduct toPublicProduct() {
        return new PublicProduct(
                productId,
                vendorId,
                sku,
                name,
                shortDescription,
                brand,
                category,
                currency,
                price,
                compareAtPrice,
                stockQuantity > 0,
                mainImageUrl,
                thumbnailUrl,
                status.getStatus(),
                onSale,
                calculateAverageRating(),
                imageUrls,
                variants,
                hasVariants,
                weight,
                length,
                width,
                height,
                weightUnit,
                dimensionUnit,
                requiresShipping,
                warrantyInformation,
                hasReturns,
                returnDays,
                minimumOrderQuantity,
                maximumOrderQuantity,
                ageRestricted,
                minimumAge,
                customAttributes,
                costPrice,
                stockQuantity
        );
    }

    // ============= SETTERS - VENDOR WRITE (Requires MANAGE_SELF_PRODUCTS) =============

    public ResultReturn setName(RoleToken rt, String name) {
        LoggerManager.quickLog(this, "Setting name...");
        checkWriteSelf(rt);
        this.name = name;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Name has been set.");
    }

    public ResultReturn setDescription(RoleToken rt, String description) {
        LoggerManager.quickLog(this, "Setting description...");
        checkWriteSelf(rt);
        this.description = description;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Description has been set.");
    }

    public ResultReturn setShortDescription(RoleToken rt, String shortDescription) {
        LoggerManager.quickLog(this, "Setting short description...");
        checkWriteSelf(rt);
        this.shortDescription = shortDescription;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Short description has been set.");
    }

    public ResultReturn setBrand(RoleToken rt, String brand) {
        LoggerManager.quickLog(this, "Setting brand...");
        checkWriteSelf(rt);
        this.brand = brand;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Brand has been set.");
    }

    public ResultReturn setCategory(RoleToken rt, Subcategory category) {
        LoggerManager.quickLog(this, "Setting category...");
        checkWriteSelf(rt);
        this.category = category;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Category has been set.");
    }

    public ResultReturn setCurrency(RoleToken rt, Currency currency) {
        LoggerManager.quickLog(this, "Setting currency...");
        checkWriteSelf(rt);
        this.currency = currency;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Currency has been set.");
    }

    public ResultReturn setPrice(RoleToken rt, BigDecimal price) {
        LoggerManager.quickLog(this, "Setting price...");
        checkWriteSelf(rt);
        this.price = price;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Price has been set.");
    }

    public ResultReturn setCompareAtPrice(RoleToken rt, BigDecimal compareAtPrice) {
        LoggerManager.quickLog(this, "Setting compare at price...");
        checkWriteSelf(rt);
        this.compareAtPrice = compareAtPrice;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Compare at price has been set.");
    }

    public ResultReturn setCostPrice(RoleToken rt, BigDecimal costPrice) {
        LoggerManager.quickLog(this, "Setting cost price...");
        checkWriteSelf(rt);
        this.costPrice = costPrice;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Cost price has been set.");
    }

    public ResultReturn setStockQuantity(RoleToken rt, int stockQuantity) {
        LoggerManager.quickLog(this, "Setting stock quantity...");
        checkWriteSelf(rt);
        this.stockQuantity = stockQuantity;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Stock quantity has been set.");
    }

    public ResultReturn setLowStockThreshold(RoleToken rt, int lowStockThreshold) {
        LoggerManager.quickLog(this, "Setting low stock threshold...");
        checkWriteSelf(rt);
        this.lowStockThreshold = lowStockThreshold;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Low stock threshold has been set.");
    }

    public ResultReturn setTrackInventory(RoleToken rt, boolean trackInventory) {
        LoggerManager.quickLog(this, "Setting track inventory...");
        checkWriteSelf(rt);
        this.trackInventory = trackInventory;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Track inventory has been set.");
    }

    public ResultReturn setVariants(RoleToken rt, ProductVariant variants) {
        LoggerManager.quickLog(this, "Setting variants...");
        checkWriteSelf(rt);
        this.variants = variants;
        this.hasVariants = variants != null && !variants.isEmpty();
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Variants have been set.");
    }

    public ResultReturn setWeight(RoleToken rt, double weight) {
        LoggerManager.quickLog(this, "Setting weight...");
        checkWriteSelf(rt);
        this.weight = weight;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Weight has been set.");
    }

    public ResultReturn setDimensions(RoleToken rt, double length, double width, double height) {
        LoggerManager.quickLog(this, "Setting dimensions...");
        checkWriteSelf(rt);
        this.length = length;
        this.width = width;
        this.height = height;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Dimensions have been set.");
    }

    public ResultReturn setWeightUnit(RoleToken rt, String weightUnit) {
        LoggerManager.quickLog(this, "Setting weight unit...");
        checkWriteSelf(rt);
        this.weightUnit = weightUnit;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Weight unit has been set.");
    }

    public ResultReturn setDimensionUnit(RoleToken rt, String dimensionUnit) {
        LoggerManager.quickLog(this, "Setting dimension unit...");
        checkWriteSelf(rt);
        this.dimensionUnit = dimensionUnit;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Dimension unit has been set.");
    }

    public ResultReturn addImage(RoleToken rt, String imageUrl) {
        LoggerManager.quickLog(this, "Adding image...");
        checkWriteSelf(rt);
        this.imageUrls = ListUtils.addToList(this.imageUrls, imageUrl);
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Image has been added.");
    }

    public ResultReturn removeImage(RoleToken rt, String imageUrl) {
        LoggerManager.quickLog(this, "Removing image...");
        checkWriteSelf(rt);
        this.imageUrls = ListUtils.removeFromList(this.imageUrls, imageUrl);
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Image has been removed.");
    }

    public ResultReturn setMainImageUrl(RoleToken rt, String mainImageUrl) {
        LoggerManager.quickLog(this, "Setting main image...");
        checkWriteSelf(rt);
        this.mainImageUrl = mainImageUrl;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Main image has been set.");
    }

    public ResultReturn setThumbnailUrl(RoleToken rt, String thumbnailUrl) {
        LoggerManager.quickLog(this, "Setting thumbnail...");
        checkWriteSelf(rt);
        this.thumbnailUrl = thumbnailUrl;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Thumbnail has been set.");
    }

    public ResultReturn updateStatus(RoleToken rt, ProductStatus.Status newStatus) {
        LoggerManager.quickLog(this, "Updating status to: " + newStatus);
        checkWriteSelf(rt);
        this.status.updateStatus(newStatus);
        if (newStatus == ProductStatus.Status.ACTIVE && publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
        if (newStatus == ProductStatus.Status.REMOVED) {
            this.archivedAt = LocalDateTime.now();
        }
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Status has been updated.");
    }

    public ResultReturn setShippingClass(RoleToken rt, String shippingClass) {
        LoggerManager.quickLog(this, "Setting shipping class...");
        checkWriteSelf(rt);
        this.shippingClass = shippingClass;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shipping class has been set.");
    }

    public ResultReturn setRequiresShipping(RoleToken rt, boolean requiresShipping) {
        LoggerManager.quickLog(this, "Setting requires shipping...");
        checkWriteSelf(rt);
        this.requiresShipping = requiresShipping;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Requires shipping has been set.");
    }

    public ResultReturn setRequiresSpecialHandling(RoleToken rt, boolean requiresSpecialHandling) {
        LoggerManager.quickLog(this, "Setting requires special handling...");
        checkWriteSelf(rt);
        this.requiresSpecialHandling = requiresSpecialHandling;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Requires special handling has been set.");
    }

    public ResultReturn setCustomsDescription(RoleToken rt, String customsDescription) {
        LoggerManager.quickLog(this, "Setting customs description...");
        checkWriteSelf(rt);
        this.customsDescription = customsDescription;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Customs description has been set.");
    }

    public ResultReturn setWarrantyInformation(RoleToken rt, String warrantyInformation) {
        LoggerManager.quickLog(this, "Setting warranty information...");
        checkWriteSelf(rt);
        this.warrantyInformation = warrantyInformation;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Warranty information has been set.");
    }

    public ResultReturn setHasReturns(RoleToken rt, boolean hasReturns) {
        LoggerManager.quickLog(this, "Setting has returns...");
        checkWriteSelf(rt);
        this.hasReturns = hasReturns;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Has returns has been set.");
    }

    public ResultReturn setReturnDays(RoleToken rt, int returnDays) {
        LoggerManager.quickLog(this, "Setting return days...");
        checkWriteSelf(rt);
        this.returnDays = returnDays;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Return days have been set.");
    }

    public ResultReturn setOnSale(RoleToken rt, boolean onSale) {
        LoggerManager.quickLog(this, "Setting on sale...");
        checkWriteSelf(rt);
        this.onSale = onSale;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "On sale has been set.");
    }

    public ResultReturn setCustomAttribute(RoleToken rt, String key, String value) {
        LoggerManager.quickLog(this, "Setting custom attribute: " + key);
        checkWriteSelf(rt);
        this.customAttributes = MapUtils.put(this.customAttributes, key, value);
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Custom attribute has been set.");
    }

    public ResultReturn removeCustomAttribute(RoleToken rt, String key) {
        LoggerManager.quickLog(this, "Removing custom attribute: " + key);
        checkWriteSelf(rt);
        this.customAttributes = MapUtils.remove(this.customAttributes, key);
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Custom attribute has been removed.");
    }

    public ResultReturn setMinimumOrderQuantity(RoleToken rt, int minimumOrderQuantity) {
        LoggerManager.quickLog(this, "Setting minimum order quantity...");
        checkWriteSelf(rt);
        this.minimumOrderQuantity = minimumOrderQuantity;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Minimum order quantity has been set.");
    }

    public ResultReturn setMaximumOrderQuantity(RoleToken rt, int maximumOrderQuantity) {
        LoggerManager.quickLog(this, "Setting maximum order quantity...");
        checkWriteSelf(rt);
        this.maximumOrderQuantity = maximumOrderQuantity;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Maximum order quantity has been set.");
    }

    public ResultReturn setAgeRestricted(RoleToken rt, boolean ageRestricted) {
        LoggerManager.quickLog(this, "Setting age restricted...");
        checkWriteSelf(rt);
        this.ageRestricted = ageRestricted;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Age restricted has been set.");
    }

    public ResultReturn setMinimumAge(RoleToken rt, int minimumAge) {
        LoggerManager.quickLog(this, "Setting minimum age...");
        checkWriteSelf(rt);
        this.minimumAge = minimumAge;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Minimum age has been set.");
    }

    // ============= SETTERS - ADMIN WRITE (Requires MANAGE_ANY_PRODUCTS) =============

    public ResultReturn setVendorIdAdmin(RoleToken rt, UUID vendorId) {
        LoggerManager.quickLog(this, "Setting vendor ID by admin...");
        checkWriteAdmin(rt);
        this.vendorId = vendorId;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Vendor ID has been set.");
    }

    public ResultReturn setNameAdmin(RoleToken rt, String name) {
        LoggerManager.quickLog(this, "Setting name by admin...");
        checkWriteAdmin(rt);
        this.name = name;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Name has been set.");
    }

    public ResultReturn setDescriptionAdmin(RoleToken rt, String description) {
        LoggerManager.quickLog(this, "Setting description by admin...");
        checkWriteAdmin(rt);
        this.description = description;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Description has been set.");
    }

    public ResultReturn setPriceAdmin(RoleToken rt, BigDecimal price) {
        LoggerManager.quickLog(this, "Setting price by admin...");
        checkWriteAdmin(rt);
        this.price = price;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Price has been set.");
    }

    public ResultReturn setStockQuantityAdmin(RoleToken rt, int stockQuantity) {
        LoggerManager.quickLog(this, "Setting stock quantity by admin...");
        checkWriteAdmin(rt);
        this.stockQuantity = stockQuantity;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Stock quantity has been set.");
    }

    public ResultReturn setStatusAdmin(RoleToken rt, ProductStatus.Status newStatus) {
        LoggerManager.quickLog(this, "Setting status by admin to: " + newStatus);
        checkWriteAdmin(rt);
        this.status.updateStatus(newStatus);
        if (newStatus == ProductStatus.Status.ACTIVE && publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
        if (newStatus == ProductStatus.Status.REMOVED) {
            this.archivedAt = LocalDateTime.now();
        }
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Status has been set.");
    }

    public ResultReturn setOnSaleAdmin(RoleToken rt, boolean onSale) {
        LoggerManager.quickLog(this, "Setting on sale by admin...");
        checkWriteAdmin(rt);
        this.onSale = onSale;
        touch();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "On sale has been set.");
    }

    // ============= FULL READ METHODS =============

    public Product readSelf(RoleToken rt) {
        LoggerManager.quickLog(this, "Reading full product with self permission...");
        checkReadSelf(rt);
        return this;
    }

    public Product readAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Reading full product with admin permission...");
        return this;
    }

    // ============= HELPER METHODS =============

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    private double calculateAverageRating() {
        return reviewManager.getAverageRating();
    }

    private double averageRating() {
        return calculateAverageRating();
    }


    public double getAverageRatingPublic() {
        return reviewManager.getAverageRating();
    }

    public int getReviewCountPublic() {
        return reviewManager.getReviewCount();
    }

    public List<Review> getReviewsPublic() {
        return reviewManager.getReviews();
    }

    public List<Review> getRecentReviewsPublic(int limit) {
        return reviewManager.getRecentReviews(limit);
    }

// ============= REVIEW OPERATIONS (Require Token) =============

    public ResultReturn addReview(RoleToken rt, UUID userId, Rating rating, String reviewText) {
        checkWriteSelf(rt);
        return reviewManager.addReview(rt, userId, rating, reviewText);
    }

    public ResultReturn removeReview(RoleToken rt, UUID userId) {
        checkWriteSelf(rt);
        return reviewManager.removeReview(rt, userId);
    }

    public ResultReturn removeReviewAdmin(RoleToken rt, UUID userId) {
        checkWriteAdmin(rt);
        return reviewManager.removeReviewAdmin(rt, userId);
    }

    public ResultReturn updateReview(RoleToken rt, UUID userId, Rating newRating, String newReviewText) {
        checkWriteSelf(rt);
        return reviewManager.updateReview(rt, userId, newRating, newReviewText);
    }

    public boolean hasUserReviewed(RoleToken rt, UUID userId) {
        checkReadSelf(rt);
        return reviewManager.hasUserReviewed(userId);
    }


    // ============= RECORDS FOR DIFFERENT VIEWS =============

    public record PublicProduct(
            UUID productId,
            UUID vendorId,
            Sku sku,
            String name,
            String shortDescription,
            String brand,
            Subcategory category,
            Currency currency,
            BigDecimal price,
            BigDecimal compareAtPrice,
            boolean inStock,
            String mainImageUrl,
            String thumbnailUrl,
            ProductStatus.Status status,
            boolean onSale,
            double averageRating,
            List<String> imageUrls,
            ProductVariant variants,
            boolean hasVariants,
            double weight,
            double length,
            double width,
            double height,
            String weightUnit,
            String dimensionUnit,
            boolean requiresShipping,
            String warrantyInformation,
            boolean hasReturns,
            int returnDays,
            int minimumOrderQuantity,
            int maximumOrderQuantity,
            boolean ageRestricted,
            int minimumAge,
            Map<String, String> customAttributes,
            BigDecimal costPrice,
            int stockQuantity
    ) {}
}