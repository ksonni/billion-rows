package com.ksonni.obrc.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PlatformThreadsSummaryBuilder extends ThreadPoolSummaryBuilder {
    @Override
    ExecutorService getExecutorService() {
        final var maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors()-1);
        System.out.printf("Using thread count: %d\n", maxThreads);
        return Executors.newFixedThreadPool(maxThreads);
    }
}
