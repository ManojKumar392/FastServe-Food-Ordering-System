package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.CreateOrderRequest;
import com.manoj.fastserve.DTO.OrderItemRequest;
import com.manoj.fastserve.DTO.PaymentRequest;
import com.manoj.fastserve.DTO.PaymentResponse;
import com.manoj.fastserve.Entity.*;
import com.manoj.fastserve.Exception.BadRequestException;
import com.manoj.fastserve.Exception.ResourceNotFoundException;
import com.manoj.fastserve.Exception.UnauthorizedException;
import com.manoj.fastserve.Repository.MenuItemRepository;
import com.manoj.fastserve.Repository.OrderItemRepository;
import com.manoj.fastserve.Repository.OrderRepository;
import com.manoj.fastserve.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final PaymentGatewayService paymentGatewayService;


    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        MenuItemRepository menuItemRepository,
                        UserRepository userRepository,
                        PaymentGatewayService paymentGatewayService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
        this.paymentGatewayService = paymentGatewayService;
    }

    // GET all orders
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    // GET order by ID
    public Order getOrderById(Long id) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        }

        return orderRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() ->
                        new UnauthorizedException("Access denied: not your order"));
    }

    public Order markAsPaid(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setPaid(true);
        order.setStatus(OrderStatus.PAID);

        return orderRepository.save(order);
    }

    public Order updateStatus(Long id, OrderStatus status) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.DELIVERED &&
                status == OrderStatus.CANCELLED) {

            throw new BadRequestException(
                    "Delivered order cannot be cancelled"
            );
        }

        order.setStatus(status);

        return orderRepository.save(order);
    }

    public Order createOrder(CreateOrderRequest request) {

        User currentUser = getCurrentUser();

        Order order = new Order();
        order.setUser(currentUser);
        order.setPaymentMode(request.getPaymentMode());
        order.setPaid(false);
        order.setStatus(OrderStatus.PLACED);
        order.setPaymentStatus(PaymentStatus.PENDING);

        double total = 0;
        List<OrderItem> savedItems = new ArrayList<>();

        order = orderRepository.save(order);

        for (OrderItemRequest itemRequest : request.getItems()) {

            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItemName(menuItem.getName());
            orderItem.setPrice(menuItem.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());

            total += menuItem.getPrice() * itemRequest.getQuantity();

            savedItems.add(orderItemRepository.save(orderItem));
        }

        order.setItems(savedItems);
        order.setTotalPrice(total);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(order.getId());
        paymentRequest.setAmount(total);
        paymentRequest.setPaymentMode(order.getPaymentMode());

        PaymentResponse paymentResponse =
                paymentGatewayService.processPayment(paymentRequest);

        order.setPaymentStatus(paymentResponse.getStatus());
        order.setTransactionId(paymentResponse.getTransactionId());

        switch (paymentResponse.getStatus()) {

            case SUCCESS -> {
                order.setPaid(true);
                order.setStatus(OrderStatus.PAID);
            }

            case FAILED -> {
                order.setPaid(false);
                order.setStatus(OrderStatus.PLACED);
            }

            case PENDING -> {
                order.setPaid(false);
                order.setStatus(OrderStatus.PLACED);
            }
        }

        return orderRepository.save(order);
    }

    public Page<Order> getMyOrders(Pageable pageable) {

        User currentUser = getCurrentUser();

        return orderRepository.findByUserId(
                currentUser.getId(),
                pageable
        );
    }

    public Order cancelOrder(Long orderId) {

        User currentUser = getCurrentUser();

        Order order;

        if (currentUser.getRole() == Role.ADMIN) {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        } else {
            order = orderRepository.findByIdAndUserId(
                            orderId,
                            currentUser.getId()
                    )
                    .orElseThrow(() ->
                            new UnauthorizedException("Access denied: not your order"));
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel delivered order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        return orderRepository.save(order);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public Order retryPayment(Long orderId) {

        User currentUser = getCurrentUser();

        Order order;

        if (currentUser.getRole() == Role.ADMIN) {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        } else {
            order = orderRepository.findByIdAndUserId(orderId, currentUser.getId())
                    .orElseThrow(() ->
                            new UnauthorizedException("Access denied: not your order"));
        }

        if (order.getPaymentMode() == PaymentMode.CASH) {
            throw new BadRequestException(
                    "Cash on Delivery orders cannot be retried"
            );
        }

        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException(
                    "Payment has already been completed"
            );
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException(
                    "Cannot retry payment for a cancelled order"
            );
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException(
                    "Cannot retry payment for a delivered order"
            );
        }

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(order.getId());
        paymentRequest.setAmount(order.getTotalPrice());
        paymentRequest.setPaymentMode(order.getPaymentMode());

        PaymentResponse paymentResponse =
                paymentGatewayService.processPayment(paymentRequest);

        order.setPaymentStatus(paymentResponse.getStatus());
        order.setTransactionId(paymentResponse.getTransactionId());

        if (paymentResponse.getStatus() == PaymentStatus.SUCCESS) {
            order.setPaid(true);
            order.setStatus(OrderStatus.PAID);
        } else {
            order.setPaid(false);
        }

        return orderRepository.save(order);
    }
}