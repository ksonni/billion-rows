package com.ksonni.obrc.concurrent;

import com.ksonni.obrc.models.PartitionedFile;
import com.ksonni.obrc.models.Summary;
import com.ksonni.obrc.models.SummaryBuilder;
import com.ksonni.obrc.serial.SerialSummaryBuilder;

import java.io.IOException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ForkJoinSummaryBuilder implements SummaryBuilder {
    @Override
    public Summary buildSummary(String filePath) throws IOException, ParsingException {
        final var file = new PartitionedFile(filePath);
        final var partitions = file.buildPartitions();
        final var tasks = partitions.stream().map(p -> new SummaryTask(file, p)).toList();

        System.out.println("Using common fork join pool");
        ForkJoinTask.invokeAll(tasks);

        Summary summary = Summary.empty();
        for (var task : tasks) {
            switch (task.join()) {
                case SummaryTask.Result.IOError ioError -> throw ioError.e();
                case SummaryTask.Result.ParsingError parsingError -> throw parsingError.e();
                case SummaryTask.Result.Success success -> {
                    var result = success.value();
                    summary = summary.merge(result);
                }
            }
        }
        return summary;
    }
}

class SummaryTask extends RecursiveTask<SummaryTask.Result> {
    sealed interface Result {
        record IOError(IOException e) implements Result {}
        record ParsingError(SummaryBuilder.ParsingException e) implements Result {}
        record Success(Summary value) implements Result {}
    }

    private final PartitionedFile.Partition partition;
    private final PartitionedFile file;

    SummaryTask(PartitionedFile file, PartitionedFile.Partition partition) {
        this.file = file;
        this.partition = partition;
    }

    @Override
    protected Result compute() {
        try {
            return new Result.Success(tryCompute());
        } catch (IOException e) {
            return new Result.IOError(e);
        } catch (SummaryBuilder.ParsingException e) {
            return new Result.ParsingError(e);
        }
    }

    private Summary tryCompute() throws IOException, SummaryBuilder.ParsingException {
        String data = file.read(partition);
        System.out.printf("Processing partition #%d\n", partition.id());
        return new SerialSummaryBuilder().buildContentsSummary(data);
    }
}