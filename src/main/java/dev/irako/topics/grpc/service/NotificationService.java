package dev.irako.topics.grpc.service;

import dev.irako.topics.grpc.model.NotificationDto;
import dev.irako.topics.grpc.model.NotificationPriority;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Service for generating notifications. Pure functions with no side-effects or
 * I/O.
 */
public final class NotificationService {

	private NotificationService() {
		// Utility class
	}

	/**
	 * Generates a stream of notifications for the given user and topics. This
	 * simulates notifications that would be sent over time.
	 *
	 * @param userId
	 *            the user ID to generate notifications for
	 * @param topics
	 *            the topics to generate notifications for
	 * @param count
	 *            the number of notifications to generate
	 * @return stream of notification DTOs
	 */
	public static Stream<NotificationDto> generateNotifications(String userId, List<String> topics, int count) {
		if (userId == null || userId.isBlank()) {
			throw new IllegalArgumentException("userId cannot be null or blank");
		}
		if (topics == null || topics.isEmpty()) {
			throw new IllegalArgumentException("topics cannot be null or empty");
		}
		if (count < 0) {
			throw new IllegalArgumentException("count cannot be negative");
		}

		return Stream.iterate(0, i -> i < count, i -> i + 1).map(index -> {
			String topic = topics.get(index % topics.size());
			NotificationPriority priority = determinePriority(topic, index);
			return createNotification(userId, topic, priority, index);
		});
	}

	private static NotificationPriority determinePriority(String topic, int index) {
		// Simple priority logic: urgent for first notification, high for alerts, etc.
		if (topic.toLowerCase().contains("alert") || topic.toLowerCase().contains("urgent")) {
			return NotificationPriority.URGENT;
		}
		if (index == 0) {
			return NotificationPriority.HIGH;
		}
		if (topic.toLowerCase().contains("news")) {
			return NotificationPriority.MEDIUM;
		}
		return NotificationPriority.LOW;
	}

	private static NotificationDto createNotification(String userId, String topic, NotificationPriority priority,
			int index) {
		String notificationId = UUID.randomUUID().toString();
		String title = "Notification for " + topic;
		String content = String.format("This is notification #%d for topic: %s", index + 1, topic);
		Instant timestamp = Instant.now().plusSeconds(index); // Simulate time progression

		return new NotificationDto(notificationId, topic, title, content, timestamp, priority);
	}
}
