package com.wexalian.showme.util.throwing;

@FunctionalInterface
public interface ThrowingFunction<T, R> {
    R applyUnchecked(T t) throws Exception;
}
