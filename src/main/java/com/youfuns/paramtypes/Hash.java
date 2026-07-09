package com.youfuns.paramtypes;

import java.util.regex.Pattern;

public record Hash(String hash) implements ValidatedValue {
    private static final Pattern HASH_PATTERN = Pattern.compile(
            "^(\\$argon2id\\$v=19\\$m=\\d+,t=\\d+,p=\\d+\\$[A-Za-z0-9+/=]+\\$[A-Za-z0-9+/=]+)$|(^[A-Za-z0-9+/]{43}=$)"
    );

    public Hash {
        validate(hash);
    }

    @Override
    public Pattern validationPattern() { return HASH_PATTERN; }

    @Override
    public ParamType paramType() { return ParamType.HASH; }
}