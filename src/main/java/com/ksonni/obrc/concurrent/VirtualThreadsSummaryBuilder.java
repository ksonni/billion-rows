package com.ksonni.obrc.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class VirtualThreadsSummaryBuilder extends ThreadPoolSummaryBuilder {
    @Override
    ExecutorService getExecutorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
