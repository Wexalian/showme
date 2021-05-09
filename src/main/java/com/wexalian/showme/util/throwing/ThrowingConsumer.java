package com.wexalian.showme.util.throwing;

@FunctionalInterface
public interface ThrowingConsumer<T>{
    void acceptUnchecked(T t) throws Exception;
}
