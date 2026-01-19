package dev.irako.topics.grpc.model;

import java.time.Instant;

/**
 * Domain DTO for chat messages. Immutable record representing a chat message in
 * the service layer.
 */
public record ChatMessageDto(String userId, String username, String content, Instant timestamp, ChatMessageType type) {
	public ChatMessageDto {
		if (userId == null || userId.isBlank()) {
			throw new IllegalArgumentException("userId cannot be null or blank");
		}
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("username cannot be null or blank");
		}
		if (content == null) {
			throw new IllegalArgumentException("content cannot be null");
		}
		if (timestamp == null) {
			throw new IllegalArgumentException("timestamp cannot be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}
	}
}
