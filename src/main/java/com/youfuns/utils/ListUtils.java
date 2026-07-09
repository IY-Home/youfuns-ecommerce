package com.youfuns.utils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListUtils {
    public record Partition<T>(List<T> matching, List<T> remaining) {}

    // ============= EXISTING METHODS (Keep as-is) =============

    public static <T> List<T> ofWhich(List<T> list, Predicate<? super T> predicate) {
        if (list == null || predicate == null) throw new IllegalArgumentException("List or predicate is null.");
        return list.stream().filter(predicate).toList();
    }

    public static <T> List<T> filterNulls(List<T> list) {
        return ofWhich(list, Objects::nonNull);
    }

    public static <T> boolean hasNulls(List<T> list) {
        if (list == null) return false;
        return list.stream().anyMatch(Objects::isNull);
    }

    public static <T> boolean isAllNull(List<T> list) {
        if (list == null || list.isEmpty()) return true;
        return list.stream().allMatch(Objects::isNull);
    }

    public static <T> Partition<T> partition(List<T> list, Predicate<? super T> predicate) {
        if (list == null || predicate == null) throw new IllegalArgumentException("List or predicate is null.");

        var partitioned = list.stream()
                .collect(Collectors.partitioningBy(predicate));

        return new Partition<>(partitioned.get(true), partitioned.get(false));
    }

    public static <T> T findFirst(List<T> list, Predicate<? super T> predicate, T defaultValue) {
        if (list == null || predicate == null) return defaultValue;
        return list.stream()
                .filter(predicate)
                .findFirst()
                .orElse(defaultValue);
    }

    // ============= NEW METHODS =============

    /**
     * Adds an item to a copy of the list, preserving immutability.
     * Returns a new immutable list.
     */
    @SafeVarargs
    public static <T> List<T> add(List<T> list, T... items) {
        if (items == null || items.length == 0) {
            return list != null ? List.copyOf(list) : List.of();
        }
        List<T> newList = new ArrayList<>(list != null ? list : List.of());
        for (T item : items) {
            if (item != null) {
                newList.add(item);
            }
        }
        return List.copyOf(newList);
    }

    /**
     * Removes an item from a copy of the list, preserving immutability.
     * Returns a new immutable list.
     */
    public static <T> List<T> remove(List<T> list, T item) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        List<T> newList = new ArrayList<>(list);
        newList.remove(item);
        return List.copyOf(newList);
    }

    /**
     * Removes an item by index from a copy of the list, preserving immutability.
     * Returns a new immutable list.
     */
    public static <T> List<T> removeAt(List<T> list, int index) {
        if (list == null || list.isEmpty() || index < 0 || index >= list.size()) {
            return list != null ? List.copyOf(list) : List.of();
        }
        List<T> newList = new ArrayList<>(list);
        newList.remove(index);
        return List.copyOf(newList);
    }

    /**
     * Removes all items matching the predicate from a copy of the list.
     * Returns a new immutable list.
     */
    public static <T> List<T> removeWhere(List<T> list, Predicate<? super T> predicate) {
        if (list == null || list.isEmpty() || predicate == null) {
            return list != null ? List.copyOf(list) : List.of();
        }
        List<T> newList = new ArrayList<>(list);
        newList.removeIf(predicate);
        return List.copyOf(newList);
    }

    /**
     * Replaces an item at a specific index in a copy of the list.
     * Returns a new immutable list.
     */
    public static <T> List<T> set(List<T> list, int index, T item) {
        if (list == null || index < 0 || index >= list.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + (list == null ? 0 : list.size()));
        }
        List<T> newList = new ArrayList<>(list);
        newList.set(index, item);
        return List.copyOf(newList);
    }

    /**
     * Checks if the list is null or empty.
     */
    public static <T> boolean isEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Checks if the list is not null and not empty.
     */
    public static <T> boolean isNotEmpty(List<T> list) {
        return list != null && !list.isEmpty();
    }

    /**
     * Returns the size of the list, or 0 if null.
     */
    public static <T> int size(List<T> list) {
        return list == null ? 0 : list.size();
    }

    /**
     * Gets the first element of the list, or null if empty/null.
     */
    public static <T> T first(List<T> list) {
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    /**
     * Gets the last element of the list, or null if empty/null.
     */
    public static <T> T last(List<T> list) {
        return list == null || list.isEmpty() ? null : list.get(list.size() - 1);
    }

    /**
     * Converts an array to an immutable list, filtering nulls.
     */
    @SafeVarargs
    public static <T> List<T> toList(T... items) {
        if (items == null || items.length == 0) {
            return List.of();
        }
        return Arrays.stream(items)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Safely maps a list to another list, handling null input.
     */
    public static <T, R> List<R> map(List<T> list, java.util.function.Function<? super T, ? extends R> mapper) {
        if (list == null || list.isEmpty() || mapper == null) {
            return List.of();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .collect(Collectors.toList());  // Returns List<R>
    }

    /**
     * Joins a list of strings with a delimiter.
     */
    public static String join(List<String> list, String delimiter) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(delimiter, list);
    }

    /**
     * Checks if the list contains a specific element.
     */
    public static <T> boolean contains(List<T> list, T item) {
        return list != null && list.contains(item);
    }

    /**
     * Creates a copy of the list (immutable).
     */
    public static <T> List<T> copyOf(List<T> list) {
        return list == null ? List.of() : List.copyOf(list);
    }

    // ============= IMMUTABLE LIST MODIFICATION METHODS =============

    /**
     * Adds an item to a copy of the list, preserving immutability.
     * Returns a new immutable list with the item added.
     * If the list is null, creates a new list.
     * If the item is null, returns the original list unchanged.
     */
    public static <T> List<T> addToList(List<T> list, T item) {
        if (item == null) {
            return list != null ? List.copyOf(list) : List.of();
        }
        List<T> newList = new ArrayList<>(list != null ? list : List.of());
        newList.add(item);
        return List.copyOf(newList);
    }

    /**
     * Removes an item from a copy of the list, preserving immutability.
     * Returns a new immutable list with the item removed.
     * If the list is null or empty, returns an empty list.
     * If the item is null, returns a copy of the original list.
     */
    public static <T> List<T> removeFromList(List<T> list, T item) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        if (item == null) {
            return List.copyOf(list);
        }
        List<T> newList = new ArrayList<>(list);
        newList.remove(item);
        return List.copyOf(newList);
    }

    /**
     * Adds multiple items to a copy of the list, preserving immutability.
     * Returns a new immutable list with all items added.
     */
    @SafeVarargs
    public static <T> List<T> addAllToList(List<T> list, T... items) {
        if (items == null || items.length == 0) {
            return list != null ? List.copyOf(list) : List.of();
        }
        List<T> newList = new ArrayList<>(list != null ? list : List.of());
        for (T item : items) {
            if (item != null) {
                newList.add(item);
            }
        }
        return List.copyOf(newList);
    }

    /**
     * Removes multiple items from a copy of the list, preserving immutability.
     * Returns a new immutable list with all items removed.
     */
    @SafeVarargs
    public static <T> List<T> removeAllFromList(List<T> list, T... items) {
        if (list == null || list.isEmpty() || items == null || items.length == 0) {
            return list != null ? List.copyOf(list) : List.of();
        }
        List<T> newList = new ArrayList<>(list);
        for (T item : items) {
            newList.remove(item);
        }
        return List.copyOf(newList);
    }
}