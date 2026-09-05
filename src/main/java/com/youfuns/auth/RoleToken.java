package com.youfuns.auth;

import java.time.Instant;
import java.util.UUID;

public record RoleToken(UUID id, UUID issuedUser) {
}
