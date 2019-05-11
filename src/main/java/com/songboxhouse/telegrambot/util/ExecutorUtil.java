package com.songboxhouse.telegrambot.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorUtil {
    private static final int MAX_POOL_SIZE = 16;

    public static ExecutorService createExecutorService(int countThreads) {
        final int poolSize = (countThreads < 1) ? 1 : ((countThreads > MAX_POOL_SIZE) ? MAX_POOL_SIZE : countThreads);
        return Executors.newFixedThreadPool(poolSize);
    }
}
