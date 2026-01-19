package dev.irako.topics.grpc.data;

import dev.irako.topics.grpc.model.*;
import dev.irako.topics.grpc.model.StreamingServiceProto.*;

import java.time.Instant;
import java.util.List;

/**
 * Converter between proto messages and domain DTOs.
 * Isolates gRPC types to the data layer.
 */
public final class ProtoConverter {

    private ProtoConverter() {
        // Utility class
    }

    // Message conversions
    public static MessageDto toMessageDto(MessageRequest request) {
        return new MessageDto(
                request.getUserId(),
                request.getContent(),
                Instant.ofEpochMilli(request.getTimestamp())
        );
    }

    public static MessageResponse toProtoMessageResponse(MessageResponseDto dto) {
        return MessageResponse.newBuilder()
                .setSuccess(dto.success())
                .setMessageId(dto.messageId())
                .setStatus(dto.status())
                .build();
    }

    // Notification conversions
    public static NotificationMessage toProtoNotification(NotificationDto dto) {
        return NotificationMessage.newBuilder()
                .setNotificationId(dto.notificationId())
                .setTopic(dto.topic())
                .setTitle(dto.title())
                .setContent(dto.content())
                .setTimestamp(dto.timestamp().toEpochMilli())
                .setPriority(toProtoPriority(dto.priority()))
                .build();
    }

    public static NotificationDto toNotificationDto(NotificationMessage message) {
        return new NotificationDto(
                message.getNotificationId(),
                message.getTopic(),
                message.getTitle(),
                message.getContent(),
                Instant.ofEpochMilli(message.getTimestamp()),
                toNotificationPriority(message.getPriority())
        );
    }

    private static NotificationPriority toNotificationPriority(NotificationPriority priority) {
        if (priority == NotificationPriority.UNRECOGNIZED) {
            throw new IllegalArgumentException("Unrecognized priority value");
        }
        return priority;
    }

    private static NotificationPriority toProtoPriority(NotificationPriority priority) {
        return priority;
    }

    // Sensor reading conversions
    public static SensorReadingDto toSensorReadingDto(SensorReading reading) {
        return new SensorReadingDto(
                reading.getSensorId(),
                reading.getValue(),
                reading.getUnit(),
                Instant.ofEpochMilli(reading.getTimestamp()),
                reading.getLocation()
        );
    }

    public static AggregateResponse toProtoAggregateResponse(SensorAggregateDto dto) {
        return AggregateResponse.newBuilder()
                .setTotalReadings(dto.totalReadings())
                .setAverageValue(dto.averageValue())
                .setMinValue(dto.minValue())
                .setMaxValue(dto.maxValue())
                .setUnit(dto.unit())
                .setProcessingTimeMs(dto.processingTimeMs())
                .build();
    }

    public static SensorAggregateDto toSensorAggregateDto(AggregateResponse response) {
        return new SensorAggregateDto(
                response.getTotalReadings(),
                response.getAverageValue(),
                response.getMinValue(),
                response.getMaxValue(),
                response.getUnit(),
                response.getProcessingTimeMs()
        );
    }

    // Chat message conversions
    public static ChatMessageDto toChatMessageDto(ChatMessage message) {
        return new ChatMessageDto(
                message.getUserId(),
                message.getUsername(),
                message.getContent(),
                Instant.ofEpochMilli(message.getTimestamp()),
                toChatMessageType(message.getType())
        );
    }

    public static ChatMessage toProtoChatMessage(ChatMessageDto dto) {
        return ChatMessage.newBuilder()
                .setUserId(dto.userId())
                .setUsername(dto.username())
                .setContent(dto.content())
                .setTimestamp(dto.timestamp().toEpochMilli())
                .setType(toProtoMessageType(dto.type()))
                .build();
    }

    private static ChatMessageType toChatMessageType(MessageType type) {
        return switch (type) {
            case TEXT -> ChatMessageType.TEXT;
            case JOIN -> ChatMessageType.JOIN;
            case LEAVE -> ChatMessageType.LEAVE;
            case SYSTEM -> ChatMessageType.SYSTEM;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unrecognized message type value");
        };
    }

    private static MessageType toProtoMessageType(ChatMessageType type) {
        return switch (type) {
            case TEXT -> MessageType.TEXT;
            case JOIN -> MessageType.JOIN;
            case LEAVE -> MessageType.LEAVE;
            case SYSTEM -> MessageType.SYSTEM;
        };
    }
}
