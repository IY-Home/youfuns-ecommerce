package com.youfuns.ecommerce.frontend.payloads;

import java.util.UUID;

public record AdminChangePasswordPayload(
        UUID userId,
        String newPassword
) {}
