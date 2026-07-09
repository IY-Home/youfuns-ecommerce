package com.youfuns.ecommerce.vendor;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.repo.InMemoryRepository;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.ecommerce.vendor.VendorService;
import com.youfuns.paramtypes.UuidFormat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class VendorRepository implements InMemoryRepository<UUID, VendorService> {
    private final Map<UUID, VendorService> store = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> activeStatusIndex = new ConcurrentHashMap<>(); // userId -> isActive

    // ============= CRUD OPERATIONS =============

    @Override
    public ResultReturn insert(VendorService vendor) {
        LoggerManager.quickLog(this, "Inserting vendor for user: " + UuidFormat.shortenUUID(vendor.getUserId()));

        if (store.containsKey(vendor.getUserId())) {
            LoggerManager.quickLog(this, "Insert failed - vendor already exists for user: " + UuidFormat.shortenUUID(vendor.getUserId()));
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor already exists.");
        }

        store.put(vendor.getUserId(), vendor);
        activeStatusIndex.put(vendor.getUserId(), vendor.isActive());

        LoggerManager.quickLog(this, "Vendor inserted successfully for user: " + UuidFormat.shortenUUID(vendor.getUserId()));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Vendor inserted successfully.");
    }

    @Override
    public ResultReturn update(VendorService vendor) {
        LoggerManager.quickLog(this, "Updating vendor for user: " + UuidFormat.shortenUUID(vendor.getUserId()));

        if (!store.containsKey(vendor.getUserId())) {
            LoggerManager.quickLog(this, "Update failed - vendor not found for user: " + UuidFormat.shortenUUID(vendor.getUserId()));
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor not found.");
        }

        store.put(vendor.getUserId(), vendor);
        activeStatusIndex.put(vendor.getUserId(), vendor.isActive());

        LoggerManager.quickLog(this, "Vendor updated successfully for user: " + UuidFormat.shortenUUID(vendor.getUserId()));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Vendor updated successfully.");
    }

    @Override
    public ResultReturn updateById(UUID id, VendorService vendor) {
        LoggerManager.quickLog(this, "Updating vendor by ID: " + UuidFormat.shortenUUID(id));
        if (!store.containsKey(id)) {
            LoggerManager.quickLog(this, "Update failed - vendor not found: " + UuidFormat.shortenUUID(id));
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor not found.");
        }
        return update(vendor);
    }

    @Override
    public ResultReturn delete(VendorService vendor) {
        LoggerManager.quickLog(this, "Deleting vendor for user: " + UuidFormat.shortenUUID(vendor.getUserId()));
        return deleteById(vendor.getUserId());
    }

    @Override
    public ResultReturn deleteById(UUID id) {
        LoggerManager.quickLog(this, "Deleting vendor by ID: " + UuidFormat.shortenUUID(id));

        VendorService removed = store.remove(id);
        if (removed == null) {
            LoggerManager.quickLog(this, "Delete failed - vendor not found: " + UuidFormat.shortenUUID(id));
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor not found.");
        }

        activeStatusIndex.remove(id);

        LoggerManager.quickLog(this, "Vendor deleted successfully: " + UuidFormat.shortenUUID(id));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Vendor deleted successfully.");
    }

    @Override
    public void deleteAll() {
        LoggerManager.quickLog(this, "Deleting all vendors");
        int count = store.size();
        store.clear();
        activeStatusIndex.clear();
        LoggerManager.quickLog(this, "Deleted " + count + " vendors.");
    }

    @Override
    public int deleteAllById(List<UUID> ids) {
        LoggerManager.quickLog(this, "Deleting " + ids.size() + " vendors by ID");
        int deleted = 0;
        for (UUID id : ids) {
            if (deleteById(id).isSuccess()) {
                deleted++;
            }
        }
        LoggerManager.quickLog(this, "Deleted " + deleted + " out of " + ids.size() + " vendors.");
        return deleted;
    }

    // ============= READ OPERATIONS =============

    @Override
    public Optional<VendorService> findById(UUID id) {
        LoggerManager.quickLog(this, "Finding vendor by ID: " + UuidFormat.shortenUUID(id));
        VendorService vendor = store.get(id);
        if (vendor != null) {
            LoggerManager.quickLog(this, "Vendor found: " + UuidFormat.shortenUUID(id));
        } else {
            LoggerManager.quickLog(this, "Vendor not found: " + UuidFormat.shortenUUID(id));
        }
        return Optional.ofNullable(vendor);
    }

    @Override
    public boolean existsById(UUID id) {
        LoggerManager.quickLog(this, "Checking existence of vendor: " + UuidFormat.shortenUUID(id));
        boolean exists = store.containsKey(id);
        LoggerManager.quickLog(this, "Vendor exists: " + exists);
        return exists;
    }

    @Override
    public long count() {
        LoggerManager.quickLog(this, "Counting total vendors");
        long count = store.size();
        LoggerManager.quickLog(this, "Total vendors: " + count);
        return count;
    }

    @Override
    public List<VendorService> findAll() {
        LoggerManager.quickLog(this, "Finding all vendors");
        List<VendorService> vendors = new ArrayList<>(store.values());
        LoggerManager.quickLog(this, "Found " + vendors.size() + " vendors.");
        return vendors;
    }

    @Override
    public Map<UUID, VendorService> findAllAsMap() {
        LoggerManager.quickLog(this, "Finding all vendors as map");
        Map<UUID, VendorService> copy = new HashMap<>(store);
        LoggerManager.quickLog(this, "Returning " + copy.size() + " vendors as map.");
        return copy;
    }

    @Override
    public List<VendorService> findAllById(List<UUID> ids) {
        LoggerManager.quickLog(this, "Finding " + ids.size() + " vendors by ID");
        List<VendorService> vendors = ids.stream()
                .map(store::get)
                .filter(Objects::nonNull)
                .toList();
        LoggerManager.quickLog(this, "Found " + vendors.size() + " out of " + ids.size() + " vendors.");
        return vendors;
    }

    @Override
    public Map<UUID, VendorService> findAllByIdAsMap(List<UUID> ids) {
        LoggerManager.quickLog(this, "Finding " + ids.size() + " vendors by ID as map");
        Map<UUID, VendorService> result = new HashMap<>();
        for (UUID id : ids) {
            VendorService vendor = store.get(id);
            if (vendor != null) {
                result.put(id, vendor);
            }
        }
        LoggerManager.quickLog(this, "Found " + result.size() + " out of " + ids.size() + " vendors.");
        return result;
    }

    // ============= IN-MEMORY SPECIFIC METHODS =============

    @Override
    public List<VendorService> selectWhere(Predicate<? super VendorService> predicate) {
        LoggerManager.quickLog(this, "Selecting vendors with predicate");
        List<VendorService> result = InMemoryRepository.super.selectWhere(predicate);
        LoggerManager.quickLog(this, "Selected " + result.size() + " vendors matching predicate.");
        return result;
    }

    @Override
    public Map<UUID, VendorService> selectWhereAsMap(Predicate<? super VendorService> predicate) {
        LoggerManager.quickLog(this, "Selecting vendors with predicate as map");
        Map<UUID, VendorService> result = InMemoryRepository.super.selectWhereAsMap(predicate);
        LoggerManager.quickLog(this, "Selected " + result.size() + " vendors matching predicate as map.");
        return result;
    }

    @Override
    public int countWhere(Predicate<? super VendorService> predicate) {
        LoggerManager.quickLog(this, "Counting vendors with predicate");
        int count = InMemoryRepository.super.countWhere(predicate);
        LoggerManager.quickLog(this, "Counted " + count + " vendors matching predicate.");
        return count;
    }

    @Override
    public void deleteWhere(Predicate<? super VendorService> predicate) {
        LoggerManager.quickLog(this, "Deleting vendors with predicate");
        InMemoryRepository.super.deleteWhere(predicate);
        LoggerManager.quickLog(this, "Deleted vendors matching predicate.");
    }

    // ============= CUSTOM QUERIES =============

    public List<VendorService> findActiveVendors() {
        LoggerManager.quickLog(this, "Finding active vendors");
        List<VendorService> vendors = store.values().stream()
                .filter(VendorService::isActive)
                .toList();
        LoggerManager.quickLog(this, "Found " + vendors.size() + " active vendors.");
        return vendors;
    }

    public List<VendorService> findInactiveVendors() {
        LoggerManager.quickLog(this, "Finding inactive vendors");
        List<VendorService> vendors = store.values().stream()
                .filter(vendor -> !vendor.isActive())
                .toList();
        LoggerManager.quickLog(this, "Found " + vendors.size() + " inactive vendors.");
        return vendors;
    }

    public long countActiveVendors() {
        LoggerManager.quickLog(this, "Counting active vendors");
        long count = store.values().stream().filter(VendorService::isActive).count();
        LoggerManager.quickLog(this, "Active vendors: " + count);
        return count;
    }

    public long countInactiveVendors() {
        LoggerManager.quickLog(this, "Counting inactive vendors");
        long count = store.values().stream().filter(vendor -> !vendor.isActive()).count();
        LoggerManager.quickLog(this, "Inactive vendors: " + count);
        return count;
    }

    public boolean existsActiveVendor(UUID userId) {
        LoggerManager.quickLog(this, "Checking if vendor is active for user: " + UuidFormat.shortenUUID(userId));
        Boolean isActive = activeStatusIndex.get(userId);
        return isActive != null && isActive;
    }
}