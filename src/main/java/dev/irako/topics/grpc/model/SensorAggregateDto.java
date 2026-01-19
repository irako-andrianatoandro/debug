package dev.irako.topics.grpc.model;

/**
 * Domain DTO for aggregated sensor data.
 * Immutable record representing aggregated statistics from multiple sensor readings.
 */
public record SensorAggregateDto(
        int totalReadings,
        double averageValue,
        double minValue,
        double maxValue,
        String unit,
        long processingTimeMs
) {
    public SensorAggregateDto {
        if (totalReadings < 0) {
            throw new IllegalArgumentException("totalReadings cannot be negative");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("unit cannot be null or blank");
        }
        if (processingTimeMs < 0) {
            throw new IllegalArgumentException("processingTimeMs cannot be negative");
        }
    }
}
