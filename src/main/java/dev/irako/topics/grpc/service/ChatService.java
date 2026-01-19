package dev.irako.topics.grpc.service;

import dev.irako.topics.grpc.model.ChatMessageDto;
import dev.irako.topics.grpc.model.ChatMessageType;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service for processing chat messages.
 * Pure functions with no side-effects or I/O.
 */
public final class ChatService {

    private ChatService() {
        // Utility class
    }

    /**
     * Processes an incoming chat message and generates a response.
     * In a real chat system, this might broadcast to other users or apply transformations.
     * 
     * @param message the incoming chat message
     * @param allMessages all messages in the conversation so far (for context)
     * @return the processed message (potentially modified or echoed)
     */
    public static ChatMessageDto processChatMessage(
            ChatMessageDto message,
            List<ChatMessageDto> allMessages
    ) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }
        if (allMessages == null) {
            throw new IllegalArgumentException("allMessages cannot be null");
        }

        // Simple echo server: return the message as-is
        // In a real system, you might add timestamps, validate, filter, etc.
        return message;
    }

    /**
     * Generates a system message when a user joins.
     * 
     * @param userId the user ID joining
     * @param username the username joining
     * @return a system JOIN message
     */
    public static ChatMessageDto createJoinMessage(String userId, String username) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or blank");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username cannot be null or blank");
        }

        return new ChatMessageDto(
                userId,
                username,
                username + " joined the chat",
                Instant.now(),
                ChatMessageType.JOIN
        );
    }

    /**
     * Generates a system message when a user leaves.
     * 
     * @param userId the user ID leaving
     * @param username the username leaving
     * @return a system LEAVE message
     */
    public static ChatMessageDto createLeaveMessage(String userId, String username) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or blank");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username cannot be null or blank");
        }

        return new ChatMessageDto(
                userId,
                username,
                username + " left the chat",
                Instant.now(),
                ChatMessageType.LEAVE
        );
    }

    /**
     * Filters messages by type.
     * 
     * @param messages the messages to filter
     * @param type the type to filter by
     * @return stream of messages matching the type
     */
    public static Stream<ChatMessageDto> filterByType(
            List<ChatMessageDto> messages,
            ChatMessageType type
    ) {
        if (messages == null) {
            throw new IllegalArgumentException("messages cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }

        return messages.stream()
                .filter(msg -> msg.type() == type);
    }
}
