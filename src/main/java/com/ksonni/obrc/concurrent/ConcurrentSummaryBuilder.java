package com.ksonni.obrc.concurrent;

import com.ksonni.obrc.models.Summary;
import com.ksonni.obrc.models.SummaryBuilder;
import com.ksonni.obrc.serial.SerialSummaryBuilder;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class ConcurrentSummaryBuilder implements SummaryBuilder {
    private static final int PARTITION_SIZE = 32 * 1024 * 1024;

    @Override
    public Summary buildSummary(String filePath) throws IOException, ParsingException {
        final var rawFile = new File(filePath);

        System.out.printf("Partitioning file of size: %d\n", rawFile.length());
        System.out.printf("Target size per partition: %s\n", FileUtils.byteCountToDisplaySize(PARTITION_SIZE));

        final var file = new PartitionedFile(rawFile, PARTITION_SIZE);

        List<PartitionedFile.Partition> partitions = file.buildPartitions();

        Summary summary = null;
        var builder = new SerialSummaryBuilder();
        for (int i = 0; i < partitions.size(); i++) {
            var reader = new BufferedReader(new StringReader(file.read(partitions.get(i))));
            System.out.printf("Partition %d\n", i);
            var partitionSummary = builder.buildSummary(reader);
            summary = summary == null ? partitionSummary : summary.merge(partitionSummary);
        }
        return summary;
    }
}
