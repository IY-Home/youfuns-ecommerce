package com.youfuns.paramtypes;

import com.youfuns.exceptions.IllegalFieldException;

import java.util.regex.Pattern;

public record JsonWebToken(String token) implements ValidatedValue {
    private static final Pattern JWT_PATTERN = Pattern.compile(
            "^eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$"
    );

    public JsonWebToken {
        validate(token);
    }

    @Override
    public Pattern validationPattern() { return JWT_PATTERN; }

    @Override
    public ParamType paramType() { return ParamType.JWT; }
}

