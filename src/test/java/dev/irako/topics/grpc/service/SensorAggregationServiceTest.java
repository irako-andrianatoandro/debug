package dev.irako.topics.grpc.service;

import dev.irako.topics.grpc.model.SensorAggregateDto;
import dev.irako.topics.grpc.model.SensorReadingDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SensorAggregationServiceTest {

    @Test
    void aggregateReadings_validReadings_computesCorrectStatistics() {
        List<SensorReadingDto> readings = List.of(
                new SensorReadingDto("sensor1", 20.0, "celsius", Instant.now(), "room1"),
                new SensorReadingDto("sensor2", 25.0, "celsius", Instant.now(), "room2"),
                new SensorReadingDto("sensor3", 30.0, "celsius", Instant.now(), "room3")
        );
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        
        SensorAggregateDto aggregate = SensorAggregationService.aggregateReadings(readings, startTime, endTime);
        
        assertEquals(3, aggregate.totalReadings());
        assertEquals(25.0, aggregate.averageValue(), 0.001);
        assertEquals(20.0, aggregate.minValue(), 0.001);
        assertEquals(30.0, aggregate.maxValue(), 0.001);
        assertEquals("celsius", aggregate.unit());
        assertTrue(aggregate.processingTimeMs() >= 0);
    }

    @Test
    void aggregateReadings_singleReading_computesCorrectStatistics() {
        List<SensorReadingDto> readings = List.of(
                new SensorReadingDto("sensor1", 25.5, "celsius", Instant.now(), "room1")
        );
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        
        SensorAggregateDto aggregate = SensorAggregationService.aggregateReadings(readings, startTime, endTime);
        
        assertEquals(1, aggregate.totalReadings());
        assertEquals(25.5, aggregate.averageValue(), 0.001);
        assertEquals(25.5, aggregate.minValue(), 0.001);
        assertEquals(25.5, aggregate.maxValue(), 0.001);
    }

    @Test
    void aggregateReadings_emptyList_throwsException() {
        long now = System.currentTimeMillis();
        assertThrows(IllegalArgumentException.class, () -> {
            SensorAggregationService.aggregateReadings(List.of(), now, now);
        });
    }

    @Test
    void aggregateReadings_nullList_throwsException() {
        long now = System.currentTimeMillis();
        assertThrows(IllegalArgumentException.class, () -> {
            SensorAggregationService.aggregateReadings(null, now, now);
        });
    }
}
