package com.ksonni.obrc.models;

public record Summary(City minCity, City maxCity, int entries, double total) {
    public Summary merge(Summary other) {
        return new Summary(
                this.minCity.temperature() < other.minCity.temperature() ? this.minCity : other.minCity,
                this.maxCity.temperature() < other.maxCity.temperature() ? this.maxCity : other.maxCity,
                this.entries + other.entries,
                this.total + other.total
        );
    }

    public double mean() {
        return this.entries == 0 ? 0 : this.total / this.entries;
    }
}

