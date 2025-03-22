package com.ksonni.obrc.concurrent;

import com.ksonni.obrc.models.City;
import com.ksonni.obrc.models.PartitionedFile;
import com.ksonni.obrc.models.Summary;
import com.ksonni.obrc.models.SummaryBuilder;
import com.ksonni.obrc.serial.SerialSummaryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests to ensure results from concurrent implementations match the simple serial implementation.
 */
public class ConcurrentSummaryTests {
    private static final int PARTITION_SIZE = 128 * 1024;
    private static final Double TOLERANCE = 0.000001;

    private static Summary serialSummary;

    @BeforeAll
    static void buildSerialSummary() throws SummaryBuilder.ParsingException, IOException {
        serialSummary = new SerialSummaryBuilder().buildSummary(getDataFilePath());
    }

    @Test
    void testForkJoinSummaryBuilder() throws SummaryBuilder.ParsingException, IOException {
        var file = getPartitionedFile();
        var summary = new ForkJoinSummaryBuilder().buildSummary(file);
        assertSummaryEquals(serialSummary, summary);
    }

    @Test
    void testPlatformThreadSummaryBuilder() throws SummaryBuilder.ParsingException, IOException {
        var file = getPartitionedFile();
        var summary = new PlatformThreadsSummaryBuilder().buildSummary(file);
        assertSummaryEquals(serialSummary, summary);
    }

    @Test
    void testVirtualThreadSummaryBuilder() throws SummaryBuilder.ParsingException, IOException {
        var file = getPartitionedFile();
        var summary = new VirtualThreadsSummaryBuilder().buildSummary(file);
        assertSummaryEquals(serialSummary, summary);
    }

    private static void assertSummaryEquals(Summary expected, Summary actual) {
        assertEquals(expected.entries(), actual.entries());
        assertEquals(expected.total(), actual.total(), TOLERANCE);
        assertEquals(expected.mean(), actual.mean(), TOLERANCE);
        if (expected.minCity().isPresent()) {
            assertTrue(actual.minCity().isPresent());
            assertCityEquals(expected.minCity().get(), actual.minCity().get());
        }
        if (expected.maxCity().isPresent()) {
            assertTrue(actual.maxCity().isPresent());
            assertCityEquals(expected.maxCity().get(), actual.maxCity().get());
        }
    }

    private static void assertCityEquals(City expected, City actual) {
        assertEquals(expected.name(), actual.name());
        assertEquals(expected.temperature(), actual.temperature(), TOLERANCE);
    }

    private static PartitionedFile getPartitionedFile() {
        var file = new File(getDataFilePath());
        return new PartitionedFile(file, PARTITION_SIZE);
    }

    private static String getDataFilePath() {
        var resource = ConcurrentSummaryTests.class.getClassLoader()
            .getResource("measurements.txt");
        assert resource != null;
        return resource.getFile();
    }
}
