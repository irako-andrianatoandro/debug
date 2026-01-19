package dev.irako.topics.grpc.model;

import java.time.Instant;

/**
 * Domain DTO for notification messages.
 * Immutable record representing a notification in the service layer.
 */
public record NotificationDto(
        String notificationId,
        String topic,
        String title,
        String content,
        Instant timestamp,
        NotificationPriority priority
) {
    public NotificationDto {
        if (notificationId == null || notificationId.isBlank()) {
            throw new IllegalArgumentException("notificationId cannot be null or blank");
        }
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("topic cannot be null or blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title cannot be null or blank");
        }
        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp cannot be null");
        }
        if (priority == null) {
            throw new IllegalArgumentException("priority cannot be null");
        }
    }
}
