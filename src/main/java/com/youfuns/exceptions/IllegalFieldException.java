package com.youfuns.exceptions;

import com.youfuns.paramtypes.ParamType;

public class IllegalFieldException extends IllegalArgumentException {
    private final String receivedValue;
    private final ParamType paramType;

    public IllegalFieldException(String receivedValue, String message, ParamType type) {
        super(message);
        this.receivedValue = (receivedValue == null ? "null" : receivedValue);
        this.paramType = type;
    }

    public IllegalFieldException(String message, ParamType type) {
        super(message);
        this.receivedValue = null;
        this.paramType = type;
    }

    public String getMessageFull() {
        return super.getMessage() + " (Received value: " + receivedValue + ", Parameter type: " + paramType + ")";
    }

    public String getReceivedValue() {
        return receivedValue;
    }

    public ParamType getParamType() {
        return paramType;
    }
}
