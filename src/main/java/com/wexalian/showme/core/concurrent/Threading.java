package com.wexalian.showme.core.concurrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Threading {
    public static final boolean THREADING_DEBUGGING = true;
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final ExecutorService CORE_TASK_THREADS = Executors.newCachedThreadPool(new ShowMeThreadFactory("Core Thread"));
    private static final ExecutorService GUI_TASK_THREADS = Executors.newCachedThreadPool(new ShowMeThreadFactory("GUI Thread"));
    private static final ExecutorService DOWNLOAD_THREADS = Executors.newCachedThreadPool(new ShowMeThreadFactory("Download Thread"));
    private static final ExecutorService SHOW_THREADS = Executors.newCachedThreadPool(new ShowMeThreadFactory("Show Thread"));
    
    public static void shutdown() {
        CORE_TASK_THREADS.shutdown();
        GUI_TASK_THREADS.shutdown();
        DOWNLOAD_THREADS.shutdown();
        SHOW_THREADS.shutdown();
    }
    
    public static void core(String name, Runnable task) {
        CORE_TASK_THREADS.submit(() -> {
            try {
                if (THREADING_DEBUGGING) {
                    LOGGER.debug("Running core task: {}", name);
                }
                task.run();
            }
            catch (Exception e) {
                LOGGER.debug("Task caused exception", e);
            }
        });
    }
    
    public static void gui(String name, Runnable task) {
        GUI_TASK_THREADS.submit(() -> {
            try {
                if (THREADING_DEBUGGING) {
                    LOGGER.debug("Running gui task: {}", name);
                }
                task.run();
            }
            catch (Exception e) {
                LOGGER.debug("Task caused exception", e);
            }
        });
    }
    
    public static Runnable runOnDownload(String name, Runnable task) {
        if (DOWNLOAD_THREADS.isShutdown()) {
            return task;
        }
        return () -> download(name, task);
    }
    
    public static void download(String name, Runnable task) {
        DOWNLOAD_THREADS.submit(() -> {
            try {
                if (THREADING_DEBUGGING) {
                    LOGGER.debug("Running download task: {}", name);
                }
                task.run();
            }
            catch (Exception e) {
                LOGGER.debug("Task caused exception", e);
            }
        });
    }
    
    public static void show(String name, Runnable task) {
        SHOW_THREADS.submit(() -> {
            try {
                if (THREADING_DEBUGGING) {
                    LOGGER.debug("Running show task: {}", name);
                }
                task.run();
            }
            catch (Exception e) {
                LOGGER.debug("Task caused exception", e);
            }
        });
    }
}
