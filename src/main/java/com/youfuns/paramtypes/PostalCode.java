package com.youfuns.paramtypes;

import java.util.regex.Pattern;

public record PostalCode(String value) implements ValidatedValue {
    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile(
            "^(?=.*\\d)[A-Z0-9\\s-]{4,10}$"
    );

    public PostalCode {
        validate(value);
    }

    @Override
    public Pattern validationPattern() { return POSTAL_CODE_PATTERN; }

    @Override
    public ParamType paramType() { return ParamType.ADDRESS; }
}
