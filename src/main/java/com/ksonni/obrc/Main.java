package com.ksonni.obrc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Main.processFile(System.getProperty("user.home") + "/Documents/software/go/1brc/data/measurements.txt");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void processFile(String path) throws IOException {
        var reader = new BufferedReader(new FileReader(path));
        String line;
        int count = 0;
        double total = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        var minCity = "";
        var maxCity = "";
        while ((line = reader.readLine()) != null) {
            count++;
            var parts = line.split(";");
            if (parts.length < 2) {
                System.out.println("Failed to parse line: " + line);
                continue;
            }
            var city = parts[0];
            var temperature = 0.0f;
            try {
                temperature = Float.parseFloat(parts[1]);
            } catch (NumberFormatException e) {
                System.out.println("Failed to parse temperature: " + parts[1]);
                continue;
            }
            total += temperature;
            if (temperature <= min) {
                min = temperature;
                minCity = city;
            } else if (temperature >= max) {
                max = temperature;
                maxCity = city;
            }
            if (count % 5_000_000 == 0) {
                System.out.println("Processed " + count + " lines.");
            }
        }
        double mean = count == 0 ? 0 : total / count;
        System.out.printf("Summary: min: %s %.3f max: %s %.3f mean: %.3f\n", minCity, min, maxCity, max, mean);
    }
}