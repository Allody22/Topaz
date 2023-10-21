package ru.nsu.carwash_server.configuration.sockets.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import ru.nsu.carwash_server.models.secondary.constants.AppPrefixes;
import ru.nsu.carwash_server.models.secondary.constants.DestinationPrefixes;
import ru.nsu.carwash_server.models.secondary.constants.StompPaths;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(DestinationPrefixes.NOTIFICATIONS);
        registry.setApplicationDestinationPrefixes(AppPrefixes.APP);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(StompPaths.WEBSOCKETS).setAllowedOriginPatterns("**").withSockJS();
    }
}
