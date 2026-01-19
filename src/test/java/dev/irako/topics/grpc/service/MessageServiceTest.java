package dev.irako.topics.grpc.service;

import dev.irako.topics.grpc.model.MessageDto;
import dev.irako.topics.grpc.model.MessageResponseDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {

    @Test
    void processMessage_validMessage_returnsSuccessResponse() {
        MessageDto message = new MessageDto("user123", "Hello", Instant.now());
        
        MessageResponseDto response = MessageService.processMessage(message);
        
        assertTrue(response.success());
        assertNotNull(response.messageId());
        assertEquals("ACCEPTED", response.status());
    }

    @Test
    void processMessage_tooLongMessage_returnsRejectedResponse() {
        String longContent = "x".repeat(10001);
        MessageDto message = new MessageDto("user123", longContent, Instant.now());
        
        MessageResponseDto response = MessageService.processMessage(message);
        
        assertFalse(response.success());
        assertEquals("REJECTED_TOO_LONG", response.status());
    }
}
