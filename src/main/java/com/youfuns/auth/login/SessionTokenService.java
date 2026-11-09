package com.youfuns.auth.login;

import java.util.function.Function;

public final class SessionTokenService {
    // Set this using AuthManager.Builder.sessionTokenService.
    private Function<String, String> generateToken = str -> "SESSION_TOKEN[" + str + "]";
    private Function<String, String> extractSubject = str -> str.replaceAll(".*?SESSION_TOKEN\\[(.*?)\\].*", "$1");

    public String generateToken(String subject) {
        return generateToken.apply(subject);
    }
    public String extractSubject(String token) {
        return extractSubject.apply(token);
    }
    public boolean validateToken(String token) {
        try {
            return extractSubject.apply(token) != null;
        } catch (Exception e) {
            return false;
        }
    }
    public void set(Function<String, String> generateToken, Function<String, String> extractSubject) {
        this.generateToken = generateToken;
        this.extractSubject = extractSubject;
    }
}
