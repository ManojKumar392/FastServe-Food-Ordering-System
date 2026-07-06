package com.manoj.fastserve.Controller;

import com.manoj.fastserve.DTO.CreateOrderRequest;
import com.manoj.fastserve.DTO.OrderResponseDTO;
import com.manoj.fastserve.Entity.Order;
import com.manoj.fastserve.Entity.OrderStatus;
import com.manoj.fastserve.Service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // GET all orders
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<Order>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    // GET one order
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/pay")
    public ResponseEntity<OrderResponseDTO> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markAsPaid(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        return ResponseEntity.ok(
                orderService.updateStatus(id, status)
        );
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        return new ResponseEntity<>(
                orderService.createOrder(request),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/my")
    public ResponseEntity<Page<Order>> getMyOrders(
            Pageable pageable) {

        return ResponseEntity.ok(
                orderService.getMyOrders(pageable)
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @PostMapping("/{id}/retry-payment")
    public ResponseEntity<OrderResponseDTO> retryPayment(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.retryPayment(id));
    }
}