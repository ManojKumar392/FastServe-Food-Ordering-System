package com.manoj.fastserve.DTO;

import com.manoj.fastserve.Entity.PaymentMode;
import com.manoj.fastserve.Entity.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentHistoryResponseDTO {

    private Long id;

    private PaymentStatus paymentStatus;

    private PaymentMode paymentMode;

    private String transactionId;

    private Double amount;

    private String message;

    private LocalDateTime createdAt;
}