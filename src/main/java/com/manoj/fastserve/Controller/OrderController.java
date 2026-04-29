package com.manoj.fastserve.Controller;

import com.manoj.fastserve.DTO.CreateOrderRequest;
import com.manoj.fastserve.Entity.Order;
import com.manoj.fastserve.Entity.OrderStatus;
import com.manoj.fastserve.Service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order placeOrder(@RequestBody CreateOrderRequest request) {
        return orderService.placeOrder(request);
    }

    // GET all orders
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // GET one order
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PatchMapping("/{id}/pay")
    public Order markAsPaid(@PathVariable Long id) {
        return orderService.markAsPaid(id);
    }

    @PatchMapping("/{id}/status")
    public Order updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        return orderService.updateStatus(id, status);
    }

    @PostMapping("/user/{userId}")
    public Order createOrderForUser(
            @PathVariable Long userId,
            @RequestBody CreateOrderRequest request) {

        return orderService.createOrderForUser(userId, request);
    }

    @GetMapping("/users/{userId}")
    public List<Order> getOrdersByUserId(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }
}