package com.youfuns.auth.login;

import java.util.function.Function;

public final class SessionTokenService {
    // Set this yourself using AuthManager.Builder.sessionTokenService.
    static Function<String, String> generateToken = str -> "SESSION_TOKEN[" + str + "]";
    static Function<String, String> extractSubject = str -> str.replaceAll(".*?SESSION_TOKEN\\[(.*?)\\].*", "$1");;

    public static String generateToken(String subject) {
        return generateToken.apply(subject);
    }
    public static String extractSubject(String token) {
        return extractSubject.apply(token);
    }
    public static boolean validateToken(String token) {
        try {
            return extractSubject.apply(token) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
