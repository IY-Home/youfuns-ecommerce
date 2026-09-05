package com.youfuns.repo;

import com.youfuns.auth.ResultReturn;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Repository<I, T> {
    ResultReturn insert(I key, T value);
    ResultReturn update(I key, T value);

    default int insertAll(Map<I, T> items) {
        int saved = 0;
        for (Map.Entry<I, T> item : items.entrySet()) {
            if (insert(item.getKey(), item.getValue()).isSuccess()) saved++;
        }
        return saved;
    }

    ResultReturn delete(I key);
    ResultReturn deleteAll();
    int deleteAllById(List<I> ids);

    Optional<T> findById(I id);
    boolean existsById(I id);
    long count();

    Map<I, T> findAll();
    Map<I, T> findAllById(List<I> ids);

    default boolean isEmpty() {
        return count() == 0;
    }

    default boolean isNotEmpty() {
        return !isEmpty();
    }
}
