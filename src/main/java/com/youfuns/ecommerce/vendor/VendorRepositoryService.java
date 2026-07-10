package com.youfuns.ecommerce.vendor;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.Permission;
import com.youfuns.ecommerce.auth.PermissionChecker;
import com.youfuns.ecommerce.auth.RoleToken;
import com.youfuns.ecommerce.auth.UserRepositoryService;
import com.youfuns.ecommerce.frontend.utils.ResultPayload;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.ecommerce.user.User;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.paramtypes.JsonWebToken;
import com.youfuns.paramtypes.UuidFormat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VendorRepositoryService {
    private final VendorRepository vendorRepository;
    private final UserRepositoryService userRepositoryService;

    public VendorRepositoryService(UserRepositoryService userRepositoryService) {
        LoggerManager.quickLog(this, "Creating VendorRepositoryService...");
        this.vendorRepository = new VendorRepository();
        this.userRepositoryService = userRepositoryService;
        LoggerManager.quickLog(this, "VendorRepository created");
    }

    // ============= CREATE =============

    public ResultReturn registerVendor(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Registering vendor...");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        try {
            PermissionChecker.checkPermissionWithUser(token, user.getId(), Permission.BECOME_VENDOR);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions to become a vendor.");
        }

        if (vendorRepository.existsById(user.getId())) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Already registered as a vendor.");
        }

        VendorService vendorService = new VendorService(user.getId());
        return vendorRepository.insert(vendorService);
    }

    // ============= APPROVAL =============

    public ResultReturn approveVendorStep1(RoleToken adminRt, UUID userId) {
        LoggerManager.quickLog(this, "Approving vendor step 1 (activate) for user: " + UuidFormat.shortenUUID(userId));

        Optional<VendorService> vendorOpt = vendorRepository.findById(userId);
        if (vendorOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor not found.");
        }

        VendorService vendor = vendorOpt.get();
        return vendor.activateVendor(adminRt);
    }

    public ResultReturn approveVendorStep3(RoleToken adminRt, UUID userId) {
        LoggerManager.quickLog(this, "Approving vendor step 3 (update status) for user: " + UuidFormat.shortenUUID(userId));

        try {
            PermissionChecker.checkPermission(adminRt, Permission.APPROVE_VENDORS);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions to approve vendors.");
        }

        Optional<VendorService> vendorOpt = vendorRepository.findById(userId);
        if (vendorOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor not found.");
        }

        VendorService vendor = vendorOpt.get();
        if (!vendor.isActive()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor has not been activated. Complete step 1 first.");
        }

        // Update the repository (already active, but we confirm)
        return vendorRepository.update(vendor);
    }

    // ============= DISABLE =============

    public ResultReturn disableVendor(JsonWebToken adminJwt, UUID userId) {
        LoggerManager.quickLog(this, "Disabling vendor for user: " + UuidFormat.shortenUUID(userId));

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(adminJwt);
        if (adminOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User admin = adminOpt.get();
        RoleToken adminRt = admin.getToken();

        Optional<VendorService> vendorOpt = vendorRepository.findById(userId);
        if (vendorOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor not found.");
        }

        VendorService vendor = vendorOpt.get();
        ResultReturn result = vendor.deactivateVendor(adminRt);
        if (result.isSuccess()) {
            vendorRepository.update(vendor);
        }
        return result;
    }

    // ============= READ =============

    public ResultPayload<VendorService> getVendor(JsonWebToken jwt, UUID userId) {
        LoggerManager.quickLog(this, "Getting vendor for user: " + UuidFormat.shortenUUID(userId));

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."),
                    null
            );
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<VendorService> vendorOpt = vendorRepository.findById(userId);
        if (vendorOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Vendor not found."),
                    null
            );
        }

        VendorService vendor = vendorOpt.get();

        // Check if user is the vendor or admin
        if (!user.getId().equals(userId)) {
            try {
                PermissionChecker.checkPermission(token, Permission.VIEW_ANY_VENDORS);
            } catch (AccessDeniedException e) {
                return new ResultPayload<>(
                        new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions."),
                        null
                );
            }
        }

        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Vendor fetched successfully."),
                vendor
        );
    }

    public ResultPayload<List<VendorService>> listAllVendors(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Listing all vendors");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."),
                    List.of()
            );
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        try {
            PermissionChecker.checkPermission(token, Permission.VIEW_ANY_VENDORS);
        } catch (AccessDeniedException e) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions."),
                    List.of()
            );
        }

        List<VendorService> vendors = vendorRepository.findAll();
        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Vendors fetched successfully."),
                vendors
        );
    }

    public ResultPayload<List<VendorService>> listActiveVendors(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Listing active vendors");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."),
                    List.of()
            );
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        try {
            PermissionChecker.checkPermission(token, Permission.VIEW_ANY_VENDORS);
        } catch (AccessDeniedException e) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions."),
                    List.of()
            );
        }

        List<VendorService> vendors = vendorRepository.findActiveVendors();
        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Active vendors fetched successfully."),
                vendors
        );
    }

    // ============= VENDOR STATISTICS METHODS =============

    public ResultReturn incrementVendorProductCount(UUID userId) {
        LoggerManager.quickLog(this, "Incrementing vendor product count for user: " + UuidFormat.shortenUUID(userId));

        Optional<VendorService> vendorOpt = vendorRepository.findById(userId);
        if (vendorOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor not found.");
        }

        VendorService vendor = vendorOpt.get();
        vendor.incrementTotalProducts();
        return vendorRepository.update(vendor);
    }

    public ResultReturn decrementVendorProductCount(UUID userId) {
        LoggerManager.quickLog(this, "Decrementing vendor product count for user: " + UuidFormat.shortenUUID(userId));

        Optional<VendorService> vendorOpt = vendorRepository.findById(userId);
        if (vendorOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor not found.");
        }

        VendorService vendor = vendorOpt.get();
        vendor.decrementTotalProducts();
        return vendorRepository.update(vendor);
    }

    public ResultReturn updateVendor(UUID userId, VendorService vendor) {
        LoggerManager.quickLog(this, "Updating vendor for user: " + UuidFormat.shortenUUID(userId));
        return vendorRepository.update(vendor);
    }

    // ============= DELETE =============

    public ResultReturn deleteVendor(UUID userId) {
        LoggerManager.quickLog(this, "Deleting vendor for user: " + UuidFormat.shortenUUID(userId));

        Optional<VendorService> vendorOpt = vendorRepository.findById(userId);
        if (vendorOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor not found.");
        }

        return vendorRepository.deleteById(userId);
    }

    public boolean isVendorActive(UUID userId) {
        return vendorRepository.existsActiveVendor(userId);
    }

    public boolean isVendorRegistered(UUID userId) {
        return vendorRepository.existsById(userId);
    }

}