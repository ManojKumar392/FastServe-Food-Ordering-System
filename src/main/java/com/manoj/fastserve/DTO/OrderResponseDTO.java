package com.manoj.fastserve.DTO;

import com.manoj.fastserve.Entity.OrderStatus;
import com.manoj.fastserve.Entity.PaymentMode;
import com.manoj.fastserve.Entity.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrderResponseDTO {

    private Long id;

    private Double totalPrice;

    private OrderStatus status;

    private PaymentMode paymentMode;

    private Boolean paid;

    private PaymentStatus paymentStatus;

    private String transactionId;

    private Integer estimatedDeliveryTime;

    private LocalDateTime createdAt;

    private LocalDateTime paymentTime;
}