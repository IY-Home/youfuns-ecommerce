package com.youfuns.ecommerce.products;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.auth.Permission;
import com.youfuns.ecommerce.auth.PermissionChecker;
import com.youfuns.ecommerce.auth.RoleToken;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.paramtypes.Rating;
import com.youfuns.paramtypes.Review;
import com.youfuns.paramtypes.UuidFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ReviewManager {
    private final List<Review> reviews;
    private final UUID productId;
    private double averageRating;
    private int reviewCount;

    public ReviewManager(UUID productId) {
        this.productId = productId;
        this.reviews = new ArrayList<>();
        this.averageRating = 0.0;
        this.reviewCount = 0;
        LoggerManager.quickLog(this, "Created ReviewManager for product: " + UuidFormat.shortenUUID(productId));
    }

    // ============= ADD REVIEW (Requires Token) =============

    public ResultReturn addReview(RoleToken rt, UUID userId, Rating rating, String reviewText) {
        LoggerManager.quickLog(this, "Adding review for user: " + UuidFormat.shortenUUID(userId));

        try {
            PermissionChecker.checkPermission(rt, Permission.REVIEW_ANY_PRODUCTS);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions to review products.");
        }

        if (rating == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Rating cannot be null.");
        }

        if (hasUserReviewed(userId)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "You have already reviewed this product.");
        }

        Review review = new Review(userId, productId, rating, reviewText);
        reviews.add(review);
        recalculateAverage();

        LoggerManager.quickLog(this, "Review added by user: " + UuidFormat.shortenUUID(userId) + " - Rating: " + rating);
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Review added successfully.");
    }

    // ============= REMOVE REVIEW (Requires Token) =============

    public ResultReturn removeReview(RoleToken rt, UUID userId) {
        LoggerManager.quickLog(this, "Removing review for user: " + UuidFormat.shortenUUID(userId));

        try {
            PermissionChecker.checkPermission(rt, Permission.REVIEW_ANY_PRODUCTS);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        boolean removed = reviews.removeIf(review -> review.userId().equals(userId));
        if (!removed) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Review not found.");
        }

        recalculateAverage();
        LoggerManager.quickLog(this, "Review removed for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Review removed successfully.");
    }

    public ResultReturn removeReviewAdmin(RoleToken rt, UUID userId) {
        LoggerManager.quickLog(this, "Removing review by admin for user: " + UuidFormat.shortenUUID(userId));

        try {
            PermissionChecker.checkPermission(rt, Permission.MANAGE_ANY_PRODUCTS);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        boolean removed = reviews.removeIf(review -> review.userId().equals(userId));
        if (!removed) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Review not found.");
        }

        recalculateAverage();
        LoggerManager.quickLog(this, "Review removed by admin for user: " + UuidFormat.shortenUUID(userId));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Review removed successfully.");
    }

    // ============= UPDATE REVIEW (Requires Token) =============

    public ResultReturn updateReview(RoleToken rt, UUID userId, Rating newRating, String newReviewText) {
        LoggerManager.quickLog(this, "Updating review for user: " + UuidFormat.shortenUUID(userId));

        try {
            PermissionChecker.checkPermission(rt, Permission.REVIEW_ANY_PRODUCTS);
        } catch (AccessDeniedException e) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Insufficient permissions.");
        }

        if (newRating == null) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Rating cannot be null.");
        }

        for (int i = 0; i < reviews.size(); i++) {
            Review existing = reviews.get(i);
            if (existing.userId().equals(userId)) {
                Review updated = new Review(userId, productId, newRating, newReviewText, existing.createdAt());
                reviews.set(i, updated);
                recalculateAverage();

                LoggerManager.quickLog(this, "Review updated for user: " + UuidFormat.shortenUUID(userId));
                return new ResultReturn(ResultReturn.Result.SUCCESS, "Review updated successfully.");
            }
        }

        return new ResultReturn(ResultReturn.Result.FAILURE, "Review not found.");
    }

    // ============= PUBLIC READ OPERATIONS (NO TOKEN REQUIRED) =============

    public List<Review> getReviews() {
        return List.copyOf(reviews);
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public boolean hasUserReviewed(UUID userId) {
        return reviews.stream().anyMatch(review -> review.userId().equals(userId));
    }

    public List<Review> getReviewsByRating(Rating rating) {
        return reviews.stream()
                .filter(review -> review.rating() == rating)
                .toList();
    }

    public List<Review> getRecentReviews(int limit) {
        return reviews.stream()
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .limit(limit)
                .toList();
    }

    // ============= HELPER METHODS =============

    private void recalculateAverage() {
        this.reviewCount = reviews.size();
        if (reviewCount == 0) {
            this.averageRating = 0.0;
            return;
        }

        int totalStars = reviews.stream()
                .mapToInt(review -> review.rating().ordinal() + 1)
                .sum();

        this.averageRating = Math.round((double) totalStars / reviewCount * 10.0) / 10.0;
        LoggerManager.quickLog(this, "Average rating recalculated: " + averageRating + " from " + reviewCount + " reviews");
    }

    // ============= CLEAR =============

    public void clear() {
        reviews.clear();
        averageRating = 0.0;
        reviewCount = 0;
        LoggerManager.quickLog(this, "ReviewManager cleared.");
    }
}