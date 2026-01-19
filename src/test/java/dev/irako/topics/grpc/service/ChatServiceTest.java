package dev.irako.topics.grpc.service;

import dev.irako.topics.grpc.model.ChatMessageDto;
import dev.irako.topics.grpc.model.ChatMessageType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ChatServiceTest {

    @Test
    void processChatMessage_validMessage_returnsSameMessage() {
        ChatMessageDto message = new ChatMessageDto(
                "user1", "Alice", "Hello", Instant.now(), ChatMessageType.TEXT
        );
        List<ChatMessageDto> allMessages = List.of();
        
        ChatMessageDto processed = ChatService.processChatMessage(message, allMessages);
        
        assertEquals(message.userId(), processed.userId());
        assertEquals(message.content(), processed.content());
        assertEquals(message.type(), processed.type());
    }

    @Test
    void createJoinMessage_validInput_createsJoinMessage() {
        ChatMessageDto joinMessage = ChatService.createJoinMessage("user1", "Alice");
        
        assertEquals("user1", joinMessage.userId());
        assertEquals("Alice", joinMessage.username());
        assertTrue(joinMessage.content().contains("joined"));
        assertEquals(ChatMessageType.JOIN, joinMessage.type());
    }

    @Test
    void createLeaveMessage_validInput_createsLeaveMessage() {
        ChatMessageDto leaveMessage = ChatService.createLeaveMessage("user1", "Alice");
        
        assertEquals("user1", leaveMessage.userId());
        assertEquals("Alice", leaveMessage.username());
        assertTrue(leaveMessage.content().contains("left"));
        assertEquals(ChatMessageType.LEAVE, leaveMessage.type());
    }

    @Test
    void filterByType_mixedMessages_returnsOnlyMatchingType() {
        List<ChatMessageDto> messages = List.of(
                new ChatMessageDto("user1", "Alice", "Hello", Instant.now(), ChatMessageType.TEXT),
                new ChatMessageDto("user2", "Bob", "Joined", Instant.now(), ChatMessageType.JOIN),
                new ChatMessageDto("user1", "Alice", "Bye", Instant.now(), ChatMessageType.TEXT)
        );
        
        List<ChatMessageDto> textMessages = ChatService.filterByType(messages, ChatMessageType.TEXT)
                .collect(Collectors.toList());
        
        assertEquals(2, textMessages.size());
        textMessages.forEach(msg -> assertEquals(ChatMessageType.TEXT, msg.type()));
    }

    @Test
    void processChatMessage_nullMessage_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ChatService.processChatMessage(null, List.of());
        });
    }

    @Test
    void createJoinMessage_nullUserId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ChatService.createJoinMessage(null, "Alice");
        });
    }
}
