package com.youfuns.ecommerce.frontend.payloads;

import java.time.LocalDate;
import java.util.UUID;

public record AdminUpdateProfilePayload(
        UUID userId,
        String name,
        String phone,
        LocalDate dateOfBirth,
        String profilePicturePath
) {}
