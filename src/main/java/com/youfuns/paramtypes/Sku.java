package com.youfuns.paramtypes;

import com.youfuns.exceptions.IllegalFieldException;

import java.util.regex.Pattern;

public record Sku(String sku) implements ValidatedValue {
    // SKU pattern: alphanumeric, hyphens, underscores, 4-30 characters
    // Must contain at least one letter and one number
    private static final Pattern SKU_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z0-9_-]{4,30}$"
    );

    public Sku {
        if (sku == null) {
            throw new IllegalFieldException(null, "SKU cannot be null", ParamType.SKU);
        }
        if (sku.isBlank()) {
            throw new IllegalFieldException(sku, "SKU cannot be blank", ParamType.SKU);
        }
        validate(sku);
    }

    @Override
    public Pattern validationPattern() {
        return SKU_PATTERN;
    }

    @Override
    public ParamType paramType() {
        return ParamType.SKU;
    }

    // ===== FACTORY METHODS =====

    public static Sku fromString(String sku) {
        return new Sku(sku);
    }

    public static Sku generate(String prefix, String suffix) {
        if (prefix == null || prefix.isBlank()) {
            prefix = "SKU";
        }
        if (suffix == null || suffix.isBlank()) {
            suffix = String.format("%04d", (int)(Math.random() * 10000));
        }
        return new Sku(prefix + "-" + suffix);
    }

    public static Sku generate() {
        return generate(null, null);
    }

    // ===== UTILITY METHODS =====

    public String getPrefix() {
        int index = sku.indexOf('-');
        if (index > 0) {
            return sku.substring(0, index);
        }
        return sku;
    }

    public String getSuffix() {
        int index = sku.lastIndexOf('-');
        if (index > 0 && index < sku.length() - 1) {
            return sku.substring(index + 1);
        }
        return sku;
    }

    public boolean matches(String prefix) {
        return sku.startsWith(prefix);
    }

    @Override
    public String toString() {
        return sku;
    }
}