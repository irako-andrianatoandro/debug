package dev.irako.topics.grpc.service;

import dev.irako.topics.grpc.model.MessageDto;
import dev.irako.topics.grpc.model.MessageResponseDto;

import java.util.UUID;

/**
 * Service for processing messages. Pure functions with no side-effects or I/O.
 */
public final class MessageService {

	private MessageService() {
		// Utility class
	}

	/**
	 * Processes a message and generates a response.
	 *
	 * @param message
	 *            the message to process
	 * @return response indicating success and message ID
	 */
	public static MessageResponseDto processMessage(MessageDto message) {
		// Generate a unique message ID
		String messageId = UUID.randomUUID().toString();

		// Simple validation: check message length
		boolean success = message.content().length() <= 10000;
		String status = success ? "ACCEPTED" : "REJECTED_TOO_LONG";

		return new MessageResponseDto(success, messageId, status);
	}
}
