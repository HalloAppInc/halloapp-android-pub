package com.halloapp.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Preconditions {

    public static @NonNull <T> T checkNotNull(@Nullable T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkState(final boolean expression) {
        checkState(expression, null);
    }

    public static void checkState(boolean expression, @Nullable String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }
}
