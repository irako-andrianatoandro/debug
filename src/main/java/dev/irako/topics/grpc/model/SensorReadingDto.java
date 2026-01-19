package dev.irako.topics.grpc.model;

import java.time.Instant;

/**
 * Domain DTO for sensor readings. Immutable record representing a single sensor
 * measurement.
 */
public record SensorReadingDto(String sensorId, double value, String unit, Instant timestamp, String location) {
	public SensorReadingDto {
		if (sensorId == null || sensorId.isBlank()) {
			throw new IllegalArgumentException("sensorId cannot be null or blank");
		}
		if (unit == null || unit.isBlank()) {
			throw new IllegalArgumentException("unit cannot be null or blank");
		}
		if (location == null || location.isBlank()) {
			throw new IllegalArgumentException("location cannot be null or blank");
		}
		if (timestamp == null) {
			throw new IllegalArgumentException("timestamp cannot be null");
		}
	}
}
