package com.ksonni.obrc.models;

import java.util.Optional;

public record Summary(Optional<City> minCity, Optional<City> maxCity, int entries, double total) {
    public Summary merge(Summary other) {
        Optional<City> min = Optional.empty();
        if (minCity.isPresent() && other.minCity.isPresent()) {
            min = minCity.get().temperature() < other.minCity.get().temperature() ? minCity : other.minCity;
        } else if (minCity.isPresent()) {
            min = minCity;
        } else if (other.minCity.isPresent()) {
            min = other.minCity;
        }

        Optional<City> max = Optional.empty();
        if (maxCity.isPresent() && other.maxCity.isPresent()) {
            max = maxCity.get().temperature() > other.maxCity.get().temperature() ? maxCity : other.maxCity;
        } else if (maxCity.isPresent()) {
            max = maxCity;
        } else if (other.maxCity.isPresent()) {
            max = other.maxCity;
        }

        return new Summary(min, max, this.entries + other.entries, this.total + other.total);
    }

    public static Summary empty() {
        return new Summary(Optional.empty(), Optional.empty(), 0, 0);
    }

    public double mean() {
        return this.entries == 0 ? 0 : this.total / this.entries;
    }
}

