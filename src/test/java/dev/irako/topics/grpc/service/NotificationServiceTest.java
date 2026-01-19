package dev.irako.topics.grpc.service;

import dev.irako.topics.grpc.model.NotificationDto;
import dev.irako.topics.grpc.model.NotificationPriority;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    @Test
    void generateNotifications_validInput_generatesCorrectCount() {
        List<String> topics = List.of("news", "updates");
        int count = 5;
        
        List<NotificationDto> notifications = NotificationService.generateNotifications("user123", topics, count)
                .collect(Collectors.toList());
        
        assertEquals(count, notifications.size());
    }

    @Test
    void generateNotifications_validInput_allNotificationsHaveValidFields() {
        List<String> topics = List.of("news");
        int count = 3;
        
        List<NotificationDto> notifications = NotificationService.generateNotifications("user123", topics, count)
                .collect(Collectors.toList());
        
        notifications.forEach(notification -> {
            assertNotNull(notification.notificationId());
            assertFalse(notification.notificationId().isBlank());
            assertEquals("news", notification.topic());
            assertNotNull(notification.title());
            assertNotNull(notification.content());
            assertNotNull(notification.timestamp());
            assertNotNull(notification.priority());
        });
    }

    @Test
    void generateNotifications_alertTopic_hasUrgentPriority() {
        List<String> topics = List.of("alert");
        int count = 1;
        
        List<NotificationDto> notifications = NotificationService.generateNotifications("user123", topics, count)
                .collect(Collectors.toList());
        
        assertEquals(NotificationPriority.URGENT, notifications.get(0).priority());
    }

    @Test
    void generateNotifications_nullUserId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            NotificationService.generateNotifications(null, List.of("news"), 1)
                    .collect(Collectors.toList());
        });
    }

    @Test
    void generateNotifications_emptyTopics_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            NotificationService.generateNotifications("user123", List.of(), 1)
                    .collect(Collectors.toList());
        });
    }

    @Test
    void generateNotifications_negativeCount_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            NotificationService.generateNotifications("user123", List.of("news"), -1)
                    .collect(Collectors.toList());
        });
    }
}
