package com.manoj.fastserve.DTO;

import java.util.List;

public class CreateOrderRequest {

    private String paymentMode;
    private List<OrderItemRequest> items;

    public CreateOrderRequest() {
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}