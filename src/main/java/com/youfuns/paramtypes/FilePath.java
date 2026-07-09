package com.youfuns.paramtypes;

import com.youfuns.exceptions.IllegalFieldException;

import java.util.regex.Pattern;

public record FilePath(String path) implements ValidatedValue {
    // Standard RFC 5322 Email Validation Pattern
    private static final Pattern PATH_PATTERN = Pattern.compile(
            "^(/[^/\\\\x00-\\\\x1F\\\\x7F]+)+/?$|^[^/\\\\x00-\\\\x1F\\\\x7F]+(/[^/\\\\x00-\\\\x1F\\\\x7F]+)*/?$"
    );

    public FilePath {
        validate(path);
    }

    @Override
    public Pattern validationPattern() { return PATH_PATTERN; }

    @Override
    public ParamType paramType() { return ParamType.FILEPATH; }
}