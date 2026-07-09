package com.youfuns.ecommerce.orders;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.ecommerce.repo.InMemoryRepository;
import com.youfuns.paramtypes.UuidFormat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class OrderRepository implements InMemoryRepository<UUID, Order> {
    private final Map<UUID, Order> store = new ConcurrentHashMap<>();

    @Override
    public ResultReturn insert(Order order) {
        LoggerManager.quickLog(this, "Inserting order: " + UuidFormat.shortenUUID(order.getId()));
        store.put(order.getId(), order);
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Order created.");
    }

    @Override
    public ResultReturn update(Order order) {
        if (!store.containsKey(order.getId())) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Order not found.");
        }
        store.put(order.getId(), order);
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Order updated.");
    }

    @Override
    public ResultReturn updateById(UUID id, Order order) {
        return update(order);
    }

    @Override
    public ResultReturn delete(Order order) {
        return deleteById(order.getId());
    }

    @Override
    public ResultReturn deleteById(UUID id) {
        store.remove(id);
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Order deleted.");
    }

    @Override
    public void deleteAll() { store.clear(); }

    @Override
    public int deleteAllById(List<UUID> ids) {
        int count = 0;
        for (UUID id : ids) { store.remove(id); count++; }
        return count;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean existsById(UUID id) { return store.containsKey(id); }

    @Override
    public long count() { return store.size(); }

    @Override
    public List<Order> findAll() { return new ArrayList<>(store.values()); }

    @Override
    public Map<UUID, Order> findAllAsMap() { return new HashMap<>(store); }

    @Override
    public List<Order> findAllById(List<UUID> ids) {
        return ids.stream().map(store::get).filter(Objects::nonNull).toList();
    }

    @Override
    public Map<UUID, Order> findAllByIdAsMap(List<UUID> ids) {
        Map<UUID, Order> result = new HashMap<>();
        for (UUID id : ids) {
            Order order = store.get(id);
            if (order != null) result.put(id, order);
        }
        return result;
    }

    @Override
    public List<Order> selectWhere(Predicate<? super Order> predicate) {
        return store.values().stream().filter(predicate).toList();
    }

    public List<Order> findByUserId(UUID userId) {
        return store.values().stream()
                .filter(o -> o.getUserId().equals(userId))
                .toList();
    }
}