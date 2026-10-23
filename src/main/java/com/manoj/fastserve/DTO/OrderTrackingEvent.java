package com.manoj.fastserve.DTO;

import com.manoj.fastserve.Entity.Order;
import com.manoj.fastserve.Entity.OrderStatus;
import com.manoj.fastserve.Entity.PaymentMode;
import com.manoj.fastserve.Entity.PaymentStatus;

import java.time.LocalDateTime;

public record OrderTrackingEvent(
        Long id,
        Double totalPrice,
        OrderStatus status,
        PaymentMode paymentMode,
        PaymentStatus paymentStatus,
        Integer estimatedDeliveryTime,
        LocalDateTime updatedAt
) {
    public static OrderTrackingEvent from(Order order) {
        return new OrderTrackingEvent(
                order.getId(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getPaymentMode(),
                order.getPaymentStatus(),
                order.getEstimatedDeliveryTime(),
                order.getUpdatedAt()
        );
    }
}
