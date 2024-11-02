package com.ksonni.obrc.serial;

import com.ksonni.obrc.models.City;
import com.ksonni.obrc.models.Summary;
import com.ksonni.obrc.models.SummaryBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

public class SerialSummaryBuilder implements SummaryBuilder {
    @Override
    public Summary buildSummary(String filePath) throws IOException, ParsingException {
        var reader = new BufferedReader(new FileReader(filePath));
        return buildContentsSummary(reader);
    }

    public Summary buildContentsSummary(BufferedReader reader) throws IOException, ParsingException {
        int count = 0;
        double total = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        String line = "", minCity = "", maxCity = "";

        while ((line = reader.readLine()) != null) {
            count++;
            var parts = line.split(";");
            if (parts.length < 2) {
                throw new ParsingException("Failed to parse line: " + line);
            }
            var city = parts[0];
            var temperature = 0.0f;
            try {
                temperature = Float.parseFloat(parts[1]);
            } catch (NumberFormatException e) {
                throw new ParsingException("Failed to parse temperature on line: " + line);
            }
            total += temperature;
            if (temperature <= min) {
                min = temperature;
                minCity = city;
            } else if (temperature >= max) {
                max = temperature;
                maxCity = city;
            }
        }
        return new Summary(new City(min, minCity), new City(max, maxCity), count, total);
    }

    public Summary buildContentsSummary(String contents) throws IOException, ParsingException {
        var reader = new BufferedReader(new StringReader(contents));
        return buildContentsSummary(reader);
    }
}
