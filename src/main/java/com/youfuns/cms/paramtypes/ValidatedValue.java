package com.youfuns.cms.paramtypes;

import java.util.regex.Pattern;

public interface ValidatedValue {
    Pattern validationPattern();
    ParamType paramType();

    // Default validation logic
    default void validate(String val) {
        if (val == null || !validationPattern().matcher(val).matches()) {
            throw new IllegalFieldException(val, "Invalid " + paramType().name() + " format", paramType());
        }
    }
}