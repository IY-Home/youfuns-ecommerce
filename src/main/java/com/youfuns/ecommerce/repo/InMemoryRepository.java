package com.youfuns.ecommerce.repo;

import java.util.*;
import java.util.function.Predicate;

public interface InMemoryRepository<I, T> extends Repository<I, T> {
    default List<T> selectWhere(Predicate<? super T> predicate) {
        return findAllAsMap().values().stream()
                .filter(predicate)
                .toList();
    }

    default Map<I, T> selectWhereAsMap(Predicate<? super T> predicate) {
        Map<I, T> result = new HashMap<>();
        findAllAsMap().forEach((id, value) -> {
            if (predicate.test(value)) {
                result.put(id, value);
            }
        });
        return result;
    }

    default int countWhere(Predicate<? super T> predicate) {
        return (int) findAllAsMap().values().stream().filter(predicate).count();
    }

    default void deleteWhere(Predicate<? super T> predicate) {
        findAllAsMap().entrySet().stream()
                .filter(entry -> predicate.test(entry.getValue()))
                .map(Map.Entry::getKey)
                .forEach(this::deleteById);
    }
}