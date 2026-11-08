package com.youfuns.auth.user;

import java.util.Set;

public record UserCredentialPayload(Set<String> usernames, String password) {
}
