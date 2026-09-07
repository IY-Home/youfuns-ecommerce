package com.youfuns.auth.rbac;

import java.util.UUID;

public record RoleToken(UUID id, UUID issuedUser) {
}
