package dev.irako.topics.grpc.api;

import dev.irako.topics.grpc.data.GrpcClient;
import dev.irako.topics.grpc.model.*;

import java.time.Instant;
import java.util.List;

/**
 * Example client application demonstrating all gRPC streaming patterns.
 * 
 * Usage:
 * <pre>
 * java GrpcClientApp [host] [port]
 * </pre>
 * Default: localhost:50051
 */
public final class GrpcClientApp {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GrpcClientApp.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 50051;

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                logger.error("Invalid port number: {}", args[1]);
                System.exit(1);
            }
        }

        try (GrpcClient client = new GrpcClient(host, port)) {
            logger.info("Connected to gRPC server at {}:{}", host, port);

            // Demonstrate unary RPC
            demonstrateUnaryRpc(client);

            // Demonstrate server streaming
            demonstrateServerStreaming(client);

            // Demonstrate client streaming
            demonstrateClientStreaming(client);

            // Demonstrate bidirectional streaming
            demonstrateBidirectionalStreaming(client);

            logger.info("All demonstrations completed");
        } catch (Exception e) {
            logger.error("Error running client", e);
            System.exit(1);
        }
    }

    private static void demonstrateUnaryRpc(GrpcClient client) {
        logger.info("\n=== Unary RPC: Send Message ===");
        try {
            MessageDto message = new MessageDto(
                    "user123",
                    "Hello from gRPC client!",
                    Instant.now()
            );
            MessageResponseDto response = client.sendMessage(message);
            logger.info("Message sent successfully: messageId={}, status={}", 
                    response.messageId(), response.status());
        } catch (Exception e) {
            logger.error("Error in unary RPC", e);
        }
    }

    private static void demonstrateServerStreaming(GrpcClient client) {
        logger.info("\n=== Server Streaming: Subscribe to Notifications ===");
        try {
            List<String> topics = List.of("news", "updates", "alerts");
            List<NotificationDto> notifications = client.subscribeToNotifications("user123", topics);
            logger.info("Received {} notifications", notifications.size());
            notifications.forEach(n -> logger.info("  - [{}] {}: {}", 
                    n.priority(), n.topic(), n.title()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while receiving notifications", e);
        } catch (Exception e) {
            logger.error("Error in server streaming", e);
        }
    }

    private static void demonstrateClientStreaming(GrpcClient client) {
        logger.info("\n=== Client Streaming: Upload Sensor Data ===");
        try {
            List<SensorReadingDto> readings = List.of(
                    new SensorReadingDto("sensor1", 25.5, "celsius", Instant.now(), "room1"),
                    new SensorReadingDto("sensor2", 26.0, "celsius", Instant.now(), "room2"),
                    new SensorReadingDto("sensor3", 24.8, "celsius", Instant.now(), "room3"),
                    new SensorReadingDto("sensor1", 25.2, "celsius", Instant.now(), "room1"),
                    new SensorReadingDto("sensor2", 26.1, "celsius", Instant.now(), "room2")
            );
            SensorAggregateDto aggregate = client.uploadSensorData(readings);
            logger.info("Aggregate received: totalReadings={}, avg={}, min={}, max={}", 
                    aggregate.totalReadings(), 
                    aggregate.averageValue(), 
                    aggregate.minValue(), 
                    aggregate.maxValue());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while uploading sensor data", e);
        } catch (Exception e) {
            logger.error("Error in client streaming", e);
        }
    }

    private static void demonstrateBidirectionalStreaming(GrpcClient client) {
        logger.info("\n=== Bidirectional Streaming: Chat ===");
        try {
            List<ChatMessageDto> messages = List.of(
                    new ChatMessageDto("user1", "Alice", "Hello everyone!", Instant.now(), ChatMessageType.TEXT),
                    new ChatMessageDto("user1", "Alice", "How are you?", Instant.now(), ChatMessageType.TEXT),
                    new ChatMessageDto("user1", "Alice", "Goodbye!", Instant.now(), ChatMessageType.TEXT)
            );
            List<ChatMessageDto> responses = client.chat(messages);
            logger.info("Received {} chat responses", responses.size());
            responses.forEach(r -> logger.info("  - {}: {}", r.username(), r.content()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted during chat", e);
        } catch (Exception e) {
            logger.error("Error in bidirectional streaming", e);
        }
    }
}
