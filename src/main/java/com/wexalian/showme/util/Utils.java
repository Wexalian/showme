package com.wexalian.showme.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Utils {
    public static <T, R> R makeIfAndMapOrElse(T value, Function<T, Boolean> condition, Function<T, R> mapper, Supplier<R> supplier) {
        if (condition.apply(value)) {
            return mapper.apply(value);
        }
        return supplier.get();
    }
    
    public static <T> T make(Supplier<T> supplier, Consumer<T> consumer) {
        T instance = supplier.get();
        consumer.accept(instance);
        return instance;
    }
    
    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }
    
    public static <T> T makeTernary(boolean condition, Supplier<T> trueSupplier, Supplier<T> falseSupplier) {
        return condition ? trueSupplier.get() : falseSupplier.get();
    }
    
}
