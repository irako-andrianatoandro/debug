package dev.irako.topics.grpc.model;

/**
 * Domain DTO for message response.
 * Immutable record representing the result of sending a message.
 */
public record MessageResponseDto(
        boolean success,
        String messageId,
        String status
) {
    public MessageResponseDto {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId cannot be null or blank");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status cannot be null or blank");
        }
    }
}
