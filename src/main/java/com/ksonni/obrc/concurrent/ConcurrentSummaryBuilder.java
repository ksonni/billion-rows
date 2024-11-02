package com.ksonni.obrc.concurrent;

import com.ksonni.obrc.models.Summary;
import com.ksonni.obrc.models.SummaryBuilder;
import com.ksonni.obrc.serial.SerialSummaryBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConcurrentSummaryBuilder implements SummaryBuilder {
    private static final int PARTITION_SIZE = 32 * 1024 * 1024;

    @Override
    public Summary buildSummary(String filePath) throws IOException, ParsingException {
        final var rawFile = new File(filePath);
        final var maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors()-1);

        System.out.printf("Using thread count: %d\n", maxThreads);
        System.out.printf("Partitioning file of size: %d\n", rawFile.length());
        System.out.printf("Target size per partition: %s\n", FileUtils.byteCountToDisplaySize(PARTITION_SIZE));

        final var file = new PartitionedFile(rawFile, PARTITION_SIZE);

        List<PartitionedFile.Partition> partitions = file.buildPartitions();
        List<Future<Summary>> futures = new ArrayList<>();

        try(var executor = Executors.newFixedThreadPool(maxThreads)) {
            for (var partition : partitions) {
                var future = executor.submit(() -> {
                    String data = file.read(partition);
                    System.out.printf("Processing partition #%d\n", partition.id());
                    return new SerialSummaryBuilder().buildContentsSummary(data);
                });
                futures.add(future);
            }
        }

        Summary summary = null;
        for (var future : futures) {
            Summary partitionSummary;
            try {
                partitionSummary = future.get();
            } catch (InterruptedException e) {
                continue;
            } catch (ExecutionException e) {
                var ex = e.getCause();
                if (ex instanceof ParsingException) {
                    throw (ParsingException) ex;
                } else if (ex instanceof IOException) {
                    throw (IOException) ex;
                }
                throw new RuntimeException(e); // Should never happen
            }
            summary = summary == null ? partitionSummary : summary.merge(partitionSummary);
        }
        return summary;
    }
}
