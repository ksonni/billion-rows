package com.ksonni.obrc.models;

import java.io.IOException;

public interface SummaryBuilder {
    Summary buildSummary(String filePath) throws IOException, ParsingException;

    class ParsingException extends Exception {
        public ParsingException(String s) {
            super(s);
        }
    }
}

