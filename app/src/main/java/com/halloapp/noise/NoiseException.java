package com.halloapp.noise;

public class NoiseException extends Exception {
    public NoiseException(String message) {
        super(message);
    }

    public NoiseException(Throwable e) {
        super(e);
    }
}
