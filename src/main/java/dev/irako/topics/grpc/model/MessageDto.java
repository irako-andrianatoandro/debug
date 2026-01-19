package dev.irako.topics.grpc.model;

import java.time.Instant;

/**
 * Domain DTO for message requests and responses.
 * Immutable record to represent message data in the service layer.
 */
public record MessageDto(
        String userId,
        String content,
        Instant timestamp
) {
    public MessageDto {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content cannot be null or blank");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp cannot be null");
        }
    }
}
