package com.youfuns.paramtypes;

import com.youfuns.exceptions.IllegalFieldException;

import java.util.regex.Pattern;

public record EmailAddress(String address) implements ValidatedValue {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"
    );

    public EmailAddress {
        validate(address);
    }

    @Override
    public Pattern validationPattern() { return EMAIL_PATTERN; }

    @Override
    public ParamType paramType() { return ParamType.EMAIL; }
}