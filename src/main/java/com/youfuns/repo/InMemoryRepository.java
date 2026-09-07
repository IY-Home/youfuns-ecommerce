package com.youfuns.repo;

import com.youfuns.auth.rbac.ResultReturn;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InMemoryRepository<I, T> implements Repository<I, T> {
    private final Map<I, T> map;

    public InMemoryRepository() {
        this.map = new HashMap<>();
    }

    @Override
    public ResultReturn insert(I key, T value) {
        map.put(key, value);
        return new ResultReturn(ResultReturn.Result.SUCCESS, "The item was inserted successfully.");
    }

    @Override
    public ResultReturn update(I key, T value) {
        if (map.get(key) == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "The item was not found.");
        }
        map.put(key, value);
        return new ResultReturn(ResultReturn.Result.SUCCESS, "The item was updated successfully.");
    }

    @Override
    public ResultReturn delete(I key) {
        map.remove(key);
        return new ResultReturn(ResultReturn.Result.SUCCESS, "The item was deleted successfully.");
    }

    @Override
    public ResultReturn deleteAll() {
        map.clear();
        return new ResultReturn(ResultReturn.Result.SUCCESS, "The repository was cleared successfully.");
    }

    @Override
    public int deleteAllById(List<I> ids) {
        int count = 0;
        for (I key : ids) {
            if (map.containsKey(key)) {
                map.remove(key);
                count++;
            }
        }
        return count;
    }

    @Override
    public Optional<T> findById(I id) {
        return Optional.ofNullable(map.get(id));
    }

    @Override
    public boolean existsById(I id) {
        return map.containsKey(id);
    }

    @Override
    public long count() {
        return map.size();
    }

    @Override
    public Map<I, T> findAll() {
        return Collections.unmodifiableMap(this.map);
    }

    @Override
    public Map<I, T> findAllById(List<I> ids) {
        return map.entrySet().stream().filter(entry -> ids.contains(entry.getKey())).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Set<Map.Entry<I, T>> findWhere(Predicate<? super T> predicate) {
        return map.entrySet().stream().filter(entry -> predicate.test(entry.getValue())).collect(Collectors.toSet());
    }
}
