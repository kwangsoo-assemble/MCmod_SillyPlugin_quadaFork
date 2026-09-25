package dev.celestial.silly.helper;

import org.jetbrains.annotations.Nullable;

public class Overridable<T> {
    private Boolean overridden = false;
    private T value;
    public Boolean isOverridden() {
        return overridden;
    }

    @Nullable
    public T getValue() {
        return value;
    }

    public void setValue(@Nullable T v) {
        overridden = v != null;
        value = v;
    }
}
