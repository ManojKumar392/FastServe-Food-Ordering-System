package com.manoj.fastserve.Controller;

import com.manoj.fastserve.DTO.PaymentHistoryResponseDTO;
import com.manoj.fastserve.Service.PaymentHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;

    public PaymentHistoryController(
            PaymentHistoryService paymentHistoryService) {

        this.paymentHistoryService = paymentHistoryService;
    }

    @GetMapping("/{orderId}/payment-history")
    public ResponseEntity<List<PaymentHistoryResponseDTO>> getPaymentHistory(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                paymentHistoryService.getPaymentHistory(orderId)
        );
    }
}