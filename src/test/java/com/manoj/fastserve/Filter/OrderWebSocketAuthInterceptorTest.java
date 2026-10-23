package com.manoj.fastserve.Filter;

import com.manoj.fastserve.Util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderWebSocketAuthInterceptorTest {

    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final OrderWebSocketAuthInterceptor interceptor =
            new OrderWebSocketAuthInterceptor(jwtUtil);
    private final MessageChannel channel = mock(MessageChannel.class);

    @Test
    void connect_setsUserFromValidatedAccessToken() {
        when(jwtUtil.extractEmail("access-token")).thenReturn("alex@example.com");
        when(jwtUtil.extractRole("access-token")).thenReturn("USER");

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer access-token");
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> authenticated = interceptor.preSend(message, channel);
        Authentication user = (Authentication) StompHeaderAccessor.wrap(authenticated).getUser();

        assertEquals("alex@example.com", user.getName());
        assertTrue(user.getAuthorities().stream().anyMatch(
                authority -> authority.getAuthority().equals("ROLE_USER")
        ));
    }

    @Test
    void subscribe_rejectsOtherUsersAndDestinations() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/user/other/queue/orders");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(
                MessageDeliveryException.class,
                () -> interceptor.preSend(message, channel)
        );
    }

    @Test
    void subscribe_allowsAuthenticatedUsersOwnOrderQueue() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/user/queue/orders");
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                "alex@example.com",
                null,
                java.util.List.of()
        ));
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertSame(message, interceptor.preSend(message, channel));
    }

    @Test
    void connect_rejectsMissingBearerToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(
                MessageDeliveryException.class,
                () -> interceptor.preSend(message, channel)
        );
    }
}
