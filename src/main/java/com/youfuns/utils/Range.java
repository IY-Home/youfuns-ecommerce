package com.youfuns.utils;

import java.util.List;
import java.util.stream.IntStream;

public class Range {
    public final int min;
    public final int max;
    public final boolean inclusive;
    public Range (int min, int max, boolean inclusive) {
        this.min = min;
        this.max = max;
        this.inclusive = inclusive;
    }
    public Range(int min, int max) {
        this(min, max, true);
    }
    public Range(int max) {
        this(0, max, true);
    }
    public boolean inRange(int val) {
        if (inclusive) {
            return val >= min && val <= max;
        }
        return val > min && val < max;
    }

    public static List<Integer> range(int min, int max, int step) {
        if (step == 0) {
            throw new IllegalArgumentException("Step cannot be zero");
        }

        // Dynamically choose the stop condition based on step direction
        return IntStream.iterate(min,
                        i -> step > 0 ? i < max : i > max,
                        i -> i + step)
                .boxed()
                .toList();
    }
    public static List<Integer> range(int min, int max) {
        return range(min, max, 1);
    }
    public static List<Integer> range(int max) {
        return range(0, max, 1);
    }
}
