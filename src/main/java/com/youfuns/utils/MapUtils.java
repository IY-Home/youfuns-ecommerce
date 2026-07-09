package com.youfuns.utils;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapUtils {

    // ============= IMMUTABLE MAP MODIFICATION METHODS =============

    /**
     * Puts a key-value pair into a copy of the map, preserving immutability.
     * Returns a new immutable map with the entry added.
     * If the map is null, creates a new map.
     * If key or value is null, returns the original map unchanged.
     */
    public static <K, V> Map<K, V> put(Map<K, V> map, K key, V value) {
        if (key == null || value == null) {
            return map != null ? Map.copyOf(map) : Map.of();
        }
        Map<K, V> newMap = new HashMap<>(map != null ? map : Map.of());
        newMap.put(key, value);
        return Map.copyOf(newMap);
    }

    /**
     * Removes a key from a copy of the map, preserving immutability.
     * Returns a new immutable map with the key removed.
     * If the map is null or empty, returns an empty map.
     * If the key is null, returns a copy of the original map.
     */
    public static <K, V> Map<K, V> remove(Map<K, V> map, K key) {
        if (map == null || map.isEmpty()) {
            return Map.of();
        }
        if (key == null) {
            return Map.copyOf(map);
        }
        Map<K, V> newMap = new HashMap<>(map);
        newMap.remove(key);
        return Map.copyOf(newMap);
    }

    /**
     * Puts multiple entries into a copy of the map, preserving immutability.
     * Returns a new immutable map with all entries added.
     */
    @SafeVarargs
    public static <K, V> Map<K, V> putAll(Map<K, V> map, Map.Entry<K, V>... entries) {
        if (entries == null || entries.length == 0) {
            return map != null ? Map.copyOf(map) : Map.of();
        }
        Map<K, V> newMap = new HashMap<>(map != null ? map : Map.of());
        for (Map.Entry<K, V> entry : entries) {
            if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                newMap.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(newMap);
    }

    /**
     * Puts multiple entries into a copy of the map, preserving immutability.
     * Returns a new immutable map with all entries added.
     */
    public static <K, V> Map<K, V> putAll(Map<K, V> map, Map<K, V> other) {
        if (other == null || other.isEmpty()) {
            return map != null ? Map.copyOf(map) : Map.of();
        }
        Map<K, V> newMap = new HashMap<>(map != null ? map : Map.of());
        for (Map.Entry<K, V> entry : other.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                newMap.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(newMap);
    }

    /**
     * Removes multiple keys from a copy of the map, preserving immutability.
     * Returns a new immutable map with all keys removed.
     */
    @SafeVarargs
    public static <K, V> Map<K, V> removeAll(Map<K, V> map, K... keys) {
        if (map == null || map.isEmpty() || keys == null || keys.length == 0) {
            return map != null ? Map.copyOf(map) : Map.of();
        }
        Map<K, V> newMap = new HashMap<>(map);
        for (K key : keys) {
            newMap.remove(key);
        }
        return Map.copyOf(newMap);
    }

    /**
     * Removes all entries where the key matches the predicate.
     * Returns a new immutable map.
     */
    public static <K, V> Map<K, V> removeWhereKey(Map<K, V> map, java.util.function.Predicate<? super K> predicate) {
        if (map == null || map.isEmpty() || predicate == null) {
            return map != null ? Map.copyOf(map) : Map.of();
        }
        Map<K, V> newMap = new HashMap<>(map);
        newMap.keySet().removeIf(predicate);
        return Map.copyOf(newMap);
    }

    /**
     * Removes all entries where the value matches the predicate.
     * Returns a new immutable map.
     */
    public static <K, V> Map<K, V> removeWhereValue(Map<K, V> map, java.util.function.Predicate<? super V> predicate) {
        if (map == null || map.isEmpty() || predicate == null) {
            return map != null ? Map.copyOf(map) : Map.of();
        }
        Map<K, V> newMap = new HashMap<>(map);
        newMap.values().removeIf(predicate);
        return Map.copyOf(newMap);
    }

    // ============= UTILITY METHODS =============

    /**
     * Checks if the map is null or empty.
     */
    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Checks if the map is not null and not empty.
     */
    public static <K, V> boolean isNotEmpty(Map<K, V> map) {
        return map != null && !map.isEmpty();
    }

    /**
     * Returns the size of the map, or 0 if null.
     */
    public static <K, V> int size(Map<K, V> map) {
        return map == null ? 0 : map.size();
    }

    /**
     * Creates an immutable copy of the map.
     */
    public static <K, V> Map<K, V> copyOf(Map<K, V> map) {
        return map == null ? Map.of() : Map.copyOf(map);
    }

    /**
     * Gets a value from the map, or defaultValue if not found.
     */
    public static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
        if (map == null || key == null) {
            return defaultValue;
        }
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * Checks if the map contains a key.
     */
    public static <K, V> boolean containsKey(Map<K, V> map, K key) {
        return map != null && key != null && map.containsKey(key);
    }

    /**
     * Checks if the map contains a value.
     */
    public static <K, V> boolean containsValue(Map<K, V> map, V value) {
        return map != null && map.containsValue(value);
    }

    /**
     * Maps keys using a function.
     * Returns a new map with transformed keys.
     */
    public static <K, V, R> Map<R, V> mapKeys(Map<K, V> map, Function<? super K, ? extends R> keyMapper) {
        if (map == null || map.isEmpty() || keyMapper == null) {
            return Map.of();
        }
        Map<R, V> result = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            R newKey = keyMapper.apply(entry.getKey());
            if (newKey != null && entry.getValue() != null) {
                result.put(newKey, entry.getValue());
            }
        }
        return Map.copyOf(result);
    }

    /**
     * Maps values using a function.
     * Returns a new map with transformed values.
     */
    public static <K, V, R> Map<K, R> mapValues(Map<K, V> map, Function<? super V, ? extends R> valueMapper) {
        if (map == null || map.isEmpty() || valueMapper == null) {
            return Map.of();
        }
        Map<K, R> result = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            R newValue = valueMapper.apply(entry.getValue());
            if (entry.getKey() != null && newValue != null) {
                result.put(entry.getKey(), newValue);
            }
        }
        return Map.copyOf(result);
    }

    /**
     * Filters entries where the key matches the predicate.
     * Returns a new immutable map.
     */
    public static <K, V> Map<K, V> filterKeys(Map<K, V> map, java.util.function.Predicate<? super K> predicate) {
        if (map == null || map.isEmpty() || predicate == null) {
            return Map.of();
        }
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (predicate.test(entry.getKey()) && entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(result);
    }

    /**
     * Filters entries where the value matches the predicate.
     * Returns a new immutable map.
     */
    public static <K, V> Map<K, V> filterValues(Map<K, V> map, java.util.function.Predicate<? super V> predicate) {
        if (map == null || map.isEmpty() || predicate == null) {
            return Map.of();
        }
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (predicate.test(entry.getValue()) && entry.getKey() != null) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(result);
    }

    /**
     * Creates a map from key-value pairs.
     */
    @SafeVarargs
    public static <K, V> Map<K, V> of(Map.Entry<K, V>... entries) {
        if (entries == null || entries.length == 0) {
            return Map.of();
        }
        Map<K, V> map = new HashMap<>();
        for (Map.Entry<K, V> entry : entries) {
            if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(map);
    }

    /**
     * Creates a map from two parallel lists (keys and values).
     */
    public static <K, V> Map<K, V> fromLists(List<K> keys, List<V> values) {
        if (keys == null || values == null || keys.isEmpty() || values.isEmpty()) {
            return Map.of();
        }
        int size = Math.min(keys.size(), values.size());
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            K key = keys.get(i);
            V value = values.get(i);
            if (key != null && value != null) {
                map.put(key, value);
            }
        }
        return Map.copyOf(map);
    }

    /**
     * Helper method to create a Map.Entry.
     */
    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }
}