package com.youfuns.ecommerce.frontend.payloads;

import com.youfuns.paramtypes.Address;

public record CreateOrderPayload(
        Address shippingAddress,
        Address billingAddress,
        String paymentMethod
) {}