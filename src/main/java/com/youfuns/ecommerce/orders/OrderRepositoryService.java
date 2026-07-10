package com.youfuns.ecommerce.orders;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.Permission;
import com.youfuns.ecommerce.auth.PermissionChecker;
import com.youfuns.ecommerce.auth.RoleToken;
import com.youfuns.ecommerce.auth.UserRepositoryService;
import com.youfuns.ecommerce.frontend.payloads.CreateOrderPayload;
import com.youfuns.ecommerce.frontend.payloads.UpdateOrderStatusPayload;
import com.youfuns.ecommerce.frontend.utils.ResultPayload;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.ecommerce.products.*;
import com.youfuns.ecommerce.status.OrderStatus;
import com.youfuns.ecommerce.user.User;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.paramtypes.JsonWebToken;
import com.youfuns.paramtypes.UuidFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderRepositoryService {
    private final OrderRepository orderRepository;
    private final ProductListRepository productListRepository;
    private final ProductRepositoryService productRepositoryService;
    private final UserRepositoryService userRepositoryService;

    public OrderRepositoryService(UserRepositoryService userRepositoryService,
                                  ProductListRepository productListRepository,
                                  ProductRepositoryService productRepositoryService) {
        this.orderRepository = new OrderRepository();
        this.userRepositoryService = userRepositoryService;
        this.productListRepository = productListRepository;
        this.productRepositoryService = productRepositoryService;
        LoggerManager.quickLog(this, "OrderRepositoryService created.");
    }

    public ResultReturn createOrder(JsonWebToken jwt, CreateOrderPayload payload) {
        LoggerManager.quickLog(this, "Creating order...");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        // Get user's cart
        Optional<ProductList> cartOpt = productListRepository.findByUserId(user.getId(), Cart.class);
        if (cartOpt.isEmpty() || cartOpt.get().isEmpty(token)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Cart is empty.");
        }

        Cart cart = (Cart) cartOpt.get();

        // Validate cart
        ResultReturn validation = cart.validateCart(token);
        if (!validation.isSuccess()) {
            return validation;
        }

        // Build order items
        List<Order.OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ProductList.Entry entry : cart.getEntriesProduct(token)) {
            Product product = entry.product();
            int quantity = entry.quantity();
            BigDecimal price = product.getPricePublic();
            orderItems.add(new Order.OrderItem(
                    product.getProductIdPublic(),
                    product.getNamePublic(),
                    quantity,
                    price
            ));
            total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
        }

        // Create order
        Order order = new Order(
                user.getId(),
                orderItems,
                payload.shippingAddress(),
                payload.billingAddress(),
                total,
                payload.paymentMethod()
        );

        // Clear cart
        cart.clearCart(token);

        // Save order
        return orderRepository.insert(order);
    }

    public ResultPayload<Order> getOrder(JsonWebToken jwt, UUID orderId) {
        LoggerManager.quickLog(this, "Getting order: " + UuidFormat.shortenUUID(orderId));

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Order not found."), null);
        }

        Order order = orderOpt.get();

        // Check if user owns this order or is admin
        if (!order.getUserId().equals(user.getId())) {
            try {
                PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_ORDERS);
            } catch (AccessDeniedException e) {
                return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions."), null);
            }
        }

        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "Order fetched."), order);
    }

    public ResultPayload<List<Order>> getUserOrders(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Getting user orders.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User user = userOpt.get();

        List<Order> orders = orderRepository.findByUserId(user.getId());
        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "Orders fetched."), orders);
    }

    public ResultReturn updateOrderStatus(JsonWebToken jwt, UUID orderId, UpdateOrderStatusPayload payload) {
        LoggerManager.quickLog(this, "Updating order status: " + UuidFormat.shortenUUID(orderId));

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT.");
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        // Check admin permission
        try {
            PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_ORDERS);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Order not found.");
        }

        Order order = orderOpt.get();

        OrderStatus.Status status;
        try {
            status = OrderStatus.Status.valueOf(payload.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Invalid status.");
        }

        order.updateStatus(status);
        orderRepository.update(order);

        return new ResultReturn(ResultReturn.Result.SUCCESS, "Order status updated.");
    }

    public ResultPayload<List<Order>> listAllOrders(JsonWebToken jwt) {
        LoggerManager.quickLog(this, "Listing all orders.");

        Optional<User> userOpt = userRepositoryService.getUserFromJwt(jwt);
        if (userOpt.isEmpty()) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Invalid JWT."), null);
        }

        User user = userOpt.get();
        RoleToken token = user.getToken();

        try {
            PermissionChecker.checkPermission(token, Permission.MANAGE_ANY_ORDERS);
        } catch (AccessDeniedException e) {
            return new ResultPayload<>(new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions."), null);
        }

        List<Order> orders = orderRepository.findAll();
        return new ResultPayload<>(new ResultReturn(ResultReturn.Result.SUCCESS, "Orders fetched."), orders);
    }
}