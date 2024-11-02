package com.ksonni.obrc.main;

import com.ksonni.obrc.concurrent.ConcurrentSummaryBuilder;
import com.ksonni.obrc.models.SummaryBuilder;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final var startTime = System.currentTimeMillis();
        final var path = System.getProperty("user.home") + "/Documents/software/go/1brc/data/measurements.txt";
        SummaryBuilder builder = new ConcurrentSummaryBuilder();
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
}