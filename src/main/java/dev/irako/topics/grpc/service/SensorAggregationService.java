package dev.irako.topics.grpc.service;

import dev.irako.topics.grpc.model.SensorAggregateDto;
import dev.irako.topics.grpc.model.SensorReadingDto;

import java.util.List;
import java.util.stream.DoubleStream;

/**
 * Service for aggregating sensor readings.
 * Pure functions with no side-effects or I/O.
 */
public final class SensorAggregationService {

    private SensorAggregationService() {
        // Utility class
    }

    /**
     * Aggregates a list of sensor readings into statistics.
     * 
     * @param readings the sensor readings to aggregate
     * @param processingStartTimeMs the start time of processing in milliseconds
     * @param processingEndTimeMs the end time of processing in milliseconds
     * @return aggregated statistics
     */
    public static SensorAggregateDto aggregateReadings(
            List<SensorReadingDto> readings,
            long processingStartTimeMs,
            long processingEndTimeMs
    ) {
        if (readings == null || readings.isEmpty()) {
            throw new IllegalArgumentException("readings cannot be null or empty");
        }

        int totalReadings = readings.size();
        
        // Extract values for aggregation
        DoubleStream values = readings.stream()
                .mapToDouble(SensorReadingDto::value);
        
        double averageValue = values.average()
                .orElseThrow(() -> new IllegalStateException("Cannot compute average of empty stream"));
        
        // Recompute min/max (stream was consumed)
        double minValue = readings.stream()
                .mapToDouble(SensorReadingDto::value)
                .min()
                .orElseThrow();
        
        double maxValue = readings.stream()
                .mapToDouble(SensorReadingDto::value)
                .max()
                .orElseThrow();
        
        // All readings should have the same unit
        String unit = readings.get(0).unit();
        
        long processingTimeMs = processingEndTimeMs - processingStartTimeMs;
        
        return new SensorAggregateDto(
                totalReadings,
                averageValue,
                minValue,
                maxValue,
                unit,
                processingTimeMs
        );
    }
}
