package com.youfuns.paramtypes;

import java.util.UUID;

public class UuidFormat {
    public static String shortenUUID(UUID uuid) {
        return uuid.toString().substring(0, 8) + "...";
    }
    public static String shortenUUID(UUID uuid, int characters, String postfix) {
        return uuid.toString().substring(0, characters) + postfix;
    }
}
