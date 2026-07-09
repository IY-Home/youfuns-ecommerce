package com.youfuns.paramtypes;

import com.youfuns.exceptions.IllegalFieldException;
import java.util.regex.Pattern;

public record Username(String username) implements ValidatedValue {
    // Alphanumeric, underscore, dot, hyphen - 3 to 30 characters
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._-]{3,30}$"
    );

    public Username {
        validate(username);
    }

    @Override
    public Pattern validationPattern() { return USERNAME_PATTERN; }

    @Override
    public ParamType paramType() { return ParamType.USERNAME; }
}