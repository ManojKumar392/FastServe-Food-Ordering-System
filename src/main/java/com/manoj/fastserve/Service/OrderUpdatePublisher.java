package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.OrderTrackingEvent;
import com.manoj.fastserve.Entity.Order;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderUpdatePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public OrderUpdatePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(Order order) {
        messagingTemplate.convertAndSendToUser(
                order.getUser().getEmail(),
                "/queue/orders",
                OrderTrackingEvent.from(order)
        );
    }
}
