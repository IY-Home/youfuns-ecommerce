package com.youfuns.ecommerce.products;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.ecommerce.repo.InMemoryRepository;
import com.youfuns.paramtypes.UuidFormat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ProductListRepository implements InMemoryRepository<UUID, ProductList> {
    private final Map<UUID, ProductList> store = new ConcurrentHashMap<>();

    // ============= CRUD OPERATIONS =============

    @Override
    public ResultReturn insert(ProductList list) {
        LoggerManager.quickLog(this, "Inserting ProductList: " + UuidFormat.shortenUUID(list.getListId()));
        store.put(list.getListId(), list);
        LoggerManager.quickLog(this, "ProductList inserted successfully.");
        return new ResultReturn(ResultReturn.Result.SUCCESS, "List inserted successfully.");
    }

    @Override
    public ResultReturn update(ProductList list) {
        LoggerManager.quickLog(this, "Updating ProductList: " + UuidFormat.shortenUUID(list.getListId()));
        if (!store.containsKey(list.getListId())) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "List not found.");
        }
        store.put(list.getListId(), list);
        LoggerManager.quickLog(this, "ProductList updated successfully.");
        return new ResultReturn(ResultReturn.Result.SUCCESS, "List updated successfully.");
    }

    @Override
    public ResultReturn updateById(UUID id, ProductList list) {
        return update(list);
    }

    @Override
    public ResultReturn delete(ProductList list) {
        return deleteById(list.getListId());
    }

    @Override
    public ResultReturn deleteById(UUID id) {
        LoggerManager.quickLog(this, "Deleting ProductList: " + UuidFormat.shortenUUID(id));
        store.remove(id);
        return new ResultReturn(ResultReturn.Result.SUCCESS, "List deleted successfully.");
    }

    @Override
    public void deleteAll() {
        store.clear();
        LoggerManager.quickLog(this, "All ProductLists deleted.");
    }

    @Override
    public int deleteAllById(List<UUID> ids) {
        int deleted = 0;
        for (UUID id : ids) {
            store.remove(id);
            deleted++;
        }
        return deleted;
    }

    // ============= READ OPERATIONS =============

    @Override
    public Optional<ProductList> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return store.containsKey(id);
    }

    @Override
    public long count() {
        return store.size();
    }

    @Override
    public List<ProductList> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Map<UUID, ProductList> findAllAsMap() {
        return new HashMap<>(store);
    }

    @Override
    public List<ProductList> findAllById(List<UUID> ids) {
        return ids.stream().map(store::get).filter(Objects::nonNull).toList();
    }

    @Override
    public Map<UUID, ProductList> findAllByIdAsMap(List<UUID> ids) {
        Map<UUID, ProductList> result = new HashMap<>();
        for (UUID id : ids) {
            ProductList list = store.get(id);
            if (list != null) result.put(id, list);
        }
        return result;
    }

    // ============= CUSTOM QUERIES =============

    public Optional<ProductList> findByUserId(UUID userId, Class<? extends ProductList> type) {
        return store.values().stream()
                .filter(list -> {
                    if (type == Cart.class && list instanceof Cart) {
                        return ((Cart) list).getUserId().equals(userId);
                    }
                    if (type == Wishlist.class && list instanceof Wishlist) {
                        return ((Wishlist) list).getUserId().equals(userId);
                    }
                    return false;
                })
                .findFirst();
    }

    public List<ProductList> findAllByUserId(UUID userId) {
        return store.values().stream()
                .filter(list -> {
                    if (list instanceof Cart) return ((Cart) list).getUserId().equals(userId);
                    if (list instanceof Wishlist) return ((Wishlist) list).getUserId().equals(userId);
                    return false;
                })
                .toList();
    }
}