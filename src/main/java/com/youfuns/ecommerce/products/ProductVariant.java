package com.youfuns.ecommerce.products;

import com.youfuns.exceptions.IllegalFieldException;
import com.youfuns.paramtypes.ParamType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductVariant {
    private final Map<String, List<String>> variantList;

    public ProductVariant() {
        variantList = new HashMap<>();
    }

    public ProductVariant addVariant(String name, List<String> variants) {
        if (name == null || name.isBlank() || variants == null || variants.isEmpty()) {
            throw new IllegalFieldException(null, "Product variants: Name or variants cannot be null or empty", ParamType.GENERIC);
        }
        if (variantList.containsKey(name)) {
            throw new IllegalFieldException(name, "Variant name already exists: " + name, ParamType.GENERIC);
        }
        variantList.put(name, variants);
        return this;
    }

    public ProductVariant removeVariant(String name) {
        variantList.remove(name);
        return this;
    }

    public ProductVariant addVariantFor(String name, String variant) {
        variantList.get(name).add(variant);
        return this;
    }

    public ProductVariant removeVariantFor(String name, String variant) {
        List<String> variants = variantList.get(name);
        if (variants != null) {
            variants.remove(variant);
        }
        return this;
    }

    public Map<String, List<String>> getVariantList() {
        return Map.copyOf(variantList);
    }

    public List<String> getVariantsFor(String name) {
        return List.copyOf(variantList.get(name));
    }

    public boolean hasVariant(String name) {
        return variantList.containsKey(name);
    }

    public int getVariantCount() {
        return variantList.size();
    }

    public int getVariantValueCount(String name) {
        List<String> variants = variantList.get(name);
        return variants != null ? variants.size() : 0;
    }

    public boolean containsVariantValue(String name, String value) {
        List<String> variants = variantList.get(name);
        return variants != null && variants.contains(value);
    }

    public ProductVariant clear() {
        variantList.clear();
        return this;
    }
    public boolean isEmpty() {
        return variantList.isEmpty();
    }

    public ProductVariant copy() {
        ProductVariant copy = new ProductVariant();
        for (Map.Entry<String, List<String>> entry : variantList.entrySet()) {
            copy.variantList.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
}
