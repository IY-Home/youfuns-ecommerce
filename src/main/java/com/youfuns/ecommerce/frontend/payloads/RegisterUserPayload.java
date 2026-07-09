package com.youfuns.ecommerce.frontend.payloads;

import java.time.LocalDate;

public record RegisterUserPayload(String name, String email, String phone, String username, String password, LocalDate dateOfBirth, RegisterCustomerPayload registerCustomerPayload) {
}
