package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.PaymentRequest;
import com.manoj.fastserve.DTO.PaymentResponse;
import com.manoj.fastserve.Entity.PaymentMode;
import com.manoj.fastserve.Entity.PaymentStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MockPaymentGatewayService implements PaymentGatewayService {

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {

        if (request.getPaymentMode() == PaymentMode.CASH) {
            return new PaymentResponse(
                    PaymentStatus.PENDING,
                    null,
                    "Cash payment will be collected on delivery"
            );
        }

        boolean success = Math.random() < 0.8; // 80% success rate

        if (success) {
            return new PaymentResponse(
                    PaymentStatus.SUCCESS,
                    "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    "Payment successful"
            );
        }

        return new PaymentResponse(
                PaymentStatus.FAILED,
                null,
                "Payment failed"
        );
    }
}