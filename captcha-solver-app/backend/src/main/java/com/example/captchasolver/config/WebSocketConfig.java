package com.example.captchasolver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enables a simple in-memory broker for topics prefixed with "/topic" and "/user" (for user-specific messages)
        config.enableSimpleBroker("/topic", "/user");
        // Sets the prefix for messages bound for @MessageMapping annotated methods in controllers
        config.setApplicationDestinationPrefixes("/app");
        // Configures the prefix for user-specific destinations.
        // When using SimpMessagingTemplate.convertAndSendToUser(username, destination, payload),
        // this prefix will be prepended to the destination.
        // e.g., convertAndSendToUser("john", "/queue/captcha-updates", msg) will actually send to "/user/john/queue/captcha-updates"
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registers the "/ws" endpoint, enabling SockJS fallback options for browsers that don't support WebSocket
        // AllowedOrigins("*") should be configured more restrictively in production.
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*") // TODO: Configure allowed origins appropriately for production
                .withSockJS();
    }
}
