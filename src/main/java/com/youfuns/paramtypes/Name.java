package com.youfuns.paramtypes;

import com.youfuns.exceptions.IllegalFieldException;
import java.util.regex.Pattern;

public record Name(String name) implements ValidatedValue {
    // Allows letters, spaces, hyphens, apostrophes, and dots
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[A-Za-zÀ-ÖØ-öø-ÿ'\\-. ]{1,100}$"
    );

    public Name {
        validate(name);
    }

    @Override
    public Pattern validationPattern() { return NAME_PATTERN; }

    @Override
    public ParamType paramType() { return ParamType.NAME; }
}