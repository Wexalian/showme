package com.wexalian.showme.util.throwing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Throwing {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static void run(ThrowingRunnable runnable) {
        run(runnable, LOGGER::catching);
    }
    
    public static void run(ThrowingRunnable runnable, Consumer<Exception> exceptionHandler) {
        try {
            runnable.runUnchecked();
        }
        catch (Exception e) {
            exceptionHandler.accept(e);
        }
    }
    
    public static <T> T get(ThrowingSupplier<T> supplier) {
        return get(supplier, LOGGER::catching);
    }
    
    public static <T> T get(ThrowingSupplier<T> supplier, Consumer<Exception> exceptionHandler) {
        try {
            return supplier.getUnchecked();
        }
        catch (Exception e) {
            exceptionHandler.accept(e);
        }
        return null;
    }
    
    public static <T> Consumer<T> consume(ThrowingConsumer<T> consumer) {
        return consume(consumer, (v, exception) -> LOGGER.catching(exception));
    }
    
    public static <T> Consumer<T> consume(ThrowingConsumer<T> consumer, BiConsumer<T, Exception> exceptionHandler) {
        return value -> {
            try {
                consumer.acceptUnchecked(value);
            }
            catch (Exception e) {
                exceptionHandler.accept(value, e);
            }
        };
    }
    
    public static <T, R> Consumer<T> transformAndConsume(Function<T, R> transformer, ThrowingConsumer<R> consumer) {
        return transformAndConsume(transformer, consumer, (v, exception) -> LOGGER.catching(exception));
    }
    
    public static <T, R> Consumer<T> transformAndConsume(Function<T, R> transformer, ThrowingConsumer<R> consumer, BiConsumer<R, Exception> exceptionHandler) {
        return raw -> {
            R value = transformer.apply(raw);
            try {
                consumer.acceptUnchecked(value);
            }
            catch (Exception e) {
                exceptionHandler.accept(value, e);
            }
        };
    }
    
    public static <T> void accept(T value, ThrowingConsumer<T> consumer) {
        accept(value, consumer, (v, exception) -> LOGGER.catching(exception));
    }
    
    public static <T> void accept(T value, ThrowingConsumer<T> consumer, BiConsumer<T, Exception> exceptionHandler) {
        try {
            consumer.acceptUnchecked(value);
        }
        catch (Exception e) {
            exceptionHandler.accept(value, e);
        }
    }
    
    public static <T, R> R apply(T value, ThrowingFunction<T, R> function) {
        return apply(value, function, (v, exception) -> LOGGER.catching(exception));
    }
    
    public static <T, R> R apply(T value, ThrowingFunction<T, R> function, BiConsumer<T, Exception> exceptionHandler) {
        try {
            return function.applyUnchecked(value);
        }
        catch (Exception e) {
            exceptionHandler.accept(value, e);
        }
        return null;
    }
}
