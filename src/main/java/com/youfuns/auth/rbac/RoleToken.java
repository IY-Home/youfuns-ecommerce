package com.youfuns.auth.rbac;

import java.util.UUID;

// Internal single-use capability token
public record RoleToken(UUID id, UUID issuedUser) {
}
