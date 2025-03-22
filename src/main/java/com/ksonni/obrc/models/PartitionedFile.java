package com.ksonni.obrc.models;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class PartitionedFile {
    private static final int PARTITION_SIZE = 32 * 1024 * 1024;

    private final File file;

    public PartitionedFile(final File file) {
        this.file = file;
    }

    public PartitionedFile(String path) {
        this(new File(path));
    }

    public record Partition(int id, long start, int length) {}

    public synchronized List<Partition> buildPartitions() throws IOException {
        final var size = file.length();

        System.out.printf("Partitioning file of size: %d\n", size);
        System.out.printf("Target size per partition: %s\n", FileUtils.byteCountToDisplaySize(PARTITION_SIZE));

        List<Partition> partitionList = new ArrayList<>();

        try (var reader = new RandomAccessFile(file, "r")) {
            int id = 0;
            while (reader.getFilePointer() < size) {
                id++;
                final var position = reader.getFilePointer();
                var nextPosition = Math.min(position + PARTITION_SIZE, size - 1);
                reader.seek(nextPosition);
                reader.readLine();
                final var length = (int) (reader.getFilePointer() - position);
                partitionList.add(new Partition(id, position, length));
            }
        }
        return partitionList;
    }

    public synchronized String read(Partition partition) throws IOException {
        try (var reader = new RandomAccessFile(file, "r")) {
            var out = new byte[partition.length];
            reader.seek(partition.start);
            reader.read(out, 0, partition.length);
            return new String(out);
        }
    }
}
