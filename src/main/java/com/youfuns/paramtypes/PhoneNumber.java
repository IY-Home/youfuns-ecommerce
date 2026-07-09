package com.youfuns.paramtypes;

import com.youfuns.exceptions.IllegalFieldException;
import java.util.regex.Pattern;

public record PhoneNumber(String phone) implements ValidatedValue {
    // International format: +[country code][number] or just digits with optional separators
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+?\\d{1,3}[-. ]?)?(\\(?\\d{1,4}\\)?[-. ]?)?[\\d\\-. ]{5,15}$"
    );

    public PhoneNumber {
        validate(phone);
    }

    @Override
    public Pattern validationPattern() { return PHONE_PATTERN; }

    @Override
    public ParamType paramType() { return ParamType.PHONE; }
}