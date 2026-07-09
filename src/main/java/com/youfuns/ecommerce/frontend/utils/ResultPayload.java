package com.youfuns.ecommerce.frontend.utils;

public record ResultPayload<T>(ResultReturn resultMessage, T payload) {}