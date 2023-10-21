package ru.nsu.carwash_server.configuration.sockets.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.nsu.carwash_server.models.secondary.constants.DestinationPrefixes;

import javax.annotation.PostConstruct;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class NotificationConfig {
    private final SimpMessagingTemplate messagingTemplate;

    @PostConstruct
    private void setDefaultDestination() {
        messagingTemplate.setDefaultDestination(DestinationPrefixes.NOTIFICATIONS + "/newOrder");
    }
}
