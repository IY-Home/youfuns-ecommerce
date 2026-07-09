package com.youfuns.utils;

import java.util.function.Supplier;

public class NullSafe<T> {
    private T value;
    private T defaultValue;
    private Supplier<T> defaultGet;
    private boolean useGet = false;

    public NullSafe(T value, T defaultValue) {
        this.value = value;
        this.defaultValue = defaultValue;
    }
    public NullSafe(T value, Supplier<T> defaultValue) {
        this.value = value;
        this.defaultGet = defaultValue;
        this.useGet = true;
    }
    public T get() {
        if (value == null) {
            return useGet ? defaultGet.get() : defaultValue;
        }
        return value;
    }
    public void set(T value) {
        this.value = value;
    }
    public T getDefault() {
        return useGet ? defaultGet.get() : defaultValue;
    }
    public void setDefault(T defaultValue) {
        this.defaultValue = defaultValue;
        this.useGet = false;
    }
    public void setDefault(Supplier<T> value) {
        this.defaultGet = value;
        this.useGet = true;
    }

    public <X extends Throwable> T getOrThrow(X t) throws X {
        if (this.value == null) throw t;
        return this.value;
    }

    // Utility functions
    public static boolean isNull(Object obj) {
        return (obj == null);
    }

    public static boolean isNotNull(Object obj) {
        return (obj != null);
    }

    public static <X> X safeGet(X val, X defVal) {
        return (val == null ? defVal : val);
    }

    public static <X, Y extends Throwable> X getOrThrow(X val, Y throwable) throws Y {
        if (val == null) throw throwable;
        return val;
    }
}