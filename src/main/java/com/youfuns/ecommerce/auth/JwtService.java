package com.youfuns.ecommerce.auth;

import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.paramtypes.JsonWebToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

final class JwtService {
    private static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();
    private static final long EXPIRATION_SECONDS = 3600; // 1 hour

    // Generate JWT
    static JsonWebToken generateToken(UUID userId) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(EXPIRATION_SECONDS);

        return new JsonWebToken(Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(SECRET_KEY)
                .compact());
    }

    // Validate JWT and extract userId
    static UUID validateTokenAndGetUserId(JsonWebToken token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token.token());

            String userId = jws.getPayload().getSubject();

            if (userId == null) {
                throw new AccessDeniedException("Invalid token: missing subject");
            }

            return UUID.fromString(userId);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AccessDeniedException("Invalid or expired token: " + e.getMessage());
        }
    }

    static boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static String extractUserId(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token);
        return jws.getPayload().getSubject();
    }
}