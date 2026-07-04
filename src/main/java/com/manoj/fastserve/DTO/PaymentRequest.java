package com.manoj.fastserve.DTO;

import com.manoj.fastserve.Entity.PaymentMode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentRequest {

    private Long orderId;

    private Double amount;

    private PaymentMode paymentMode;

    public PaymentRequest() {
    }

}