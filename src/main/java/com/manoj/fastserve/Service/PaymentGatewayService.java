package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.PaymentRequest;
import com.manoj.fastserve.DTO.PaymentResponse;

public interface PaymentGatewayService {

    PaymentResponse processPayment(PaymentRequest request);

}