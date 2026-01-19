package dev.irako.topics.grpc.data;

import dev.irako.topics.grpc.model.*;
import dev.irako.topics.grpc.service.*;
import dev.irako.topics.grpc.model.StreamingServiceProto.*;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * gRPC service implementation. Handles I/O and delegates business logic to
 * service layer.
 */
public final class StreamingServiceImpl extends StreamingServiceGrpc.StreamingServiceImplBase {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StreamingServiceImpl.class);

	@Override
	public void sendMessage(MessageRequest request, StreamObserver<MessageResponse> responseObserver) {
		try {
			logger.debug("Received message request from user: {}", request.getUserId());

			// Convert proto to domain DTO
			MessageDto messageDto = ProtoConverter.toMessageDto(request);

			// Process using pure service function
			MessageResponseDto responseDto = MessageService.processMessage(messageDto);

			// Convert back to proto and send
			MessageResponse response = ProtoConverter.toProtoMessageResponse(responseDto);
			responseObserver.onNext(response);
			responseObserver.onCompleted();

			logger.debug("Message processed successfully: {}", responseDto.messageId());
		} catch (Exception e) {
			logger.error("Error processing message", e);
			responseObserver.onError(e);
		}
	}

	@Override
	public void subscribeToNotifications(SubscribeRequest request,
			StreamObserver<NotificationMessage> responseObserver) {
		try {
			logger.info("Client subscribed: userId={}, topics={}", request.getUserId(), request.getTopicsList());

			// Generate notifications using service layer
			List<String> topics = new ArrayList<>(request.getTopicsList());
			var notificationStream = NotificationService.generateNotifications(request.getUserId(), topics, 10 // Generate
																												// 10
																												// notifications
																												// as
																												// example
			);

			// Stream notifications to client
			notificationStream.forEach(notificationDto -> {
				NotificationMessage protoNotification = ProtoConverter.toProtoNotification(notificationDto);
				responseObserver.onNext(protoNotification);

				// Simulate delay between notifications
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					logger.warn("Interrupted while streaming notifications");
					responseObserver.onError(e);
					return;
				}
			});

			responseObserver.onCompleted();
			logger.info("Completed streaming notifications to user: {}", request.getUserId());
		} catch (Exception e) {
			logger.error("Error streaming notifications", e);
			responseObserver.onError(e);
		}
	}

	@Override
	public StreamObserver<SensorReading> uploadSensorData(StreamObserver<AggregateResponse> responseObserver) {
		final List<SensorReadingDto> readings = new ArrayList<>();
		final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

		return new StreamObserver<SensorReading>() {
			@Override
			public void onNext(SensorReading reading) {
				logger.debug("Received sensor reading: sensorId={}, value={}", reading.getSensorId(),
						reading.getValue());

				// Convert and accumulate
				SensorReadingDto dto = ProtoConverter.toSensorReadingDto(reading);
				readings.add(dto);
			}

			@Override
			public void onError(Throwable t) {
				logger.error("Error receiving sensor data", t);
				responseObserver.onError(t);
			}

			@Override
			public void onCompleted() {
				try {
					logger.info("Received {} sensor readings, computing aggregate", readings.size());

					// Aggregate using service layer
					long endTime = System.currentTimeMillis();
					SensorAggregateDto aggregate = SensorAggregationService.aggregateReadings(readings, startTime.get(),
							endTime);

					// Convert and send response
					AggregateResponse response = ProtoConverter.toProtoAggregateResponse(aggregate);
					responseObserver.onNext(response);
					responseObserver.onCompleted();

					logger.info("Sent aggregate response: totalReadings={}, avg={}", aggregate.totalReadings(),
							aggregate.averageValue());
				} catch (Exception e) {
					logger.error("Error aggregating sensor data", e);
					responseObserver.onError(e);
				}
			}
		};
	}

	@Override
	public StreamObserver<ChatMessage> chat(StreamObserver<ChatMessage> responseObserver) {
		final List<ChatMessageDto> allMessages = new ArrayList<>();

		return new StreamObserver<ChatMessage>() {
			@Override
			public void onNext(ChatMessage message) {
				try {
					logger.debug("Received chat message from: {}", message.getUsername());

					// Convert to domain DTO
					ChatMessageDto messageDto = ProtoConverter.toChatMessageDto(message);
					allMessages.add(messageDto);

					// Process using service layer
					ChatMessageDto processedMessage = ChatService.processChatMessage(messageDto, allMessages);

					// Echo back to client (in real system, might broadcast to all clients)
					ChatMessage response = ProtoConverter.toProtoChatMessage(processedMessage);
					responseObserver.onNext(response);

					logger.debug("Echoed chat message from: {}", message.getUsername());
				} catch (Exception e) {
					logger.error("Error processing chat message", e);
					responseObserver.onError(e);
				}
			}

			@Override
			public void onError(Throwable t) {
				logger.error("Error in chat stream", t);
				responseObserver.onError(t);
			}

			@Override
			public void onCompleted() {
				logger.info("Chat stream completed");
				responseObserver.onCompleted();
			}
		};
	}
}
