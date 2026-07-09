package com.youfuns.ecommerce.frontend.payloads;

public record ChangePasswordPayload(
        String oldPassword,
        String newPassword
) {}
