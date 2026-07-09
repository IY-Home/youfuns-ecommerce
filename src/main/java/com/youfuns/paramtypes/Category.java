package com.youfuns.paramtypes;

public enum Category {
    ELECTRONICS("Electronics"),
    CLOTHING("Clothing & Fashion"),
    HOME("Home & Living"),
    BOOKS("Books & Media"),
    BEAUTY("Beauty & Health"),
    GROCERY("Food & Grocery"),
    VEHICLES("Vehicles & Parts"),
    SPORTS("Sports & Outdoors"),
    TOYS("Toys & Hobbies"),
    MISC("Miscellaneous");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Category fromDisplayName(String displayName) {
        for (Category category : values()) {
            if (category.displayName.equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        return null;
    }

    public static Category fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}