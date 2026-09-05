package com.youfuns.cms.user;

public record UserRegistrationPayload(String name, String email, String phone, String username, String password) {
}
