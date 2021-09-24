package com.halloapp.util;

import androidx.annotation.Nullable;

public class Result<T> {

    public static <T> Result<T> ok(T result) {
        return new Result<T>(true, result, null);
    }

    public static <T> Result<T> fail(String error) {
        return new Result<T>(false, null, error);
    }

    private final boolean success;
    private final T result;
    private final String error;

    private Result(boolean success, @Nullable T result, @Nullable String error) {
        this.success = success;
        this.result = result;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getResult() {
        Preconditions.checkState(success, "Trying to get result on failure");
        return result;
    }

    public String getError() {
        Preconditions.checkState(!success, "Trying to get error on success");
        return error;
    }
}
