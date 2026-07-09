package com.youfuns.ecommerce.frontend.payloads;

import java.time.LocalDate;

public record UpdateProfilePayload(
        String name,
        String phone,
        LocalDate dateOfBirth,
        String profilePicturePath
) {}

