package com.manoj.fastserve.DTO;

import com.manoj.fastserve.Entity.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentResponse {

    private PaymentStatus status;

    private String transactionId;

    private String message;

    public PaymentResponse() {
    }

    public PaymentResponse(
            PaymentStatus status,
            String transactionId,
            String message
    ) {
        this.status = status;
        this.transactionId = transactionId;
        this.message = message;
    }

}