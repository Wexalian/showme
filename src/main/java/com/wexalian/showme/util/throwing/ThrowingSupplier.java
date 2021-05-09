package com.wexalian.showme.util.throwing;

@FunctionalInterface
public interface ThrowingSupplier<T>{
    T getUnchecked() throws Exception;
}
