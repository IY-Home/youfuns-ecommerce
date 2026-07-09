package com.youfuns.paramtypes;

import java.time.LocalDateTime;
import java.util.UUID;

public record Review(
        UUID userId,
        UUID productId,
        Rating rating,
        String reviewText,
        LocalDateTime createdAt
) {
    public Review(UUID userId, UUID productId, Rating rating, String reviewText) {
        this(userId, productId, rating, reviewText, LocalDateTime.now());
    }
}