package com.manoj.fastserve.Filter;

import com.manoj.fastserve.Util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderWebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String ORDER_SUBSCRIPTION = "/user/queue/orders";

    private final JwtUtil jwtUtil;

    public OrderWebSocketAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            throw new MessageDeliveryException(message, "Invalid STOMP frame");
        }

        if (accessor.getCommand() == StompCommand.CONNECT) {
            authenticate(message, accessor);
            return message;
        }

        if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            if (accessor.getUser() == null
                    || !ORDER_SUBSCRIPTION.equals(accessor.getDestination())) {
                throw new MessageDeliveryException(
                        message,
                        "Only authenticated order updates can be subscribed to"
                );
            }
            return message;
        }

        if (accessor.getCommand() == StompCommand.DISCONNECT
                || accessor.getCommand() == StompCommand.UNSUBSCRIBE) {
            if (accessor.getUser() == null) {
                throw new MessageDeliveryException(message, "Authentication required");
            }
            return message;
        }

        throw new MessageDeliveryException(message, "STOMP command is not allowed");
    }

    private void authenticate(Message<?> message, StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new MessageDeliveryException(message, "Bearer token required");
        }

        String token = authorization.substring(7);
        try {
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            if (email == null || email.isBlank() || role == null || role.isBlank()) {
                throw new MessageDeliveryException(message, "Invalid access token");
            }

            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            ));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new MessageDeliveryException(message, "Invalid or expired access token", exception);
        }
    }
}
