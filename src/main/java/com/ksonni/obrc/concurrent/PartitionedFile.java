package com.ksonni.obrc.concurrent;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class PartitionedFile {
    private final File file;
    private final int partitionSize;

    PartitionedFile(final File file, final int partitionSize) {
        this.file = file;
        this.partitionSize = partitionSize;
    }

    record Partition(int id, long start, int length) {
    }

    synchronized List<Partition> buildPartitions() throws IOException {
        final var size = file.length();
        List<Partition> partitionList = new ArrayList<>();

        try (var reader = new RandomAccessFile(file, "r")) {
            int id = 0;
            while (reader.getFilePointer() < size) {
                id++;
                final var position = reader.getFilePointer();
                var nextPosition = Math.min(position + partitionSize, size - 1);
                reader.seek(nextPosition);
                reader.readLine();
                final var length = (int) (reader.getFilePointer() - position);
                partitionList.add(new Partition(id, position, length));
            }
        }
        return partitionList;
    }

    synchronized String read(Partition partition) throws IOException {
        try (var reader = new RandomAccessFile(file, "r")) {
            var out = new byte[partition.length];
            reader.seek(partition.start);
            reader.read(out, 0, partition.length);
            return new String(out);
        }
    }
}
