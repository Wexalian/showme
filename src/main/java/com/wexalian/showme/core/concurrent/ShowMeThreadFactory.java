package com.wexalian.showme.core.concurrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

public class ShowMeThreadFactory implements ThreadFactory {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private final String name;
    private final ThreadGroup group;
    
    public ShowMeThreadFactory(String name) {
        this.name = name;
        
        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    }
    
    public Thread newThread(@Nonnull Runnable runnable) {
        Thread newThread = new Thread(group, runnable, name, 0);
        newThread.setDaemon(false);
        newThread.setUncaughtExceptionHandler((t, e) -> LOGGER.debug("Uncaught exception from thread '{}'", t.getName(), e));
        return newThread;
    }
}
