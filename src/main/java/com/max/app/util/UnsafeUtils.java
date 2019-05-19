package com.max.app.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public final class UnsafeUtils {

    private UnsafeUtils(){}

    @SuppressWarnings("restriction")
    public static Unsafe getUnsafe() {
        try {
            Field instanceField = Unsafe.class.getDeclaredField("theUnsafe");
            instanceField.setAccessible(true);
            return (Unsafe) instanceField.get(null);
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
