package com.youfuns.ecommerce.frontend;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.*;
import com.youfuns.ecommerce.frontend.payloads.*;
import com.youfuns.ecommerce.frontend.utils.ResultPayload;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.ecommerce.orders.Order;
import com.youfuns.ecommerce.orders.OrderRepositoryService;
import com.youfuns.ecommerce.products.*;
import com.youfuns.ecommerce.status.ProductStatus;
import com.youfuns.ecommerce.user.CustomerService;
import com.youfuns.ecommerce.user.User;
import com.youfuns.ecommerce.user.UserProfile;
import com.youfuns.ecommerce.vendor.VendorRepositoryService;
import com.youfuns.ecommerce.vendor.VendorService;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.exceptions.IllegalFieldException;
import com.youfuns.paramtypes.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

public final class FrontendService {
    private final UserRepositoryService userRepositoryService;
    private final ProductListRepository productListRepository;
    private final ProductRepositoryService productRepositoryService;
    private final OrderRepositoryService orderRepositoryService;
    private final VendorRepositoryService vendorRepositoryService;

    public FrontendService() {
        LoggerManager.quickLog(this, "Creating FrontendService and creating UserRepositoryService...");
        userRepositoryService = new UserRepositoryService();
        this.productListRepository = new ProductListRepository();
        this.productRepositoryService = new ProductRepositoryService(userRepositoryService);
        this.orderRepositoryService = new OrderRepositoryService(userRepositoryService, productListRepository, productRepositoryService);
        this.vendorRepositoryService = new VendorRepositoryService(userRepositoryService);
        LoggerManager.quickLog(this, "FrontendService and UserRepositoryService created successfully");
    }

    public ResultReturn createUser(RegisterUserPayload registerUserPayload) {
        LoggerManager.quickLog(this, "Called createUser...");
        if (registerUserPayload == null) {
            LoggerManager.quickLog(this, "registerUserPayload is null!");
            return new ResultReturn(ResultReturn.Result.FAILURE, "The payload is empty.");
        }
        User user = null;
        try {
            user = new User(registerUserPayload);
        } catch (IllegalFieldException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, e.getMessage());
        }
        LoggerManager.quickLog(this, "Adding user to repository...");
        ResultReturn result = userRepositoryService.addUser(user);
        return result.isSuccess() ? new ResultReturn(ResultReturn.Result.SUCCESS, "The user was registered successfully.") : result;
    }

    public ResultReturn insertUser(User user) {
        return userRepositoryService.addUser(user);
    }

    public ResultPayload<JsonWebToken> loginUser(LoginUserPayload loginUserPayload) {
        LoggerManager.quickLog(this, "Called loginUser...");
        if (loginUserPayload == null) {
            LoggerManager.quickLog(this, "loginUserPayload is null!");
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "The payload is empty."), null);
        }
        LoggerManager.quickLog(this, "Calling userRepositoryService.loginUser...");
        UserRepositoryService.SessionToken st = userRepositoryService.loginUser(loginUserPayload.username(), loginUserPayload.password());
        if (st == null) {
            LoggerManager.quickLog(this, "Response is null!");
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "An unexpected error occurred during authentication."), null);
        }
        return new ResultPayload<>(st.result(), st.token());
    }

    public ResultPayload<UserProfile.FullUserProfile> getProfile(JsonWebToken jsonWebToken) {
        LoggerManager.quickLog(this, "Called getProfile...");
        if (jsonWebToken == null) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."), null);
        }
        Optional<User> user = userRepositoryService.getUserFromJwt(jsonWebToken);
        if (user.isPresent()) {
            RoleToken token = user.get().getToken();
            UserProfile.FullUserProfile profile = user.get().getUserProfile().toFullProfile(token);
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "The profile was fetched successfully."), profile);
        }
        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
    }

    public ResultReturn updateProfile(JsonWebToken jwt, UpdateProfilePayload payload) {
        LoggerManager.quickLog(this, "Called updateProfile...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        UserProfile profile = user.getUserProfile();

        // Update each field if present
        if (payload.name() != null) {
            try {
                profile.setName(user.getToken(), new Name(payload.name()));
            } catch (IllegalFieldException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, e.getMessage());
            }
        }

        if (payload.phone() != null) {
            try {
                profile.setPhone(user.getToken(), new PhoneNumber(payload.phone()));
            } catch (IllegalFieldException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, e.getMessage());
            }
        }

        if (payload.dateOfBirth() != null) {
            profile.setDateOfBirth(user.getToken(), payload.dateOfBirth());
        }

        if (payload.profilePicturePath() != null) {
            try {
                profile.setProfilePicturePath(user.getToken(), new FilePath(payload.profilePicturePath()));
            } catch (IllegalFieldException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, e.getMessage());
            }
        }

        // Save changes
        userRepositoryService.updateUser(user);

        return new ResultReturn(ResultReturn.Result.SUCCESS, "Profile updated successfully.");
    }

    public ResultReturn changePassword(JsonWebToken jwt, ChangePasswordPayload payload) {
        LoggerManager.quickLog(this, "Called changePassword...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        return user.getUserCredentials().changePassword(
                token,
                payload.oldPassword(),
                payload.newPassword()
        );
    }

    public ResultReturn adminChangePassword(JsonWebToken jwt, AdminChangePasswordPayload payload) {
        LoggerManager.quickLog(this, "Called adminChangePassword...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(jwt);
        if (adminOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User admin = adminOpt.get();
        RoleToken adminToken = admin.getToken();

        // Check admin permission
        try {
            PermissionChecker.checkPermission(adminToken, Permission.MANAGE_ANY_USERS);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        // Find target user
        Optional<User> targetOpt = userRepositoryService.getUserById(payload.userId());
        if (targetOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Target user not found.");
        }

        User target = targetOpt.get();

        return target.getUserCredentials().resetPasswordAdmin(
                admin.getToken(),
                payload.newPassword()
        );
    }

    public ResultReturn deleteAccount(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called deleteAccount...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        // Check permission
        try {
            PermissionChecker.checkPermissionWithUser(token, user.getId(), Permission.DELETE_SELF_USER);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        return userRepositoryService.deleteUser(user);
    }

// ============= GET PUBLIC PROFILE (No Auth) =============

    public ResultPayload<VendorService.VendorPublicProfile> getPublicProfile(UUID userId) {
        LoggerManager.quickLog(this, "Called getPublicProfile...");

        if (userId == null) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "User ID is null."),
                    null
            );
        }

        Optional<User> userOpt = userRepositoryService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "User not found."),
                    null
            );
        }

        VendorService.VendorPublicProfile publicProfile = null;
        try {
            publicProfile = userOpt.get()
                    .getVendorService().toPublicProfile();
        } catch (AccessDeniedException e) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Target user is not a vendor."), null);
        }

        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Public profile fetched successfully."),
                publicProfile
        );
    }

    public ResultPayload<List<UserRole>> getOwnRoles(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called getOwnRoles...");

        if (jwt == null) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."),
                    null
            );
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."),
                    null
            );
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        try {
            PermissionChecker.checkPermissionWithUser(token, user.getId(), Permission.VIEW_SELF_ROLES);
        } catch (AccessDeniedException e) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions."),
                    null
            );
        }

        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Roles fetched successfully."),
                user.getRoles()
        );
    }

    public ResultPayload<UserProfile.FullUserProfile> getProfileAdmin(JsonWebToken jwt, UUID userId) {
        LoggerManager.quickLog(this, "Called getProfileAdmin...");

        if (jwt == null) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."),
                    null
            );
        }
        if (userId == null) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "User ID is null."),
                    null
            );
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(jwt);
        if (adminOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."),
                    null
            );
        }

        User admin = adminOpt.get();
        RoleToken adminToken = admin.getToken();

        // Check admin permission
        try {
            PermissionChecker.checkPermission(adminToken, Permission.VIEW_ANY_USERS);
        } catch (AccessDeniedException e) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions."),
                    null
            );
        }

        Optional<User> targetOpt = userRepositoryService.getUserById(userId);
        if (targetOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Target user not found."),
                    null
            );
        }

        User target = targetOpt.get();
        UserProfile.FullUserProfile profile = target.getUserProfile().toFullProfileAdmin(admin.getToken());

        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "User profile fetched successfully."),
                profile
        );
    }

    public ResultReturn updateProfileAdmin(JsonWebToken jwt, AdminUpdateProfilePayload payload) {
        LoggerManager.quickLog(this, "Called updateProfileAdmin...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(jwt);
        if (adminOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User admin = adminOpt.get();
        RoleToken adminToken = admin.getToken();

        // Check admin permission
        try {
            PermissionChecker.checkPermission(adminToken, Permission.MANAGE_ANY_USERS);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        Optional<User> targetOpt = userRepositoryService.getUserById(payload.userId());
        if (targetOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Target user not found.");
        }

        User target = targetOpt.get();
        UserProfile profile = target.getUserProfile();

        // Update each field if present
        if (payload.name() != null) {
            try {
                profile.setNameAdmin(admin.getToken(), new Name(payload.name()));
            } catch (IllegalFieldException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, e.getMessage());
            }
        }

        if (payload.phone() != null) {
            try {
                profile.setPhoneAdmin(admin.getToken(), new PhoneNumber(payload.phone()));
            } catch (IllegalFieldException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, e.getMessage());
            }
        }

        if (payload.dateOfBirth() != null) {
            profile.setDateOfBirthAdmin(admin.getToken(), payload.dateOfBirth());
        }

        if (payload.profilePicturePath() != null) {
            try {
                profile.setProfilePicturePathAdmin(admin.getToken(), new FilePath(payload.profilePicturePath()));
            } catch (IllegalFieldException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, e.getMessage());
            }
        }

        // Save changes
        userRepositoryService.updateUser(target);

        return new ResultReturn(ResultReturn.Result.SUCCESS, "User profile updated successfully.");
    }

    public ResultReturn addUsername(JsonWebToken jwt, AddUsernamePayload payload) {
        LoggerManager.quickLog(this, "Called addUsername...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        return user.getUserCredentials().addUsername(token, payload.username());
    }

    public ResultReturn removeUsername(JsonWebToken jwt, RemoveUsernamePayload payload) {
        LoggerManager.quickLog(this, "Called removeUsername...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        return user.getUserCredentials().removeUsername(token, payload.username());
    }

    // ============= FRONTENDSERVICE ADDITIONS =============

// ============= GET SPECIFIC PROFILE FIELDS =============

    public ResultPayload<EmailAddress> getEmail(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called getEmail...");

        if (jwt == null) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."), null);
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();
        EmailAddress email = user.getUserProfile().getEmail(token);

        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "Email fetched successfully."), email);
    }

    public ResultPayload<Name> getName(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called getName...");

        if (jwt == null) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."), null);
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();
        Name name = user.getUserProfile().getName(token);

        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "Name fetched successfully."), name);
    }

    public ResultPayload<PhoneNumber> getPhone(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called getPhone...");

        if (jwt == null) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."), null);
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();
        PhoneNumber phone = user.getUserProfile().getPhone(token);

        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "Phone fetched successfully."), phone);
    }

    public ResultPayload<Username> getUsername(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called getUsername...");

        if (jwt == null) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."), null);
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();
        Username username = user.getUserProfile().getUsername(token);

        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "Username fetched successfully."), username);
    }

    public ResultPayload<LocalDate> getDateOfBirth(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called getDateOfBirth...");

        if (jwt == null) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."), null);
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();
        LocalDate dateOfBirth = user.getUserProfile().getDateOfBirth(token);

        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "Date of birth fetched successfully."), dateOfBirth);
    }

    public ResultPayload<FilePath> getProfilePicturePath(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called getProfilePicturePath...");

        if (jwt == null) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."), null);
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();
        FilePath path = user.getUserProfile().getProfilePicturePath(token);

        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "Profile picture path fetched successfully."), path);
    }

// ============= UPDATE SPECIFIC PROFILE FIELDS =============

    public ResultReturn updateEmail(JsonWebToken jwt, EmailAddress email) {
        LoggerManager.quickLog(this, "Called updateEmail...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (email == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Email is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        ResultReturn result = user.getUserProfile().setEmail(user.getToken(), email);

        if (result.isSuccess()) {
            userRepositoryService.updateUser(user);
        }

        return result;
    }

    public ResultReturn updateName(JsonWebToken jwt, Name name) {
        LoggerManager.quickLog(this, "Called updateName...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (name == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Name is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        ResultReturn result = user.getUserProfile().setName(user.getToken(), name);

        if (result.isSuccess()) {
            userRepositoryService.updateUser(user);
        }

        return result;
    }

    public ResultReturn updatePhone(JsonWebToken jwt, PhoneNumber phone) {
        LoggerManager.quickLog(this, "Called updatePhone...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (phone == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Phone is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        ResultReturn result = user.getUserProfile().setPhone(user.getToken(), phone);

        if (result.isSuccess()) {
            userRepositoryService.updateUser(user);
        }

        return result;
    }

    public ResultReturn updateUsername(JsonWebToken jwt, Username username) {
        LoggerManager.quickLog(this, "Called updateUsername...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (username == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Username is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        ResultReturn result = user.getUserProfile().setUsername(user.getToken(), username);

        if (result.isSuccess()) {
            userRepositoryService.updateUser(user);
        }

        return result;
    }

    public ResultReturn updateDateOfBirth(JsonWebToken jwt, LocalDate dateOfBirth) {
        LoggerManager.quickLog(this, "Called updateDateOfBirth...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (dateOfBirth == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Date of birth is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        ResultReturn result = user.getUserProfile().setDateOfBirth(user.getToken(), dateOfBirth);

        if (result.isSuccess()) {
            userRepositoryService.updateUser(user);
        }

        return result;
    }

// ============= UPDATE PROFILE PICTURE (Called by FileUploadHandler) =============

    public ResultReturn updateProfilePicture(JsonWebToken jwt, String filePath) {
        LoggerManager.quickLog(this, "Called updateProfilePicture...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (filePath == null || filePath.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "File path is null or empty.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();

        try {
            ResultReturn result = user.getUserProfile().setProfilePicturePath(user.getToken(), new FilePath(filePath));
            if (result.isSuccess()) {
                userRepositoryService.updateUser(user);
            }
            return result;
        } catch (IllegalFieldException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, e.getMessage());
        }
    }

    public ResultPayload<Cart> getCart(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called getCart...");

        if (jwt == null) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."), null);
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        // Check if user has a cart, create if not
        Optional<ProductList> cartOpt = productListRepository.findByUserId(user.getId(), Cart.class);
        Cart cart;
        if (cartOpt.isEmpty()) {
            cart = new Cart(user.getId());
            productListRepository.insert(cart);
        } else {
            cart = (Cart) cartOpt.get();
        }

        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "Cart fetched."), cart);
    }

    public ResultReturn addToCart(JsonWebToken jwt, AddToCartPayload payload) {
        LoggerManager.quickLog(this, "Called addToCart...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        // Get or create cart
        Optional<ProductList> cartOpt = productListRepository.findByUserId(user.getId(), Cart.class);
        Cart cart;
        if (cartOpt.isEmpty()) {
            cart = new Cart(user.getId());
            productListRepository.insert(cart);
        } else {
            cart = (Cart) cartOpt.get();
        }

        // Get product
        ResultPayload<Product> productResult = productRepositoryService.getProductById(payload.productId());
        if (!productResult.resultMessage().isSuccess() || productResult.payload() == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product product = productResult.payload();
        return cart.addToCart(token, product, payload.quantity());
    }

    public ResultReturn removeFromCart(JsonWebToken jwt, RemoveFromCartPayload payload) {
        LoggerManager.quickLog(this, "Called removeFromCart...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<ProductList> cartOpt = productListRepository.findByUserId(user.getId(), Cart.class);
        if (cartOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Cart is empty.");
        }

        Cart cart = (Cart) cartOpt.get();
        return cart.removeFromCart(token, payload.productId());
    }

    public ResultReturn updateCartQuantity(JsonWebToken jwt, UpdateCartQuantityPayload payload) {
        LoggerManager.quickLog(this, "Called updateCartQuantity...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<ProductList> cartOpt = productListRepository.findByUserId(user.getId(), Cart.class);
        if (cartOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Cart is empty.");
        }

        Cart cart = (Cart) cartOpt.get();
        return cart.updateCartQuantity(token, payload.productId(), payload.quantity());
    }

    public ResultReturn clearCart(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called clearCart...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<ProductList> cartOpt = productListRepository.findByUserId(user.getId(), Cart.class);
        if (cartOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Cart is empty.");
        }

        Cart cart = (Cart) cartOpt.get();
        return cart.clearCart(token);
    }

    public ResultPayload<Cart> getCartTotal(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called getCartTotal...");

        if (jwt == null) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."), null);
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<ProductList> cartOpt = productListRepository.findByUserId(user.getId(), Cart.class);
        if (cartOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Cart is empty."), null);
        }

        Cart cart = (Cart) cartOpt.get();
        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "Cart total fetched."), cart);
    }

// ============= WISHLIST FUNCTIONS =============

    public ResultPayload<Wishlist> getWishlist(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called getWishlist...");

        if (jwt == null) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."), null);
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<ProductList> wishlistOpt = productListRepository.findByUserId(user.getId(), Wishlist.class);
        Wishlist wishlist;
        if (wishlistOpt.isEmpty()) {
            wishlist = new Wishlist(user.getId());
            productListRepository.insert(wishlist);
        } else {
            wishlist = (Wishlist) wishlistOpt.get();
        }

        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "Wishlist fetched."), wishlist);
    }

    public ResultReturn addToWishlist(JsonWebToken jwt, AddToWishlistPayload payload) {
        LoggerManager.quickLog(this, "Called addToWishlist...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<ProductList> wishlistOpt = productListRepository.findByUserId(user.getId(), Wishlist.class);
        Wishlist wishlist;
        if (wishlistOpt.isEmpty()) {
            wishlist = new Wishlist(user.getId());
            productListRepository.insert(wishlist);
        } else {
            wishlist = (Wishlist) wishlistOpt.get();
        }

        ResultPayload<Product> productResult = productRepositoryService.getProductById(payload.productId());
        if (!productResult.resultMessage().isSuccess() || productResult.payload() == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product product = productResult.payload();
        return wishlist.addEntry(token, product);
    }

    public ResultReturn removeFromWishlist(JsonWebToken jwt, RemoveFromWishlistPayload payload) {
        LoggerManager.quickLog(this, "Called removeFromWishlist...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<ProductList> wishlistOpt = productListRepository.findByUserId(user.getId(), Wishlist.class);
        if (wishlistOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Wishlist is empty.");
        }

        Wishlist wishlist = (Wishlist) wishlistOpt.get();
        return wishlist.removeEntry(token, payload.productId());
    }

    public ResultReturn clearWishlist(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called clearWishlist...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<ProductList> wishlistOpt = productListRepository.findByUserId(user.getId(), Wishlist.class);
        if (wishlistOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Wishlist is empty.");
        }

        Wishlist wishlist = (Wishlist) wishlistOpt.get();
        return wishlist.clear(token);
    }

// ============= ORDER FUNCTIONS =============

    public ResultReturn createOrder(JsonWebToken jwt, CreateOrderPayload payload) {
        LoggerManager.quickLog(this, "Called createOrder...");
        return orderRepositoryService.createOrder(jwt, payload);
    }

    public ResultPayload<Order> getOrder(JsonWebToken jwt, UUID orderId) {
        LoggerManager.quickLog(this, "Called getOrder...");
        return orderRepositoryService.getOrder(jwt, orderId);
    }

    public ResultPayload<List<Order>> getUserOrders(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called getUserOrders...");
        return orderRepositoryService.getUserOrders(jwt);
    }

    public ResultReturn updateOrderStatus(JsonWebToken jwt, UUID orderId, UpdateOrderStatusPayload payload) {
        LoggerManager.quickLog(this, "Called updateOrderStatus...");
        return orderRepositoryService.updateOrderStatus(jwt, orderId, payload);
    }

    public ResultPayload<List<Order>> listAllOrders(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called listAllOrders...");
        return orderRepositoryService.listAllOrders(jwt);
    }

// ============= PRODUCT IMAGE FUNCTIONS =============

    public ResultReturn addProductImage(JsonWebToken jwt, UUID productId, String imageUrl) {
        LoggerManager.quickLog(this, "Called addProductImage...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (productId == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product ID is null.");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Image URL is null or empty.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        ResultPayload<Product> productResult = productRepositoryService.getProductById(productId);
        if (!productResult.resultMessage().isSuccess() || productResult.payload() == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product product = productResult.payload();

        // Check if vendor owns this product
        if (!product.getVendorIdPublic().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_PRODUCTS);
            } catch (AccessDeniedException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
            }
        }

        return product.addImage(token, imageUrl);
    }

    public ResultReturn removeProductImage(JsonWebToken jwt, UUID productId, String imageUrl) {
        LoggerManager.quickLog(this, "Called removeProductImage...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (productId == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product ID is null.");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Image URL is null or empty.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        ResultPayload<Product> productResult = productRepositoryService.getProductById(productId);
        if (!productResult.resultMessage().isSuccess() || productResult.payload() == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product product = productResult.payload();

        if (!product.getVendorIdPublic().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_PRODUCTS);
            } catch (AccessDeniedException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
            }
        }

        return product.removeImage(token, imageUrl);
    }

    public ResultReturn setProductMainImage(JsonWebToken jwt, UUID productId, String mainImageUrl) {
        LoggerManager.quickLog(this, "Called setProductMainImage...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (productId == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product ID is null.");
        }
        if (mainImageUrl == null || mainImageUrl.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Main image URL is null or empty.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        ResultPayload<Product> productResult = productRepositoryService.getProductById(productId);
        if (!productResult.resultMessage().isSuccess() || productResult.payload() == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product product = productResult.payload();

        if (!product.getVendorIdPublic().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_PRODUCTS);
            } catch (AccessDeniedException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
            }
        }

        return product.setMainImageUrl(token, mainImageUrl);
    }

    public ResultReturn setProductThumbnail(JsonWebToken jwt, UUID productId, String thumbnailUrl) {
        LoggerManager.quickLog(this, "Called setProductThumbnail...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (productId == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product ID is null.");
        }
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Thumbnail URL is null or empty.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        ResultPayload<Product> productResult = productRepositoryService.getProductById(productId);
        if (!productResult.resultMessage().isSuccess() || productResult.payload() == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product product = productResult.payload();

        if (!product.getVendorIdPublic().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_PRODUCTS);
            } catch (AccessDeniedException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
            }
        }

        return product.setThumbnailUrl(token, thumbnailUrl);
    }

    public ProductRepositoryService getProductRepositoryService() {
        return productRepositoryService;
    }

    // ============= CUSTOMER PROFILE FUNCTIONS =============

    public ResultPayload<CustomerService.CustomerFullProfile> getCustomerProfile(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called getCustomerProfile...");

        if (jwt == null) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."), null);
        }

        LoggerManager.quickLog(this, "Fetching user...");
        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User user = userOpt.get();
        LoggerManager.quickLog(this, "Getting token...");
        RoleToken token = user.getToken();
        LoggerManager.quickLog(this, "Getting customer profile...");
        CustomerService.CustomerFullProfile profile = user.getCustomerService().toFullProfile(token);
        LoggerManager.quickLog(this, "Profile fetched.");
        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "Customer profile fetched successfully."), profile);
    }

    public ResultReturn updateCustomerShippingAddress(JsonWebToken jwt, Address shippingAddress) {
        LoggerManager.quickLog(this, "Called updateCustomerShippingAddress...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (shippingAddress == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Shipping address is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        return user.getCustomerService().setShippingAddress(token, shippingAddress);
    }

    public ResultReturn updateCustomerBillingAddress(JsonWebToken jwt, Address billingAddress) {
        LoggerManager.quickLog(this, "Called updateCustomerBillingAddress...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (billingAddress == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Billing address is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        return user.getCustomerService().setBillingAddress(token, billingAddress);
    }

    public ResultReturn updateCustomerCountry(JsonWebToken jwt, String countryCode) {
        LoggerManager.quickLog(this, "Called updateCustomerCountry...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (countryCode == null || countryCode.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Country code is null or empty.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Country country = Country.fromCode(countryCode);
        if (country == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid country code: " + countryCode);
        }

        return user.getCustomerService().setCountry(token, country);
    }

    public ResultReturn updateCustomerLanguage(JsonWebToken jwt, String languageCode) {
        LoggerManager.quickLog(this, "Called updateCustomerLanguage...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (languageCode == null || languageCode.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Language code is null or empty.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Language language = Language.fromCode(languageCode);
        if (language == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid language code: " + languageCode);
        }

        return user.getCustomerService().setLanguage(token, language);
    }

    public ResultReturn updateCustomerCurrency(JsonWebToken jwt, String currencyCode) {
        LoggerManager.quickLog(this, "Called updateCustomerCurrency...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Currency code is null or empty.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Currency currency = Currency.fromCode(currencyCode);
        if (currency == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid currency code: " + currencyCode);
        }

        return user.getCustomerService().setCurrency(token, currency);
    }

    public ResultPayload<CustomerService.CustomerFullProfile> getCustomerProfileAdmin(JsonWebToken jwt, UUID userId) {
        LoggerManager.quickLog(this, "Called getCustomerProfileAdmin...");

        if (jwt == null) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."), null);
        }
        if (userId == null) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "User ID is null."), null);
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(jwt);
        if (adminOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User admin = adminOpt.get();
        RoleToken adminToken = admin.getToken();

        // Check admin permission
        try {
            PermissionChecker.checkPermission(adminToken, Permission.VIEW_ANY_USERS);
        } catch (AccessDeniedException e) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions."),
                    null
            );
        }

        Optional<User> targetOpt = userRepositoryService.getUserById(userId);
        if (targetOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Target user not found."),
                    null
            );
        }

        User target = targetOpt.get();
        CustomerService.CustomerFullProfile profile = target.getCustomerService().toFullProfileAdmin(admin.getToken());

        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Customer profile fetched successfully."),
                profile
        );
    }

    // ============= VENDOR REGISTRATION =============

    public ResultReturn registerVendor(JsonWebToken jwt, RegisterVendorPayload payload) {
        LoggerManager.quickLog(this, "Called registerVendor...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        // Check if already registered as vendor
        if (vendorRepositoryService.isVendorRegistered(user.getId())) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Already registered as a vendor.");
        }

        // Register vendor in repository (creates VendorService with UUID only)
        ResultReturn registerResult = vendorRepositoryService.registerVendor(jwt);
        if (!registerResult.isSuccess()) {
            return registerResult;
        }

        // Initialize vendor with payload data
        ResultReturn initResult = user.registerVendor(token, payload);
        if (!initResult.isSuccess()) {
            // Rollback: remove from repository if initialization fails
            vendorRepositoryService.deleteVendor(user.getId());
            return initResult;
        }

        LoggerManager.quickLog(this, "Vendor registered successfully for user: " + UuidFormat.shortenUUID(user.getId()));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Vendor registration submitted. Waiting for approval.");
    }

    // ============= VENDOR PROFILE UPDATE FUNCTIONS =============

    private User getVendorUser(JsonWebToken jwt, RoleToken token) {
        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return null;
        }
        return userOpt.get();
    }

    public ResultReturn updateVendorShopName(JsonWebToken jwt, UpdateVendorShopNamePayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorShopName...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setShopName(token, payload.shopName());
    }

    public ResultReturn updateVendorShopDescription(JsonWebToken jwt, UpdateVendorShopDescriptionPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorShopDescription...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setShopDescription(token, payload.shopDescription());
    }

    public ResultReturn updateVendorCategory(JsonWebToken jwt, UpdateVendorCategoryPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorCategory...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        Category category = Category.fromString(payload.category());
        if (category == null) return failure("Invalid category: " + payload.category());

        return user.getVendorService().setCategory(token, category);
    }

    public ResultReturn updateVendorShopLogo(JsonWebToken jwt, UpdateVendorShopLogoPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorShopLogo...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setShopLogoUrl(token, payload.shopLogoUrl());
    }

    public ResultReturn updateVendorShopBanner(JsonWebToken jwt, UpdateVendorShopBannerPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorShopBanner...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setShopBannerUrl(token, payload.shopBannerUrl());
    }

    public ResultReturn updateVendorShopTagline(JsonWebToken jwt, UpdateVendorShopTaglinePayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorShopTagline...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setShopTagline(token, payload.shopTagline());
    }

    public ResultReturn updateVendorBusinessEmail(JsonWebToken jwt, UpdateVendorBusinessEmailPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorBusinessEmail...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setBusinessEmail(token, payload.businessEmail());
    }

    public ResultReturn updateVendorBusinessPhone(JsonWebToken jwt, UpdateVendorBusinessPhonePayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorBusinessPhone...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setBusinessPhone(token, payload.businessPhone());
    }

    public ResultReturn updateVendorWebsiteUrl(JsonWebToken jwt, UpdateVendorWebsiteUrlPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorWebsiteUrl...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setWebsiteUrl(token, payload.websiteUrl());
    }

    public ResultReturn updateVendorAddress(JsonWebToken jwt, UpdateVendorAddressPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorAddress...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setAddress(token, payload.address(), payload.city(), payload.state(), payload.countryCode(), payload.postalCode());
    }

    public ResultReturn addVendorSocialLink(JsonWebToken jwt, UpdateVendorSocialLinkPayload payload) {
        LoggerManager.quickLog(this, "Called addVendorSocialLink...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().addSocialLink(token, payload.platform(), payload.url());
    }

    public ResultReturn removeVendorSocialLink(JsonWebToken jwt, UpdateVendorSocialLinkPayload payload) {
        LoggerManager.quickLog(this, "Called removeVendorSocialLink...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().removeSocialLink(token, payload.platform());
    }

    public ResultReturn updateVendorTaxId(JsonWebToken jwt, UpdateVendorTaxIdPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorTaxId...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setTaxId(token, payload.taxId());
    }

    public ResultReturn updateVendorBusinessRegistration(JsonWebToken jwt, UpdateVendorBusinessRegistrationPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorBusinessRegistration...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setBusinessRegistrationNumber(token, payload.businessRegistrationNumber());
    }

    public ResultReturn updateVendorBusinessType(JsonWebToken jwt, UpdateVendorBusinessTypePayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorBusinessType...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setBusinessType(token, payload.businessType());
    }

    public ResultReturn updateVendorLegalName(JsonWebToken jwt, UpdateVendorLegalNamePayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorLegalName...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setLegalName(token, payload.legalName());
    }

    public ResultReturn updateVendorYearEstablished(JsonWebToken jwt, UpdateVendorYearEstablishedPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorYearEstablished...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setYearEstablished(token, payload.yearEstablished());
    }

    public ResultReturn updateVendorStoreTheme(JsonWebToken jwt, UpdateVendorStoreThemePayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorStoreTheme...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setStoreTheme(token, payload.storeTheme());
    }

    public ResultReturn updateVendorStoreLanguage(JsonWebToken jwt, UpdateVendorStoreLanguagePayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorStoreLanguage...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setStoreLanguage(token, payload.storeLanguage());
    }

    public ResultReturn updateVendorStoreCurrency(JsonWebToken jwt, UpdateVendorStoreCurrencyPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorStoreCurrency...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setStoreCurrency(token, payload.storeCurrency());
    }

    public ResultReturn updateVendorStoreVisibility(JsonWebToken jwt, UpdateVendorStoreVisibilityPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorStoreVisibility...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().setStoreVisible(token, payload.isStoreVisible());
    }

    public ResultReturn updateVendorReturnPolicy(JsonWebToken jwt, UpdateVendorReturnPolicyPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorReturnPolicy...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();

        ResultReturn result = user.getVendorService().setAcceptReturns(token, payload.acceptReturns());
        if (!result.isSuccess()) return result;

        if (payload.returnWindowDays() != null) {
            result = user.getVendorService().setReturnWindowDays(token, payload.returnWindowDays());
            if (!result.isSuccess()) return result;
        }

        if (payload.returnPolicy() != null) {
            result = user.getVendorService().setReturnPolicy(token, payload.returnPolicy());
        }
        return result;
    }

    public ResultReturn updateVendorShippingPolicy(JsonWebToken jwt, UpdateVendorShippingPolicyPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorShippingPolicy...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        VendorService vendor = user.getVendorService();

        if (payload.shippingPolicy() != null) {
            ResultReturn result = vendor.setShippingPolicy(token, payload.shippingPolicy());
            if (!result.isSuccess()) return result;
        }
        if (payload.freeShippingThreshold() != null) {
            ResultReturn result = vendor.setFreeShippingThreshold(token, payload.freeShippingThreshold());
            if (!result.isSuccess()) return result;
        }
        if (payload.domesticShippingRate() != null) {
            ResultReturn result = vendor.setDomesticShippingRate(token, payload.domesticShippingRate());
            if (!result.isSuccess()) return result;
        }
        if (payload.internationalShippingRate() != null) {
            ResultReturn result = vendor.setInternationalShippingRate(token, payload.internationalShippingRate());
            if (!result.isSuccess()) return result;
        }
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shipping policy updated.");
    }

    public ResultReturn addVendorShopImage(JsonWebToken jwt, String imageUrl) {
        LoggerManager.quickLog(this, "Called addVendorShopImage...");
        if (jwt == null || imageUrl == null || imageUrl.isBlank()) return failure("JWT or image URL is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().addShopImage(token, imageUrl);
    }

    public ResultReturn removeVendorShopImage(JsonWebToken jwt, String imageUrl) {
        LoggerManager.quickLog(this, "Called removeVendorShopImage...");
        if (jwt == null || imageUrl == null || imageUrl.isBlank()) return failure("JWT or image URL is null.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) return failure("Invalid JWT.");

        User user = userOpt.get();
        RoleToken token = user.getToken();
        return user.getVendorService().removeShopImage(token, imageUrl);
    }

    public ResultReturn updateVendorShopImages(JsonWebToken jwt, UpdateVendorShopImagesPayload payload) {
        LoggerManager.quickLog(this, "Called updateVendorShopImages...");
        if (jwt == null || payload == null) return failure("JWT or payload is null.");

        // Clear existing and add new ones
        ResultReturn result = removeVendorShopImage(jwt, "");
        if (!result.isSuccess()) return result;

        for (String imageUrl : payload.shopImageUrls()) {
            result = addVendorShopImage(jwt, imageUrl);
            if (!result.isSuccess()) return result;
        }
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Shop images updated.");
    }

    // ============= PRODUCT MANAGEMENT FUNCTIONS =============

    public ResultReturn createProduct(JsonWebToken jwt, ProductCreatePayload payload) {
        LoggerManager.quickLog(this, "Called createProduct...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        // Check vendor permission
        try {
            PermissionChecker.checkPermission(token, Permission.CREATE_PRODUCT);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions to create product.");
        }

        // Check if vendorId matches the authenticated user
        if (!user.getId().equals(payload.vendorId())) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor ID does not match authenticated user.");
        }

        // Check if user has an active vendor status
        if (!user.isVendorActive()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor is not active. Please wait for admin approval.");
        }

        // Check SKU uniqueness
        Sku sku = new Sku(payload.sku());
        if (productRepositoryService.productExistsBySku(sku)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "SKU already exists.");
        }

        try {
            ResultReturn result = productRepositoryService.createProduct(jwt, payload);
            if (result.isSuccess()) {
                // Update vendor's total products count via service
                vendorRepositoryService.incrementVendorProductCount(user.getId());
            }
            return result;
        } catch (IllegalFieldException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, e.getMessage());
        }
    }

    public ResultPayload<List<Product.PublicProduct>> listAllProductsPublic() {
        LoggerManager.quickLog(this, "Called listAllProductsPublic...");
        return productRepositoryService.listAllProductsPublic();
    }

    public ResultPayload<List<Product.PublicProduct>> getProductsByCategoryPublic(String category) {
        LoggerManager.quickLog(this, "Called getProductsByCategoryPublic...");
        Subcategory subcategory = Subcategory.fromString(category);
        if (subcategory == null) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Invalid category."),
                    List.of()
            );
        }
        return productRepositoryService.getProductsByCategoryPublic(subcategory);
    }

    public ResultPayload<List<Product.PublicProduct>> searchProductsPublic(String query) {
        LoggerManager.quickLog(this, "Called searchProductsPublic...");
        return productRepositoryService.searchProductsPublic(query);
    }

    public ResultPayload<Product.PublicProduct> getProductPublic(UUID productId) {
        LoggerManager.quickLog(this, "Called getProductPublic...");
        return productRepositoryService.getProductPublic(productId);
    }

    public ResultPayload<Product> getProductForVendor(JsonWebToken jwt, UUID productId) {
        LoggerManager.quickLog(this, "Called getProductForVendor...");
        return productRepositoryService.getProductForVendor(jwt, productId);
    }

    public ResultReturn updateProduct(JsonWebToken jwt, UpdateProductPayload payload) {
        LoggerManager.quickLog(this, "Called updateProduct...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (payload == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Payload is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        // Extract productId from payload
        UUID productId = payload.productId();
        if (productId == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product ID is null.");
        }

        ResultPayload<Product> productResult = productRepositoryService.getProductById(productId);
        if (!productResult.resultMessage().isSuccess() || productResult.payload() == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product product = productResult.payload();

        // Check if vendor owns this product
        if (!product.getVendorIdPublic().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_PRODUCTS);
            } catch (AccessDeniedException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
            }
        }

        // Update fields if present
        if (payload.name() != null) {
            ResultReturn result = product.setName(token, payload.name());
            if (!result.isSuccess()) return result;
        }
        if (payload.description() != null) {
            ResultReturn result = product.setDescription(token, payload.description());
            if (!result.isSuccess()) return result;
        }
        if (payload.shortDescription() != null) {
            ResultReturn result = product.setShortDescription(token, payload.shortDescription());
            if (!result.isSuccess()) return result;
        }
        if (payload.brand() != null) {
            ResultReturn result = product.setBrand(token, payload.brand());
            if (!result.isSuccess()) return result;
        }
        if (payload.subcategory() != null) {
            Subcategory category = Subcategory.fromString(payload.subcategory());
            if (category == null) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid subcategory: " + payload.subcategory());
            }
            ResultReturn result = product.setCategory(token, category);
            if (!result.isSuccess()) return result;
        }
        if (payload.currency() != null) {
            Currency currency = Currency.fromCode(payload.currency());
            if (currency == null) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid currency: " + payload.currency());
            }
            ResultReturn result = product.setCurrency(token, currency);
            if (!result.isSuccess()) return result;
        }
        if (payload.price() != null) {
            ResultReturn result = product.setPrice(token, payload.price());
            if (!result.isSuccess()) return result;
        }
        if (payload.compareAtPrice() != null) {
            ResultReturn result = product.setCompareAtPrice(token, payload.compareAtPrice());
            if (!result.isSuccess()) return result;
        }
        if (payload.costPrice() != null) {
            ResultReturn result = product.setCostPrice(token, payload.costPrice());
            if (!result.isSuccess()) return result;
        }
        if (payload.stockQuantity() != null) {
            ResultReturn result = product.setStockQuantity(token, payload.stockQuantity());
            if (!result.isSuccess()) return result;
        }
        if (payload.lowStockThreshold() != null) {
            ResultReturn result = product.setLowStockThreshold(token, payload.lowStockThreshold());
            if (!result.isSuccess()) return result;
        }
        if (payload.trackInventory() != null) {
            ResultReturn result = product.setTrackInventory(token, payload.trackInventory());
            if (!result.isSuccess()) return result;
        }
        if (payload.weight() != null) {
            ResultReturn result = product.setWeight(token, payload.weight());
            if (!result.isSuccess()) return result;
        }
        if (payload.weightUnit() != null) {
            ResultReturn result = product.setWeightUnit(token, payload.weightUnit());
            if (!result.isSuccess()) return result;
        }
        if (payload.dimensionUnit() != null) {
            ResultReturn result = product.setDimensionUnit(token, payload.dimensionUnit());
            if (!result.isSuccess()) return result;
        }
        if (payload.requiresShipping() != null) {
            ResultReturn result = product.setRequiresShipping(token, payload.requiresShipping());
            if (!result.isSuccess()) return result;
        }
        if (payload.requiresSpecialHandling() != null) {
            ResultReturn result = product.setRequiresSpecialHandling(token, payload.requiresSpecialHandling());
            if (!result.isSuccess()) return result;
        }
        if (payload.customsDescription() != null) {
            ResultReturn result = product.setCustomsDescription(token, payload.customsDescription());
            if (!result.isSuccess()) return result;
        }
        if (payload.warrantyInformation() != null) {
            ResultReturn result = product.setWarrantyInformation(token, payload.warrantyInformation());
            if (!result.isSuccess()) return result;
        }
        if (payload.hasReturns() != null) {
            ResultReturn result = product.setHasReturns(token, payload.hasReturns());
            if (!result.isSuccess()) return result;
        }
        if (payload.returnDays() != null) {
            ResultReturn result = product.setReturnDays(token, payload.returnDays());
            if (!result.isSuccess()) return result;
        }
        if (payload.onSale() != null) {
            ResultReturn result = product.setOnSale(token, payload.onSale());
            if (!result.isSuccess()) return result;
        }
        if (payload.customAttributes() != null) {
            for (Map.Entry<String, String> entry : payload.customAttributes().entrySet()) {
                ResultReturn result = product.setCustomAttribute(token, entry.getKey(), entry.getValue());
                if (!result.isSuccess()) return result;
            }
        }
        if (payload.minimumOrderQuantity() != null) {
            ResultReturn result = product.setMinimumOrderQuantity(token, payload.minimumOrderQuantity());
            if (!result.isSuccess()) return result;
        }
        if (payload.maximumOrderQuantity() != null) {
            ResultReturn result = product.setMaximumOrderQuantity(token, payload.maximumOrderQuantity());
            if (!result.isSuccess()) return result;
        }
        if (payload.ageRestricted() != null) {
            ResultReturn result = product.setAgeRestricted(token, payload.ageRestricted());
            if (!result.isSuccess()) return result;
        }
        if (payload.minimumAge() != null) {
            ResultReturn result = product.setMinimumAge(token, payload.minimumAge());
            if (!result.isSuccess()) return result;
        }

        return productRepositoryService.updateProduct(jwt, productId, product);
    }

    public ResultReturn updateProductStatus(JsonWebToken jwt, UUID productId, String status) {
        LoggerManager.quickLog(this, "Called updateProductStatus...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (status == null || status.isBlank()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Status is null or empty.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        ResultPayload<Product> productResult = productRepositoryService.getProductById(productId);
        if (!productResult.resultMessage().isSuccess() || productResult.payload() == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product product = productResult.payload();

        // Check if vendor owns this product
        if (!product.getVendorIdPublic().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_PRODUCTS);
            } catch (AccessDeniedException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
            }
        }

        ProductStatus.Status newStatus;
        try {
            newStatus = ProductStatus.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid status: " + status + ". Valid statuses: DRAFT, ANNOUNCED, ACTIVE, INACTIVE, OUT_OF_STOCK, REMOVED");
        }

        return productRepositoryService.updateProductStatus(jwt, productId, newStatus);
    }

    public ResultReturn updateProductPrice(JsonWebToken jwt, UUID productId, BigDecimal price) {
        LoggerManager.quickLog(this, "Called updateProductPrice...");
        return productRepositoryService.updatePrice(jwt, productId, price);
    }

    public ResultReturn updateProductStock(JsonWebToken jwt, UUID productId, int stockQuantity) {
        LoggerManager.quickLog(this, "Called updateProductStock...");
        return productRepositoryService.updateStock(jwt, productId, stockQuantity);
    }

    public ResultReturn deleteProduct(JsonWebToken jwt, UUID productId) {
        LoggerManager.quickLog(this, "Called deleteProduct...");

        if (jwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        ResultPayload<Product> productResult = productRepositoryService.getProductById(productId);
        if (!productResult.resultMessage().isSuccess() || productResult.payload() == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Product not found.");
        }

        Product product = productResult.payload();

        // Check if vendor owns this product
        if (!product.getVendorIdPublic().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_PRODUCTS);
            } catch (AccessDeniedException e) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
            }
        }

        ResultReturn result = productRepositoryService.deleteProduct(jwt, productId);
        if (result.isSuccess()) {
            vendorRepositoryService.decrementVendorProductCount(user.getId());
        }
        return result;
    }

    private ResultReturn failure(String message) {
        return new ResultReturn(ResultReturn.Result.FAILURE, message);
    }

    public boolean existsManager() {
        LoggerManager.quickLog(this, "Checking if manager exists");
        return userRepositoryService.existsManager();
    }

// ============= ADMIN USER MANAGEMENT FUNCTIONS =============

    public ResultPayload<List<User>> listAllUsers(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Called listAllUsers...");

        if (jwt == null) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."),
                    null
            );
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(jwt);
        if (adminOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."),
                    null
            );
        }

        User admin = adminOpt.get();
        RoleToken adminToken = admin.getToken();

        try {
            PermissionChecker.checkPermission(adminToken, Permission.VIEW_ANY_USERS);
        } catch (AccessDeniedException e) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions."),
                    null
            );
        }

        List<User> users = userRepositoryService.getAllUsers();
        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "Users fetched successfully."),
                users
        );
    }

    public ResultPayload<User> getUserByIdAdmin(JsonWebToken jwt, UUID userId) {
        LoggerManager.quickLog(this, "Called getUserByIdAdmin...");

        if (jwt == null) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null."),
                    null
            );
        }
        if (userId == null) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "User ID is null."),
                    null
            );
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(jwt);
        if (adminOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."),
                    null
            );
        }

        User admin = adminOpt.get();
        RoleToken adminToken = admin.getToken();

        try {
            PermissionChecker.checkPermission(adminToken, Permission.VIEW_ANY_USERS);
        } catch (AccessDeniedException e) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions."),
                    null
            );
        }

        Optional<User> targetOpt = userRepositoryService.getUserById(userId);
        if (targetOpt.isEmpty()) {
            return new ResultPayload<>(
                    new ResultReturn(ResultReturn.Result.FAILURE, "User not found."),
                    null
            );
        }

        return new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, "User fetched successfully."),
                targetOpt.get()
        );
    }

    public ResultReturn grantAdminRole(JsonWebToken adminJwt, UUID userId) {
        LoggerManager.quickLog(this, "Called grantAdminRole...");

        if (adminJwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (userId == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "User ID is null.");
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(adminJwt);
        if (adminOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User admin = adminOpt.get();
        RoleToken adminToken = admin.getToken();

        try {
            PermissionChecker.checkPermission(adminToken, Permission.MANAGE_ANY_ROLES);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        // Only MANAGER can grant ADMIN
        if (!admin.getRoles().contains(UserRole.MANAGER)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Only a MANAGER can grant ADMIN role.");
        }

        Optional<User> targetOpt = userRepositoryService.getUserById(userId);
        if (targetOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Target user not found.");
        }

        User target = targetOpt.get();

        // ADMIN uses its own token to add role to target
        ResultReturn result = target.getRole().addRole(adminToken, UserRole.ADMIN);
        if (result.isSuccess()) {
            userRepositoryService.updateUser(target);
        }

        LoggerManager.quickLog(this, "Admin role granted to user: " + UuidFormat.shortenUUID(userId));
        return result;
    }

    public ResultReturn grantManagerRole(JsonWebToken adminJwt, UUID userId) {
        LoggerManager.quickLog(this, "Called grantManagerRole...");

        if (adminJwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (userId == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "User ID is null.");
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(adminJwt);
        if (adminOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User admin = adminOpt.get();
        RoleToken adminToken = admin.getToken();

        try {
            PermissionChecker.checkPermission(adminToken, Permission.MANAGE_ANY_ROLES);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        // Only MANAGER can grant MANAGER
        if (!admin.getRoles().contains(UserRole.MANAGER)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Only a MANAGER can grant MANAGER role.");
        }

        Optional<User> targetOpt = userRepositoryService.getUserById(userId);
        if (targetOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Target user not found.");
        }

        User target = targetOpt.get();

        ResultReturn result = target.getRole().addRole(adminToken, UserRole.MANAGER);
        if (result.isSuccess()) {
            userRepositoryService.updateUser(target);
        }

        LoggerManager.quickLog(this, "Manager role granted to user: " + UuidFormat.shortenUUID(userId));
        return result;
    }

    public ResultReturn removeAdminRole(JsonWebToken adminJwt, UUID userId) {
        LoggerManager.quickLog(this, "Called removeAdminRole...");

        if (adminJwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (userId == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "User ID is null.");
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(adminJwt);
        if (adminOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User admin = adminOpt.get();
        RoleToken adminToken = admin.getToken();

        try {
            PermissionChecker.checkPermission(adminToken, Permission.MANAGE_ANY_ROLES);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        // Only MANAGER can remove ADMIN
        if (!admin.getRoles().contains(UserRole.MANAGER)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Only a MANAGER can remove ADMIN role.");
        }

        Optional<User> targetOpt = userRepositoryService.getUserById(userId);
        if (targetOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Target user not found.");
        }

        User target = targetOpt.get();

        ResultReturn result = target.getRole().removeRole(adminToken, UserRole.ADMIN);
        if (result.isSuccess()) {
            userRepositoryService.updateUser(target);
        }

        LoggerManager.quickLog(this, "Admin role removed from user: " + UuidFormat.shortenUUID(userId));
        return result;
    }

    public ResultReturn removeManagerRole(JsonWebToken adminJwt, UUID userId) {
        LoggerManager.quickLog(this, "Called removeManagerRole...");

        if (adminJwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (userId == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "User ID is null.");
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(adminJwt);
        if (adminOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User admin = adminOpt.get();
        RoleToken adminToken = admin.getToken();

        try {
            PermissionChecker.checkPermission(adminToken, Permission.MANAGE_ANY_ROLES);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        // Only MANAGER can remove MANAGER
        if (!admin.getRoles().contains(UserRole.MANAGER)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Only a MANAGER can remove MANAGER role.");
        }

        // Cannot remove your own MANAGER role
        if (admin.getId().equals(userId)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Cannot remove your own MANAGER role.");
        }

        Optional<User> targetOpt = userRepositoryService.getUserById(userId);
        if (targetOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Target user not found.");
        }

        User target = targetOpt.get();

        ResultReturn result = target.getRole().removeRole(adminToken, UserRole.MANAGER);
        if (result.isSuccess()) {
            userRepositoryService.updateUser(target);
        }

        LoggerManager.quickLog(this, "Manager role removed from user: " + UuidFormat.shortenUUID(userId));
        return result;
    }

// ============= USER ENABLE/DISABLE FUNCTIONS =============

    public ResultReturn disableUser(JsonWebToken adminJwt, UUID userId) {
        LoggerManager.quickLog(this, "Called disableUser...");

        if (adminJwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (userId == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "User ID is null.");
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(adminJwt);
        if (adminOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User admin = adminOpt.get();
        RoleToken adminToken = admin.getToken();

        try {
            PermissionChecker.checkPermission(adminToken, Permission.DISABLE_ANY_USER);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        // Cannot disable yourself
        if (admin.getId().equals(userId)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Cannot disable your own account.");
        }

        Optional<User> targetOpt = userRepositoryService.getUserById(userId);
        if (targetOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Target user not found.");
        }

        User target = targetOpt.get();

        // Only MANAGER can disable MANAGER or ADMIN
        if (target.getRoles().contains(UserRole.MANAGER) || target.getRoles().contains(UserRole.ADMIN)) {
            if (!admin.getRoles().contains(UserRole.MANAGER)) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Only a MANAGER can disable an ADMIN or MANAGER.");
            }
        }

        // Admin uses its own token to disable the target user
        ResultReturn result = target.disableUser(adminToken);

        if (result.isSuccess()) {
            userRepositoryService.updateUser(target);
        }

        LoggerManager.quickLog(this, "User disabled: " + UuidFormat.shortenUUID(userId));
        return result;
    }

    public ResultReturn enableUser(JsonWebToken adminJwt, UUID userId) {
        LoggerManager.quickLog(this, "Called enableUser...");

        if (adminJwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (userId == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "User ID is null.");
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(adminJwt);
        if (adminOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User admin = adminOpt.get();
        RoleToken adminToken = admin.getToken();

        try {
            PermissionChecker.checkPermission(adminToken, Permission.DISABLE_ANY_USER);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        Optional<User> targetOpt = userRepositoryService.getUserById(userId);
        if (targetOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Target user not found.");
        }

        User target = targetOpt.get();

        // Only MANAGER can enable MANAGER or ADMIN
        if (target.getRoles().contains(UserRole.MANAGER) || target.getRoles().contains(UserRole.ADMIN)) {
            if (!admin.getRoles().contains(UserRole.MANAGER)) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Only a MANAGER can enable an ADMIN or MANAGER.");
            }
        }

        // Admin uses its own token to enable the target user
        ResultReturn result = target.enableUser(adminToken);

        if (result.isSuccess()) {
            userRepositoryService.updateUser(target);
        }

        LoggerManager.quickLog(this, "User enabled: " + UuidFormat.shortenUUID(userId));
        return result;
    }

// ============= VENDOR ENABLE/DISABLE FUNCTIONS =============

    public ResultReturn disableVendor(JsonWebToken adminJwt, UUID userId) {
        LoggerManager.quickLog(this, "Called disableVendor...");

        if (adminJwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (userId == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "User ID is null.");
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(adminJwt);
        if (adminOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User admin = adminOpt.get();
        RoleToken adminToken = admin.getToken();

        try {
            PermissionChecker.checkPermission(adminToken, Permission.DISABLE_ANY_VENDOR);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        Optional<User> targetOpt = userRepositoryService.getUserById(userId);
        if (targetOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Target user not found.");
        }

        User target = targetOpt.get();

        // Check if user is a vendor
        if (!target.isVendor()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Target user is not a vendor.");
        }

        // Only MANAGER can disable MANAGER or ADMIN vendors
        if (target.getRoles().contains(UserRole.MANAGER) || target.getRoles().contains(UserRole.ADMIN)) {
            if (!admin.getRoles().contains(UserRole.MANAGER)) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Only a MANAGER can disable an ADMIN or MANAGER vendor.");
            }
        }

        // Admin uses its own token to disable vendor
        ResultReturn result = target.disableVendor(adminToken);

        if (result.isSuccess()) {
            vendorRepositoryService.disableVendor(adminJwt, userId);
            userRepositoryService.updateUser(target);
        }

        LoggerManager.quickLog(this, "Vendor disabled for user: " + UuidFormat.shortenUUID(userId));
        return result;
    }

    public ResultReturn enableVendor(JsonWebToken adminJwt, UUID userId) {
        LoggerManager.quickLog(this, "Called enableVendor...");

        if (adminJwt == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "JWT is null.");
        }
        if (userId == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "User ID is null.");
        }

        Optional<User> adminOpt = userRepositoryService.getUserFromJwt(adminJwt);
        if (adminOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User admin = adminOpt.get();

        // Check if vendor exists and is inactive
        if (!vendorRepositoryService.isVendorRegistered(userId)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor not found.");
        }

        if (vendorRepositoryService.isVendorActive(userId)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Vendor is already active.");
        }

        Optional<User> targetOpt = userRepositoryService.getUserById(userId);
        if (targetOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Target user not found.");
        }

        User target = targetOpt.get();

        // Only MANAGER can enable MANAGER or ADMIN vendors
        if (target.getRoles().contains(UserRole.MANAGER) || target.getRoles().contains(UserRole.ADMIN)) {
            if (!admin.getRoles().contains(UserRole.MANAGER)) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Only a MANAGER can enable an ADMIN or MANAGER vendor.");
            }
        }

        // STEP 1: Activate vendor service (admin uses its own token)
        RoleToken adminToken1 = admin.getToken();
        ResultReturn step1 = target.approveVendorStep1(adminToken1);
        if (!step1.isSuccess()) {
            return step1;
        }

        // STEP 2: Add VENDOR role (admin uses its own token)
        RoleToken adminToken2 = admin.getToken();
        ResultReturn step2 = target.approveVendorStep2(adminToken2);
        if (!step2.isSuccess()) {
            return step2;
        }

        // STEP 3: Update repository status (admin uses its own token)
        RoleToken adminToken3 = admin.getToken();
        ResultReturn step3 = target.approveVendorStep3(adminToken3);
        if (!step3.isSuccess()) {
            return step3;
        }

        // Update vendor repository
        ResultReturn repoResult = vendorRepositoryService.approveVendorStep3(adminToken3, userId);
        if (!repoResult.isSuccess()) {
            return repoResult;
        }

        // Update user in repository
        userRepositoryService.updateUser(target);

        LoggerManager.quickLog(this, "Vendor enabled for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Vendor enabled successfully.");
    }
}
