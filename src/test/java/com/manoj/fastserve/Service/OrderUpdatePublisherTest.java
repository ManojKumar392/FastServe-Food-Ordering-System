package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.OrderTrackingEvent;
import com.manoj.fastserve.Entity.Order;
import com.manoj.fastserve.Entity.OrderStatus;
import com.manoj.fastserve.Entity.PaymentMode;
import com.manoj.fastserve.Entity.PaymentStatus;
import com.manoj.fastserve.Entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OrderUpdatePublisherTest {

    @Test
    void publish_sendsOrderEventToOwningUserQueue() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        OrderUpdatePublisher publisher = new OrderUpdatePublisher(messagingTemplate);

        User user = new User();
        user.setEmail("alex@example.com");
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PREPARING);
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setPaymentMode(PaymentMode.CARD);
        order.setTotalPrice(250.0);

        publisher.publish(order);

        var event = org.mockito.ArgumentCaptor.forClass(OrderTrackingEvent.class);
        verify(messagingTemplate).convertAndSendToUser(
                eq("alex@example.com"),
                eq("/queue/orders"),
                event.capture()
        );
        assertEquals(OrderStatus.PREPARING, event.getValue().status());
        assertEquals(PaymentStatus.SUCCESS, event.getValue().paymentStatus());
    }
}
