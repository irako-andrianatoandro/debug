package dev.irako.topics.grpc.data;

import dev.irako.topics.grpc.model.*;
import io.grpc.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GrpcIntegrationTest {

    private Server server;
    private GrpcClient client;
    private static final int TEST_PORT = 50052;

    @BeforeEach
    void setUp() throws IOException {
        server = GrpcServerFactory.createAndStartServer(TEST_PORT);
        client = new GrpcClient("localhost", TEST_PORT);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (client != null) {
            client.close();
        }
        if (server != null) {
            server.shutdown();
            server.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    @Test
    void sendMessage_unaryRpc_returnsSuccessResponse() throws Exception {
        MessageDto message = new MessageDto("user123", "Test message", Instant.now());
        
        MessageResponseDto response = client.sendMessage(message);
        
        assertTrue(response.success());
        assertNotNull(response.messageId());
        assertEquals("ACCEPTED", response.status());
    }

    @Test
    void subscribeToNotifications_serverStreaming_receivesNotifications() throws Exception {
        List<String> topics = List.of("news", "updates");
        
        List<NotificationDto> notifications = client.subscribeToNotifications("user123", topics);
        
        assertFalse(notifications.isEmpty());
        notifications.forEach(n -> {
            assertNotNull(n.notificationId());
            assertTrue(topics.contains(n.topic()));
            assertNotNull(n.priority());
        });
    }

    @Test
    void uploadSensorData_clientStreaming_returnsAggregate() throws Exception {
        List<SensorReadingDto> readings = List.of(
                new SensorReadingDto("sensor1", 20.0, "celsius", Instant.now(), "room1"),
                new SensorReadingDto("sensor2", 25.0, "celsius", Instant.now(), "room2"),
                new SensorReadingDto("sensor3", 30.0, "celsius", Instant.now(), "room3")
        );
        
        SensorAggregateDto aggregate = client.uploadSensorData(readings);
        
        assertEquals(3, aggregate.totalReadings());
        assertEquals(25.0, aggregate.averageValue(), 0.1);
        assertEquals(20.0, aggregate.minValue(), 0.1);
        assertEquals(30.0, aggregate.maxValue(), 0.1);
        assertEquals("celsius", aggregate.unit());
    }

    @Test
    void chat_bidirectionalStreaming_echoesMessages() throws Exception {
        List<ChatMessageDto> messages = List.of(
                new ChatMessageDto("user1", "Alice", "Hello", Instant.now(), ChatMessageType.TEXT),
                new ChatMessageDto("user1", "Alice", "World", Instant.now(), ChatMessageType.TEXT)
        );
        
        List<ChatMessageDto> responses = client.chat(messages);
        
        assertEquals(2, responses.size());
        assertEquals("Hello", responses.get(0).content());
        assertEquals("World", responses.get(1).content());
    }
}
