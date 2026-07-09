package com.youfuns.paramtypes;

import com.youfuns.exceptions.IllegalFieldException;

import java.util.regex.Pattern;

public record PreferredContactMethod(String method) implements ValidatedValue {
    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "^(email|sms|push|none)$"
    );

    public PreferredContactMethod {
        validate(method);
    }

    @Override
    public Pattern validationPattern() { return METHOD_PATTERN; }

    @Override
    public ParamType paramType() { return ParamType.CONTACT_METHOD; }
}