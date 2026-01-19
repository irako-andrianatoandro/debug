package dev.irako.topics.grpc.data;

import dev.irako.topics.grpc.model.*;
import dev.irako.topics.grpc.model.StreamingServiceProto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * gRPC client for interacting with the streaming service.
 * Handles I/O and converts between proto and domain DTOs.
 */
public final class GrpcClient implements AutoCloseable {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GrpcClient.class);
    
    private final ManagedChannel channel;
    private final StreamingServiceGrpc.StreamingServiceStub asyncStub;
    private final StreamingServiceGrpc.StreamingServiceBlockingStub blockingStub;

    /**
     * Creates a new gRPC client connected to the specified host and port.
     * 
     * @param host the server host
     * @param port the server port
     */
    public GrpcClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // For simplicity; use TLS in production
                .build();
        this.asyncStub = StreamingServiceGrpc.newStub(channel);
        this.blockingStub = StreamingServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Sends a message using unary RPC.
     * 
     * @param message the message to send
     * @return the response DTO
     */
    public MessageResponseDto sendMessage(MessageDto message) {
        MessageRequest request = MessageRequest.newBuilder()
                .setUserId(message.userId())
                .setContent(message.content())
                .setTimestamp(message.timestamp().toEpochMilli())
                .build();

        MessageResponse response = blockingStub.sendMessage(request);
        
        return new MessageResponseDto(
                response.getSuccess(),
                response.getMessageId(),
                response.getStatus()
        );
    }

    /**
     * Subscribes to notifications using server streaming.
     * 
     * @param userId the user ID
     * @param topics the topics to subscribe to
     * @return list of received notifications
     * @throws InterruptedException if interrupted while waiting
     */
    public List<NotificationDto> subscribeToNotifications(String userId, List<String> topics) 
            throws InterruptedException {
        SubscribeRequest request = SubscribeRequest.newBuilder()
                .setUserId(userId)
                .addAllTopics(topics)
                .build();

        final List<NotificationDto> notifications = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);

        asyncStub.subscribeToNotifications(request, new StreamObserver<NotificationMessage>() {
            @Override
            public void onNext(NotificationMessage notification) {
                NotificationDto dto = ProtoConverter.toNotificationDto(notification);
                notifications.add(dto);
                logger.debug("Received notification: {}", dto.notificationId());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error receiving notifications", t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.info("Notification stream completed. Received {} notifications", notifications.size());
                latch.countDown();
            }
        });

        latch.await(30, TimeUnit.SECONDS);
        return notifications;
    }

    /**
     * Uploads sensor data using client streaming.
     * 
     * @param readings the sensor readings to upload
     * @return the aggregate response DTO
     * @throws InterruptedException if interrupted while waiting
     */
    public SensorAggregateDto uploadSensorData(List<SensorReadingDto> readings) 
            throws InterruptedException {
        final SensorAggregateDto[] result = new SensorAggregateDto[1];
        final CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<SensorReading> requestObserver = asyncStub.uploadSensorData(
                new StreamObserver<AggregateResponse>() {
                    @Override
                    public void onNext(AggregateResponse response) {
                        result[0] = ProtoConverter.toSensorAggregateDto(response);
                        logger.info("Received aggregate: totalReadings={}, avg={}", 
                                response.getTotalReadings(), response.getAverageValue());
                    }

                    @Override
                    public void onError(Throwable t) {
                        logger.error("Error uploading sensor data", t);
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("Sensor data upload completed");
                        latch.countDown();
                    }
                }
        );

        // Send all readings
        for (SensorReadingDto reading : readings) {
            SensorReading protoReading = SensorReading.newBuilder()
                    .setSensorId(reading.sensorId())
                    .setValue(reading.value())
                    .setUnit(reading.unit())
                    .setTimestamp(reading.timestamp().toEpochMilli())
                    .setLocation(reading.location())
                    .build();
            requestObserver.onNext(protoReading);
        }

        requestObserver.onCompleted();
        latch.await(30, TimeUnit.SECONDS);

        if (result[0] == null) {
            throw new IllegalStateException("No aggregate response received");
        }
        return result[0];
    }

    /**
     * Sends chat messages using bidirectional streaming.
     * 
     * @param messages the messages to send
     * @return list of received responses
     * @throws InterruptedException if interrupted while waiting
     */
    public List<ChatMessageDto> chat(List<ChatMessageDto> messages) throws InterruptedException {
        final List<ChatMessageDto> responses = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<ChatMessage> requestObserver = asyncStub.chat(
                new StreamObserver<ChatMessage>() {
                    @Override
                    public void onNext(ChatMessage message) {
                        ChatMessageDto dto = ProtoConverter.toChatMessageDto(message);
                        responses.add(dto);
                        logger.debug("Received chat response: {}", dto.content());
                    }

                    @Override
                    public void onError(Throwable t) {
                        logger.error("Error in chat stream", t);
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("Chat stream completed. Received {} responses", responses.size());
                        latch.countDown();
                    }
                }
        );

        // Send all messages
        for (ChatMessageDto message : messages) {
            ChatMessage protoMessage = ProtoConverter.toProtoChatMessage(message);
            requestObserver.onNext(protoMessage);
        }

        requestObserver.onCompleted();
        latch.await(30, TimeUnit.SECONDS);

        return responses;
    }

    @Override
    public void close() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while closing channel");
        }
    }
}
