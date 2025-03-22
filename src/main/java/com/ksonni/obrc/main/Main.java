package com.ksonni.obrc.main;

import com.ksonni.obrc.concurrent.ForkJoinSummaryBuilder;
import com.ksonni.obrc.concurrent.PlatformThreadsSummaryBuilder;
import com.ksonni.obrc.concurrent.VirtualThreadsSummaryBuilder;
import com.ksonni.obrc.models.SummaryBuilder;
import com.ksonni.obrc.serial.SerialSummaryBuilder;

import java.io.IOException;

public class Main {
    public static void main(String[] rawArgs) {
        final var startTime = System.currentTimeMillis();

        final LaunchArguments args;
        try {
            args = LaunchArguments.from(rawArgs);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getLocalizedMessage());
            System.exit(1);
            return;
        }

        final var builder = switch (args.type()) {
            case SERIAL -> new SerialSummaryBuilder();
            case FORK_JOIN -> new ForkJoinSummaryBuilder();
            case PLATFORM_THREADS -> new PlatformThreadsSummaryBuilder();
            case VIRTUAL_THREADS -> new VirtualThreadsSummaryBuilder();
        };

        try {
            System.out.println(builder.buildSummary(args.path()));
        } catch (IOException e) {
            System.out.printf("An error occurred while reading the file: %s\n", e.getLocalizedMessage());
            System.exit(1);
        } catch (SummaryBuilder.ParsingException e) {
            System.out.printf("File format is invalid: %s\n", e.getLocalizedMessage());
            System.exit(1);
        }
        System.out.printf("Completed in: %.2f seconds\n", (System.currentTimeMillis()-startTime)/1000f);
    }
}