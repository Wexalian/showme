package com.wexalian.showme.util;

import java.util.function.Supplier;

public class LazyObject<T> {
    private T value;
    private final Supplier<T> supplier;
    
    public LazyObject(Supplier<T> supplier) {
        this.supplier = supplier;
    }
    
    public T get() {
        if (value == null) {
            value = supplier.get();
        }
        return value;
    }
    
    public boolean isEmpty() {
        return value == null;
    }
}
