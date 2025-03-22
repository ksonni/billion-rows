package com.ksonni.obrc.main;

import com.ksonni.obrc.concurrent.ForkJoinSummaryBuilder;
import com.ksonni.obrc.concurrent.PlatformThreadsSummaryBuilder;
import com.ksonni.obrc.concurrent.VirtualThreadsSummaryBuilder;
import com.ksonni.obrc.models.SummaryBuilder;
import com.ksonni.obrc.serial.SerialSummaryBuilder;

import java.io.IOException;

public class Main {
    enum BuilderType {
        SERIAL,
        FORK_JOIN,
        PLATFORM_THREADS,
        VIRTUAL_THREADS,
    }

    private static final BuilderType builderType = BuilderType.VIRTUAL_THREADS;

    public static void main(String[] args) {
        final var startTime = System.currentTimeMillis();

        if (args.length < 1) {
            System.out.println("Must specify file path as an argument");
            System.exit(1);
        }

        final var path = args[0];
        SummaryBuilder builder = getBuilder();
        try {
            System.out.println(builder.buildSummary(path));
        } catch (IOException e) {
            System.out.printf("An error occurred while reading the file: %s\n", e.getLocalizedMessage());
            System.exit(1);
        } catch (SummaryBuilder.ParsingException e) {
            System.out.printf("File format is invalid: %s\n", e.getLocalizedMessage());
            System.exit(1);
        }
        System.out.printf("Completed in: %.2f seconds\n", (System.currentTimeMillis()-startTime)/1000f);
    }

    private static SummaryBuilder getBuilder() {
        return switch (builderType) {
            case SERIAL -> new SerialSummaryBuilder();
            case FORK_JOIN -> new ForkJoinSummaryBuilder();
            case PLATFORM_THREADS -> new PlatformThreadsSummaryBuilder();
            case VIRTUAL_THREADS -> new VirtualThreadsSummaryBuilder();
        };
    }
}