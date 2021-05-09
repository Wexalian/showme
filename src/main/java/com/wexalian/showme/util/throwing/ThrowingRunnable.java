package com.wexalian.showme.util.throwing;

@FunctionalInterface
public interface ThrowingRunnable {
    void runUnchecked() throws Exception;
}